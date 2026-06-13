package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
  ONBOARDING,
  HOME,
  HISTORY,
  BOOKMARKS,
  LISTENING_HISTORY,
  SURAH_BROWSER,
  SETTINGS
}

enum class OrbState {
  IDLE,
  LISTENING,
  PROCESSING,
  SPEAKING,
  PLAYING
}

class ConversationViewModel(application: Application) : AndroidViewModel(application) {

  private val context = application.applicationContext
  private val database = NoorDatabase.getDatabase(context)
  private val noorDao = database.noorDao()

  // 1. SUB-COMPONENTS
  val audioPlayer = NoorAudioPlayer(context)
  val downloadManager = NoorDownloadManager(context, noorDao, viewModelScope)
  val speechPipeline = NoorSpeechPipeline(context, viewModelScope)

  // 2. UI STATES
  private val _currentScreen = MutableStateFlow(AppScreen.ONBOARDING)
  val currentScreen = _currentScreen.asStateFlow()

  private val _onboardingStep = MutableStateFlow(1)
  val onboardingStep = _onboardingStep.asStateFlow()

  private val _orbState = MutableStateFlow(OrbState.IDLE)
  val orbState = _orbState.asStateFlow()

  private val _isListening = MutableStateFlow(false)
  val isListening = _isListening.asStateFlow()

  private val _activeSessionId = MutableStateFlow(UUID.randomUUID().toString())
  val activeSessionId = _activeSessionId.asStateFlow()

  // Reactive DB flows
  val bookmarks = noorDao.getAllBookmarksFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val listeningHistory = noorDao.getListeningHistoryFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val downloadedSurahs = noorDao.getAllDownloadedSurahsFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allExchanges = noorDao.getAllExchangesFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSessions = noorDao.getAllSessionsFlow()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _isBookmarked = MutableStateFlow(false)
  val isBookmarked = _isBookmarked.asStateFlow()

  // Settings in-memory (and backed by simple SharedPreferences for persistence)
  private val prefs = context.getSharedPreferences("noor_prefs", Application.MODE_PRIVATE)

  private val _reciter = MutableStateFlow(prefs.getString("reciter", "ar.alafasy") ?: "ar.alafasy")
  val reciter = _reciter.asStateFlow()

  private val _userLanguage = MutableStateFlow(prefs.getString("language", "English") ?: "English")
  val userLanguage = _userLanguage.asStateFlow()

  private val _autoContinue = MutableStateFlow(prefs.getBoolean("auto_continue", true))
  val autoContinue = _autoContinue.asStateFlow()

  private val _handsFreeMode = MutableStateFlow(prefs.getBoolean("hands_free", false))
  val handsFreeMode = _handsFreeMode.asStateFlow()

  private val _wakeSensitivity = MutableStateFlow(prefs.getString("sensitivity", "Medium") ?: "Medium")
  val wakeSensitivity = _wakeSensitivity.asStateFlow()

  private val _pushToHold = MutableStateFlow(prefs.getBoolean("push_to_hold", true))
  val pushToHold = _pushToHold.asStateFlow()

  init {
    // Check if onboarding already finished
    val onboardingDone = prefs.getBoolean("onboarding_done", false)
    if (onboardingDone) {
      _currentScreen.value = AppScreen.HOME
    }

    // Start a fresh session
    startNewSession()

    // Setup speech pipelines callbacks
    setupSpeechCallbacks()

    // Setup player callbacks
    setupPlayerCallbacks()
  }

  private fun startNewSession() {
    val sessionId = UUID.randomUUID().toString()
    _activeSessionId.value = sessionId
    viewModelScope.launch(Dispatchers.IO) {
      noorDao.insertSession(
        SessionEntity(
          id = sessionId,
          startedAt = System.currentTimeMillis()
        )
      )
    }
  }

