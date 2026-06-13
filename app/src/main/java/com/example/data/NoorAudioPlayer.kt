package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

interface PlaybackListener {
  fun onPlayStateChanged(isPlaying: Boolean)
  fun onVerseChanged(surah: Int, verse: Int)
}

class NoorAudioPlayer(private val context: Context) {
  private var mediaPlayer: MediaPlayer? = null
  
  private val _isPlaying = MutableStateFlow(false)
  val isPlaying = _isPlaying.asStateFlow()

  private val _currentSurah = MutableStateFlow<Int?>(null)
  val currentSurah = _currentSurah.asStateFlow()

  private val _currentVerse = MutableStateFlow<Int?>(null)
  val currentVerse = _currentVerse.asStateFlow()

  private val _isBuffering = MutableStateFlow(false)
  val isBuffering = _isBuffering.asStateFlow()

  var onVerseCompleted: (() -> Unit)? = null

  // Function to calculate global verse ID
  fun getGlobalVerseId(surahNumber: Int, verseNumber: Int): Int {
    var id = 0
    val surahsList = QuranData.surahs
    for (i in 1 until surahNumber) {
      id += surahsList[i - 1].verseCount
    }
    return id + verseNumber
  }

  fun playVerse(surah: Int, verse: Int, reciterId: String = "ar.alafasy") {
    stop()
    _isBuffering.value = true
    _currentSurah.value = surah
    _currentVerse.value = verse

    val localFile = File(context.filesDir, "downloads/$surah/$verse.mp3")
    val dataSourceUrl = if (localFile.exists()) {
      localFile.absolutePath
    } else {
      val globalId = getGlobalVerseId(surah, verse)
      "https://cdn.islamic.network/quran/audio/128/$reciterId/$globalId.mp3"
    }

    try {
      mediaPlayer = MediaPlayer().apply {
        setDataSource(dataSourceUrl)
        setOnPreparedListener { mp ->
          _isBuffering.value = false
          mp.start()
          _isPlaying.value = true
        }
        setOnCompletionListener {
          _isPlaying.value = false
          onVerseCompleted?.invoke()
        }
        setOnErrorListener { _, what, extra ->
          Log.e("NoorAudioPlayer", "MediaPlayer Error: what=$what, extra=$extra")
          _isBuffering.value = false
          _isPlaying.value = false
          true
        }
        prepareAsync()
      }
    } catch (e: Exception) {
      Log.e("NoorAudioPlayer", "Error initializing MediaPlayer", e)
      _isBuffering.value = false
      _isPlaying.value = false
    }
  }

  fun play() {
    mediaPlayer?.let {
      if (!it.isPlaying) {
        it.start()
        _isPlaying.value = true
      }
    }
  }

  fun pause() {
    mediaPlayer?.let {
      if (it.isPlaying) {
        it.pause()
        _isPlaying.value = false
      }
    }
  }

  fun stop() {
    mediaPlayer?.let {
      try {
        if (it.isPlaying) {
          it.stop()
        }
        it.release()
      } catch (e: Exception) {
        Log.e("NoorAudioPlayer", "Error releasing player", e)
      }
    }
    mediaPlayer = null
    _isPlaying.value = false
    _isBuffering.value = false
  }

  fun togglePlayPause() {
    mediaPlayer?.let {
      if (it.isPlaying) {
        pause()
      } else {
        play()
      }
    }
  }

  fun seekForward10s() {
    mediaPlayer?.let {
      val currentPos = it.currentPosition
      val duration = it.duration
      if (duration > 0) {
        val newPos = (currentPos + 10000).coerceAtMost(duration)
        it.seekTo(newPos)
      }
    }
  }

  fun seekBackward10s() {
    mediaPlayer?.let {
      val currentPos = it.currentPosition
      val newPos = (currentPos - 10000).coerceAtLeast(0)
      it.seekTo(newPos)
    }
  }

  fun release() {
    stop()
  }
}
