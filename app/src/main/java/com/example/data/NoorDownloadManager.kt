package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

class NoorDownloadManager(
  private val context: Context,
  private val noorDao: NoorDao,
  private val scope: CoroutineScope
) {
  private val client = OkHttpClient()

  // State maps: surahNumber -> progress (0.0f to 1.0f)
  private val _downloadProgresses = MutableStateFlow<Map<Int, Float>>(emptyMap())
  val downloadProgresses = _downloadProgresses.asStateFlow()

  // Set of currently downloading surah numbers
  private val _activeDownloads = MutableStateFlow<Set<Int>>(emptySet())
  val activeDownloads = _activeDownloads.asStateFlow()

  private fun getGlobalVerseId(surahNumber: Int, verseNumber: Int): Int {
    var id = 0
    val surahsList = QuranData.surahs
    for (i in 1 until surahNumber) {
      id += surahsList[i - 1].verseCount
    }
    return id + verseNumber
  }

  fun startDownload(surahNumber: Int, reciterId: String = "ar.alafasy") {
    val surah = QuranData.surahs.firstOrNull { it.number == surahNumber } ?: return
    if (_activeDownloads.value.contains(surahNumber)) return

    _activeDownloads.value = _activeDownloads.value + surahNumber
    _downloadProgresses.value = _downloadProgresses.value + (surahNumber to 0.0f)

    scope.launch(Dispatchers.IO) {
      val dir = File(context.filesDir, "downloads/$surahNumber")
      if (!dir.exists()) {
        dir.mkdirs()
      }

      var successCount = 0
      var totalBytesDownloaded = 0L

      for (v in 1..surah.verseCount) {
        // Check if download was cancelled or stopped
        if (!_activeDownloads.value.contains(surahNumber)) {
          break
        }

        val file = File(dir, "$v.mp3")
        if (file.exists() && file.length() > 0) {
          successCount++
          totalBytesDownloaded += file.length()
          val progress = successCount.toFloat() / surah.verseCount
          updateProgress(surahNumber, progress)
          continue
        }

        val globalId = getGlobalVerseId(surahNumber, v)
        val url = "https://cdn.islamic.network/quran/audio/128/$reciterId/$globalId.mp3"

        val success = downloadFile(url, file)
        if (success) {
          successCount++
          totalBytesDownloaded += file.length()
        } else {
          // If a single verse fails, log and continue, or fail the surah
          Log.w("NoorDownloadManager", "Failed to download Surah $surahNumber, Verse $v")
        }

        val progress = successCount.toFloat() / surah.verseCount
        updateProgress(surahNumber, progress)
      }

      _activeDownloads.value = _activeDownloads.value - surahNumber

      if (successCount == surah.verseCount) {
        // Save to DB
        val entity = DownloadedSurahEntity(
          surahNumber = surahNumber,
          surahName = surah.englishName,
          verseCount = surah.verseCount,
          localPath = dir.absolutePath,
          totalBytes = totalBytesDownloaded,
          downloadedAt = System.currentTimeMillis()
        )
        noorDao.insertDownloadedSurah(entity)
        Log.i("NoorDownloadManager", "Successfully downloaded Surah $surahNumber")
      } else {
        // If not complete, we don't insert it to local downloaded DB
        _downloadProgresses.value = _downloadProgresses.value - surahNumber
        Log.e("NoorDownloadManager", "Incomplete download of Surah $surahNumber: downloaded $successCount of ${surah.verseCount}")
      }
    }
  }

  fun cancelDownload(surahNumber: Int) {
    _activeDownloads.value = _activeDownloads.value - surahNumber
    _downloadProgresses.value = _downloadProgresses.value - surahNumber
    scope.launch(Dispatchers.IO) {
      deleteLocalFiles(surahNumber)
    }
  }

  fun deleteDownload(surahNumber: Int) {
    scope.launch(Dispatchers.IO) {
      noorDao.deleteDownloadedSurah(surahNumber)
      deleteLocalFiles(surahNumber)
      _downloadProgresses.value = _downloadProgresses.value - surahNumber
    }
  }

  private fun deleteLocalFiles(surahNumber: Int) {
    val dir = File(context.filesDir, "downloads/$surahNumber")
    if (dir.exists()) {
      dir.deleteRecursively()
    }
  }

  private fun updateProgress(surahNumber: Int, progress: Float) {
    _downloadProgresses.value = _downloadProgresses.value + (surahNumber to progress)
  }

  private fun downloadFile(url: String, targetFile: File): Boolean {
    val request = Request.Builder().url(url).build()
    return try {
      client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) return false
        val body = response.body ?: return false
        
        // Ensure parent exists
        targetFile.parentFile?.mkdirs()
        
        FileOutputStream(targetFile).use { output ->
          body.byteStream().use { input ->
            input.copyTo(output)
          }
        }
        true
      }
    } catch (e: Exception) {
      Log.e("NoorDownloadManager", "Error downloading $url", e)
      if (targetFile.exists()) {
        targetFile.delete() // clean up partial file
      }
      false
    }
  }
}