  private fun setupSpeechCallbacks() {
    speechPipeline.onIntentResolved = { intent ->
      _orbState.value = OrbState.PROCESSING
      viewModelScope.launch(Dispatchers.Main) {
        handleResolvedIntent(intent)
      }
    }

    speechPipeline.onSpeechStarted = {
      viewModelScope.launch(Dispatchers.Main) {
        if (audioPlayer.isPlaying.value) {
          audioPlayer.pause()
        }
      }
    }

    speechPipeline.onListeningFinished = {
      _isListening.value = false
    }

    speechPipeline.onErrorOccurred = { errMessage ->
      viewModelScope.launch {
        Log.e("ConversationViewModel", "Speech error: $errMessage")
        _orbState.value = if (audioPlayer.isPlaying.value) OrbState.PLAYING else OrbState.IDLE
      }
    }
    
    // Track speech pipeline states
    viewModelScope.launch {
      combine(
        _isListening,
        speechPipeline.isListening,
        speechPipeline.isTtsSpeaking,
        audioPlayer.isPlaying
      ) { manualListening, bgListening, TtsSpeaking, playing ->
        when {
          manualListening || bgListening -> OrbState.LISTENING
          TtsSpeaking -> OrbState.SPEAKING
          playing -> OrbState.PLAYING
          else -> OrbState.IDLE
        }
      }.collect { calculatedState ->
        _orbState.value = calculatedState
      }
    }

    if (_handsFreeMode.value) {
      speechPipeline.startListening()
    }
  }

  private fun setupPlayerCallbacks() {
    audioPlayer.onVerseCompleted = {
      viewModelScope.launch {
        if (_autoContinue.value) {
          nextVerse()
        } else {
          _orbState.value = OrbState.IDLE
        }
      }
    }

    // Collect bookmarks state updates
    viewModelScope.launch {
      combine(audioPlayer.currentSurah, audioPlayer.currentVerse, bookmarks) { s, v, bks ->
        if (s != null && v != null) {
          bks.any { it.surahNumber == s && it.verseNumber == v }
        } else {
          false
        }
      }.collect {
        _isBookmarked.value = it
      }
    }
  }

