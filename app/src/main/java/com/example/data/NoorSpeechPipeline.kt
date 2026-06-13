package com.example.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.google.android.gms.tasks.Task
import java.util.Locale

suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
  addOnCompleteListener { task ->
    if (task.isSuccessful) {
      continuation.resume(task.result)
    } else {
      continuation.resumeWithException(task.exception ?: RuntimeException("Task failed"))
    }
  }
}

sealed class NoorIntent {
  data class PlaySurah(val surahNumber: Int, val verseNumber: Int = 1) : NoorIntent()
  data class PlayJuz(val juzNumber: Int) : NoorIntent()
  data object Pause : NoorIntent()
  data object Resume : NoorIntent()
  data object Bookmark : NoorIntent()
  data class JumpToVerse(val verseNumber: Int) : NoorIntent()
  data object Forward : NoorIntent()
  data object Backward : NoorIntent()
  data object QueryContext : NoorIntent()
  data object Unknown : NoorIntent()
}

class NoorSpeechPipeline(
  private val context: Context,
  private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

  private var tts: TextToSpeech? = null
  private var isTtsInitialized = false

  val isSttAvailable: Boolean
    get() = SpeechRecognizer.isRecognitionAvailable(context)

  private var speechRecognizer: SpeechRecognizer? = null
  private var recognizerIntent: Intent? = null

  private val _isListening = MutableStateFlow(false)
  val isListening = _isListening.asStateFlow()

  private val _recognizedText = MutableStateFlow("")
  val recognizedText = _recognizedText.asStateFlow()

  private val _translatedText = MutableStateFlow("")
  val translatedText = _translatedText.asStateFlow()

  private val _detectedLanguage = MutableStateFlow("en")
  val detectedLanguage = _detectedLanguage.asStateFlow()

  private val _isTtsSpeaking = MutableStateFlow(false)
  val isTtsSpeaking = _isTtsSpeaking.asStateFlow()

  var onIntentResolved: ((NoorIntent) -> Unit)? = null
  var onErrorOccurred: ((String) -> Unit)? = null

  // ML Kit language models
  private val languageIdentifier = LanguageIdentification.getClient()
  
  // Translators
  private val urToEnTranslator = Translation.getClient(
    TranslatorOptions.Builder()
      .setSourceLanguage(TranslateLanguage.URDU)
      .setTargetLanguage(TranslateLanguage.ENGLISH)
      .build()
  )

  private val arToEnTranslator = Translation.getClient(
    TranslatorOptions.Builder()
      .setSourceLanguage(TranslateLanguage.ARABIC)
      .setTargetLanguage(TranslateLanguage.ENGLISH)
      .build()
  )

  init {
    scope.launch(Dispatchers.Main) {
      initializeTts()
      initializeStt()
      downloadTranslationModels()
    }
  }

  private fun initializeTts() {
    tts = TextToSpeech(context, this)
  }

  override fun onInit(status: Int) {
    if (status == TextToSpeech.SUCCESS) {
      val result = tts?.setLanguage(Locale.getDefault())
      if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
        tts?.setLanguage(Locale.ENGLISH)
      }
      isTtsInitialized = true
    } else {
      Log.e("NoorSpeechPipeline", "TTS Initialization failed")
    }
  }

  fun speak(text: String) {
    if (!isTtsInitialized) {
      Log.w("NoorSpeechPipeline", "TTS not initialized yet")
      return
    }
    _isTtsSpeaking.value = true
    tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "NoorSpeech")
    
    // Simple state timing fallback as TTS done listener is optional
    scope.launch {
      kotlinx.coroutines.delay((text.split(" ").size * 400L).coerceAtLeast(1200L))
      _isTtsSpeaking.value = false
    }
  }

  private fun initializeStt() {
    if (SpeechRecognizer.isRecognitionAvailable(context)) {
      speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
      recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        // Accept multiple preferred languages
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayListOf("en", "ur", "ar"))
      }

      speechRecognizer?.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
          _isListening.value = true
          _recognizedText.value = ""
          _translatedText.value = ""
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        
        override fun onEndOfSpeech() {
          _isListening.value = false
        }

        override fun onError(error: Int) {
          _isListening.value = false
          val message = when (error) {
            SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_NETWORK -> "Network interface failure"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions required"
            else -> "Speech engine error ($error)"
          }
          onErrorOccurred?.invoke(message)
        }

        override fun onResults(results: Bundle?) {
          val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          if (!matches.isNullOrEmpty()) {
            processSpokenText(matches[0])
          }
        }

        override fun onPartialResults(partialResults: Bundle?) {
          val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
          if (!matches.isNullOrEmpty()) {
            _recognizedText.value = matches[0]
          }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
      })
    }
  }

  private fun downloadTranslationModels() {
    scope.launch(Dispatchers.IO) {
      try {
        urToEnTranslator.downloadModelIfNeeded().awaitTask()
        arToEnTranslator.downloadModelIfNeeded().awaitTask()
        Log.i("NoorSpeechPipeline", "ML Kit model download complete")
      } catch (e: Exception) {
        Log.e("NoorSpeechPipeline", "ML Kit download failed", e)
      }
    }
  }

  fun startListening() {
    speechRecognizer?.startListening(recognizerIntent)
  }

  fun stopListening() {
    speechRecognizer?.stopListening()
    _isListening.value = false
  }

  fun containsArabicOrUrdu(text: String): Boolean {
    return text.any { it.code in 0x0600..0x06FF }
  }

  fun processSpokenText(text: String) {
    _recognizedText.value = text
    scope.launch(Dispatchers.IO) {
      try {
        var langCode = "en"
        var englishText = text

        if (containsArabicOrUrdu(text)) {
          kotlinx.coroutines.withTimeoutOrNull(1500L) {
            langCode = languageIdentifier.identifyLanguage(text).awaitTask()
            _detectedLanguage.value = langCode
            Log.i("NoorSpeechPipeline", "Detected language: $langCode for '$text'")

            englishText = when (langCode) {
              "ur" -> {
                val trans = urToEnTranslator.translate(text).awaitTask()
                _translatedText.value = trans
                trans
              }
              "ar" -> {
                val trans = arToEnTranslator.translate(text).awaitTask()
                _translatedText.value = trans
                trans
              }
              else -> {
                _translatedText.value = text
                text
              }
            }
          }
        } else {
          _detectedLanguage.value = "en"
          _translatedText.value = text
        }

        // 3. Resolve Intent
        val resolvedIntent = parseEnglishIntent(englishText, text)
        onIntentResolved?.invoke(resolvedIntent)

      } catch (e: Exception) {
        Log.e("NoorSpeechPipeline", "Speech processing error", e)
        // Fallback directly to English parser with raw text
        val resolvedIntent = parseEnglishIntent(text, text)
        onIntentResolved?.invoke(resolvedIntent)
      }
    }
  }

  private fun parseEnglishIntent(englishText: String, originalText: String): NoorIntent {
    val cleanText = englishText.lowercase().trim()
    val rawText = originalText.lowercase()

    // Fallback checks for direct Urdu/Arabic buttons in case translation didn't run
    if (rawText.contains("روکو") || rawText.contains("وقف") || cleanText.contains("stop") || cleanText.contains("pause")) {
      return NoorIntent.Pause
    }
    if (rawText.contains("چلاؤ") || rawText.contains("اقرا") || rawText.contains("إقرأ") || cleanText == "play" || cleanText.contains("resume") || cleanText == "start") {
      return NoorIntent.Resume
    }
    if (cleanText.contains("bookmark") || cleanText.contains("save this") || cleanText.contains("pin this") || rawText.contains("نشانی")) {
      return NoorIntent.Bookmark
    }
    if (cleanText.contains("next") || cleanText.contains("forward") || cleanText.contains("skip") || cleanText.contains("fast forward")) {
      return NoorIntent.Forward
    }
    if (cleanText.contains("back") || cleanText.contains("previous") || cleanText.contains("rewind") || cleanText.contains("go back")) {
      return NoorIntent.Backward
    }
    if (cleanText.contains("where am i") || cleanText.contains("which surah") || cleanText.contains("current position") || cleanText.contains("what is playing")) {
      return NoorIntent.QueryContext
    }

    // Capture Juz intents: e.g. "juz 30", "play juz 12"
    val juzRegex = Regex(".*juz(?:_)?(\\d+).*|.*para(?:_)?(\\d+).*|.*part(?:_)?(\\d+).*")
    val juzMatch = juzRegex.find(cleanText)
    if (juzMatch != null) {
      val numStr = juzMatch.groupValues.firstOrNull { it.isNotEmpty() && it.any { c -> c.isDigit() } }
      numStr?.toIntOrNull()?.let { juzNum ->
        if (juzNum in 1..30) {
          return NoorIntent.PlayJuz(juzNum)
        }
      }
    }

    // Capture verse jump intents: e.g. "go to verse 15", "verse 50", "ayah 10"
    val verseRegex = Regex(".*(?:verse|ayah|verse number|ayah number|sentence)\\s*(\\d+).*")
    val verseMatch = verseRegex.find(cleanText)
    if (verseMatch != null) {
      verseMatch.groupValues.getOrNull(1)?.toIntOrNull()?.let { verseNum ->
        return NoorIntent.JumpToVerse(verseNum)
      }
    }

    // Capture play Surah intents: e.g. "play surah rehman", "play surah 2 verse 12", "play surah mulk"
    // Also captures numeric style "play chapter 36"
    val surahNumRegex = Regex(".*(?:surah|chapter|sura)\\s*(\\d+).*")
    val surahNumMatch = surahNumRegex.find(cleanText)
    if (surahNumMatch != null) {
      val surahNo = surahNumMatch.groupValues[1].toIntOrNull()
      if (surahNo in 1..114) {
        // Check for verse trailing
        var verseNo = 1
        val extraVerseRegex = Regex(".*verse\\s*(\\d+).*|.*ayah\\s*(\\d+).*")
        val extraVerseMatch = extraVerseRegex.find(cleanText)
        if (extraVerseMatch != null) {
          val vNo = extraVerseMatch.groupValues.firstOrNull { it.isNotEmpty() && it.all { c -> c.isDigit() } }?.toIntOrNull()
          if (vNo != null) verseNo = vNo
        }
        return NoorIntent.PlaySurah(surahNo!!, verseNo)
      }
    }

    // If surah name was spoken: find matching surah
    val surahsList = QuranData.surahs
    for (s in surahsList) {
      val namesToCompare = listOf(
        s.englishName.lowercase(),
        s.phoneticName.lowercase(),
        s.englishName.lowercase().replace("-", " "),
        s.englishName.lowercase().replace("al-", ""),
        s.englishName.lowercase().replace("an-", "")
      )
      
      for (name in namesToCompare) {
        if (cleanText.contains(name)) {
          // Found matching surah name! Check if a verse was mentioned too
          var verseNo = 1
          val verseNumRegex = Regex(".*?(?:verse|ayah|ayah number|verse number)\\s*(\\d+).*")
          val verseNumMatch = verseNumRegex.find(cleanText)
          if (verseNumMatch != null) {
            val fetchedVerse = verseNumMatch.groupValues[1].toIntOrNull()
            if (fetchedVerse != null && fetchedVerse <= s.verseCount) {
              verseNo = fetchedVerse
            }
          } else {
            // Check if user spoke a number directly after the surah name, e.g. "play surah rahman 10"
            val directNumRegex = Regex(Regex.escape(name) + "\\s+(\\d+)")
            val directNumMatch = directNumRegex.find(cleanText)
            if (directNumMatch != null) {
              val fetchedVerse = directNumMatch.groupValues[1].toIntOrNull()
              if (fetchedVerse != null && fetchedVerse <= s.verseCount) {
                verseNo = fetchedVerse
              }
            }
          }
          return NoorIntent.PlaySurah(s.number, verseNo)
        }
      }
    }

    // If it contains "play" followed just by a number, see if we can resolve it to that surah number
    val generalPlayNumberRegex = Regex(".*play\\s*(\\d+).*")
    val genPlayMatch = generalPlayNumberRegex.find(cleanText)
    if (genPlayMatch != null) {
      val num = genPlayMatch.groupValues[1].toIntOrNull()
      if (num in 1..114) {
        return NoorIntent.PlaySurah(num!!)
      }
    }

    return NoorIntent.Unknown
  }

  fun release() {
    speechRecognizer?.destroy()
    speechRecognizer = null
    tts?.stop()
    tts?.shutdown()
    tts = null
  }
}