  private suspend fun handleResolvedIntent(intent: NoorIntent) {
    val userSpeech = speechPipeline.recognizedText.value

    val responseText = when (intent) {
      is NoorIntent.PlaySurah -> {
        val s = QuranData.surahs.firstOrNull { it.number == intent.surahNumber }
        if (s != null) {
          if (intent.verseNumber > s.verseCount) {
             "Surah ${s.englishName} contains only ${s.verseCount} verses."
          } else {
            // Save Listening History Log
            viewModelScope.launch(Dispatchers.IO) {
              noorDao.insertListeningHistory(
                ListeningHistoryEntity(
                  surahNumber = s.number,
                  surahName = s.englishName,
                  startVerse = intent.verseNumber,
                  endVerse = null,
                  durationSeconds = 0,
                  playedAt = System.currentTimeMillis(),
                  completed = false
                )
              )
            }
            if (intent.verseNumber == 1) "Now reciting Surah ${s.englishName}." else "Now reciting Surah ${s.englishName}, beginning from verse ${intent.verseNumber}."
          }
        } else {
          "I couldn't find that Surah. Please try again."
        }
      }
      is NoorIntent.PlayJuz -> {
        "Starting recitation of Juz ${intent.juzNumber}."
      }
      is NoorIntent.Pause -> {
        "Recitation paused."
      }
      is NoorIntent.Resume -> {
        "Resuming recitation."
      }
      is NoorIntent.Bookmark -> {
        val sNum = audioPlayer.currentSurah.value
        val vNum = audioPlayer.currentVerse.value
        if (sNum != null && vNum != null) {
          val s = QuranData.surahs.firstOrNull { it.number == sNum }
          if (s != null) {
            val key = "${sNum}_${vNum}"
            val finished = noorDao.isBookmarked(key)
            if (finished) {
              noorDao.deleteBookmarkById(key)
              "Bookmark removed."
            } else {
              noorDao.insertBookmark(
                BookmarkEntity(
                  id = key,
                  surahNumber = sNum,
                  surahName = s.englishName,
                  verseNumber = vNum,
                  juzNumber = s.juzStart,
                  savedAt = System.currentTimeMillis()
                )
              )
              "Verse bookmarked."
            }
          } else "I'm unable to bookmark this position right now."
        } else {
          "There is no active recitation to bookmark."
        }
      }
      is NoorIntent.JumpToVerse -> {
        val sNum = audioPlayer.currentSurah.value
        if (sNum != null) {
          val s = QuranData.surahs.firstOrNull { it.number == sNum }
          if (s != null && intent.verseNumber <= s.verseCount) {
            "Jumping to verse ${intent.verseNumber}."
          } else {
            "This Surah has only ${s?.verseCount ?: 0} verses."
          }
        } else {
          "Please select a Surah before specifying a verse."
        }
      }
      is NoorIntent.Forward -> {
        "Moving to the next verse."
      }
      is NoorIntent.Backward -> {
        "Returning to the previous verse."
      }
      is NoorIntent.QueryContext -> {
        val sNum = audioPlayer.currentSurah.value
        val vNum = audioPlayer.currentVerse.value
        if (sNum != null && vNum != null) {
          val s = QuranData.surahs.firstOrNull { it.number == sNum }
          "You are listening to Surah ${s?.englishName}, verse $vNum."
        } else {
          "No recitation is currently active."
        }
      }
      is NoorIntent.Unknown -> {
        "I'm sorry, I didn't quite catch that. You can ask me to play a Surah, Juz, or control playback."
      }
    }

    // Update state to speaking confirmation vocally
    _orbState.value = OrbState.SPEAKING
    
    // Save to Exchanges DB
    viewModelScope.launch(Dispatchers.IO) {
      var sNo: Int? = null
      var vNo: Int? = null
      if (intent is NoorIntent.PlaySurah) {
        sNo = intent.surahNumber
        vNo = intent.verseNumber
      }
      noorDao.insertExchange(
        ExchangeEntity(
          sessionId = _activeSessionId.value,
          userText = userSpeech,
          noorText = responseText,
          intentType = intent::class.simpleName,
          surahNumber = sNo,
          verseNumber = vNo,
          timestamp = System.currentTimeMillis()
        )
      )
    }

    // Speak confirmation response vocally
    speechPipeline.speak(responseText)

    // Execute actual playback or state command
    when (intent) {
      is NoorIntent.PlaySurah -> {
        val s = QuranData.surahs.firstOrNull { it.number == intent.surahNumber }
        if (s != null && intent.verseNumber <= s.verseCount) {
          audioPlayer.playVerse(intent.surahNumber, intent.verseNumber, _reciter.value)
        }
      }
      is NoorIntent.PlayJuz -> {
        // Juz starts at specific chapter and verse
        val firstSurahOfJuz = QuranData.surahs.firstOrNull { it.juzStart == intent.juzNumber }
        if (firstSurahOfJuz != null) {
          audioPlayer.playVerse(firstSurahOfJuz.number, 1, _reciter.value)
        }
      }
      is NoorIntent.Pause -> {
        audioPlayer.pause()
      }
      is NoorIntent.Resume -> {
        audioPlayer.play()
      }
      is NoorIntent.JumpToVerse -> {
        val sNum = audioPlayer.currentSurah.value
        if (sNum != null) {
          val s = QuranData.surahs.firstOrNull { it.number == sNum }
          if (s != null && intent.verseNumber <= s.verseCount) {
            audioPlayer.playVerse(sNum, intent.verseNumber, _reciter.value)
          }
        }
      }
      is NoorIntent.Forward -> {
        nextVerse()
      }
      is NoorIntent.Backward -> {
        previousVerse()
      }
      else -> {
        // Do nothing further for queries/bookmarks as state has changed or speech finished
      }
    }
  }

  // TRANS ACTIONS
  fun nextSurah() {
    val sNum = audioPlayer.currentSurah.value ?: return
    if (sNum < 114) {
      audioPlayer.playVerse(sNum + 1, 1, _reciter.value)
    } else {
      viewModelScope.launch {
        speechPipeline.speak("You have reached the end of the Quran.")
      }
    }
  }

  fun previousSurah() {
    val sNum = audioPlayer.currentSurah.value ?: return
    if (sNum > 1) {
      audioPlayer.playVerse(sNum - 1, 1, _reciter.value)
    } else {
      viewModelScope.launch {
        speechPipeline.speak("You are already at the first Surah.")
      }
    }
  }

  fun nextVerse() {
    val sNum = audioPlayer.currentSurah.value ?: return
    val vNum = audioPlayer.currentVerse.value ?: return
    val s = QuranData.surahs.firstOrNull { it.number == sNum } ?: return

    if (vNum < s.verseCount) {
      audioPlayer.playVerse(sNum, vNum + 1, _reciter.value)
    } else if (sNum < 114) {
      // Transition to next Surah!
      audioPlayer.playVerse(sNum + 1, 1, _reciter.value)
    } else {
      audioPlayer.stop()
      viewModelScope.launch {
        speechPipeline.speak("End of the Quran recitation.")
      }
    }
  }

  fun previousVerse() {
    val sNum = audioPlayer.currentSurah.value ?: return
    val vNum = audioPlayer.currentVerse.value ?: return

    if (vNum > 1) {
      audioPlayer.playVerse(sNum, vNum - 1, _reciter.value)
    } else if (sNum > 1) {
      // Transition to previous Surah's last verse!
      val prevS = QuranData.surahs.firstOrNull { it.number == sNum - 1 } ?: return
      audioPlayer.playVerse(sNum - 1, prevS.verseCount, _reciter.value)
    }
  }

  fun togglePlayPause() {
    if (audioPlayer.currentSurah.value == null) {
      // Default: play surah fatihah first verse to start
      audioPlayer.playVerse(1, 1, _reciter.value)
    } else {
      audioPlayer.togglePlayPause()
    }
  }

  fun toggleBookmark() {
    val currS = audioPlayer.currentSurah.value ?: return
    val currV = audioPlayer.currentVerse.value ?: return
    viewModelScope.launch(Dispatchers.IO) {
      val key = "${currS}_${currV}"
      val finished = noorDao.isBookmarked(key)
      if (finished) {
        noorDao.deleteBookmarkById(key)
        viewModelScope.launch { speechPipeline.speak("Bookmark removed.") }
      } else {
        val s = QuranData.surahs.firstOrNull { it.number == currS } ?: return@launch
        noorDao.insertBookmark(
          BookmarkEntity(
            id = key,
            surahNumber = currS,
            surahName = s.englishName,
            verseNumber = currV,
            juzNumber = s.juzStart,
            savedAt = System.currentTimeMillis()
          )
        )
        viewModelScope.launch { speechPipeline.speak("Bookmarked.") }
      }
    }
  }

  fun playBookmark(bookmark: BookmarkEntity) {
    _currentScreen.value = AppScreen.HOME
    audioPlayer.playVerse(bookmark.surahNumber, bookmark.verseNumber, _reciter.value)
  }

  // NAVIGATION ACTIONS
  fun navigateTo(screen: AppScreen, addToBackStack: Boolean = true) {
    if (addToBackStack && _currentScreen.value != screen) {
      // Simple logic: if we go to a sub-screen, we can always go back to HOME
      // For more complex apps we'd use a real stack, but this suffices for now
    }
    _currentScreen.value = screen
  }

  fun handleBack(): Boolean {
    return if (_currentScreen.value != AppScreen.HOME && _currentScreen.value != AppScreen.ONBOARDING) {
      _currentScreen.value = AppScreen.HOME
      true
    } else {
      false
    }
  }

  fun nextOnboardingStep() {
    if (_onboardingStep.value < 3) {
      _onboardingStep.value += 1
    } else {
      completeOnboarding()
    }
  }

  fun completeOnboarding() {
    prefs.edit().putBoolean("onboarding_done", true).apply()
    _currentScreen.value = AppScreen.HOME
  }

  // PREFERENCE SETTINGS
  fun setLanguage(lang: String) {
    _userLanguage.value = lang
    prefs.edit().putString("language", lang).apply()
  }

  fun setReciter(reciterId: String) {
    _reciter.value = reciterId
    prefs.edit().putString("reciter", reciterId).apply()
  }

  fun setAutoContinue(auto: Boolean) {
    _autoContinue.value = auto
    prefs.edit().putBoolean("auto_continue", auto).apply()
  }

  fun setHandsFreeMode(enabled: Boolean) {
    _handsFreeMode.value = enabled
    prefs.edit().putBoolean("hands_free", enabled).apply()
    if (enabled) {
      speechPipeline.startListening()
    } else {
      speechPipeline.stopListening()
    }
  }

  fun setSensitivity(sens: String) {
    _wakeSensitivity.value = sens
    prefs.edit().putString("sensitivity", sens).apply()
  }

  fun setPushToHold(hold: Boolean) {
    _pushToHold.value = hold
    prefs.edit().putBoolean("push_to_hold", hold).apply()
  }

  fun setListening(listening: Boolean) {
    _isListening.value = listening
    if (listening) {
      speechPipeline.startListening(oneShot = true)
    } else {
      speechPipeline.stopListening()
    }
  }

  override fun onCleared() {
    super.onCleared()
    audioPlayer.release()
    speechPipeline.release()
  }
}
erride fun onCleared() {
    super.onCleared()
    audioPlayer.release()
    speechPipeline.release()
  }
}
