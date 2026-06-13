package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// 1. ENTITIES

@Entity(tableName = "sessions")
data class SessionEntity(
  @PrimaryKey val id: String,
  val startedAt: Long,
  val endedAt: Long? = null,
  val totalDuration: Int = 0
)

@Entity(
  tableName = "exchanges",
  foreignKeys = [
    ForeignKey(
      entity = SessionEntity::class,
      parentColumns = ["id"],
      childColumns = ["sessionId"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index(value = ["sessionId"])]
)
data class ExchangeEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val sessionId: String,
  val userText: String,
  val noorText: String,
  val intentType: String?,
  val surahNumber: Int?,
  val verseNumber: Int?,
  val timestamp: Long
)

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
  @PrimaryKey val id: String, // surahNumber_verseNumber
  val surahNumber: Int,
  val surahName: String,
  val verseNumber: Int,
  val juzNumber: Int?,
  val savedAt: Long
)

@Entity(tableName = "listening_history")
data class ListeningHistoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val surahNumber: Int,
  val surahName: String,
  val startVerse: Int?,
  val endVerse: Int?,
  val durationSeconds: Int,
  val playedAt: Long,
  val completed: Boolean
)

@Entity(tableName = "downloaded_surahs")
data class DownloadedSurahEntity(
  @PrimaryKey val surahNumber: Int,
  val surahName: String,
  val verseCount: Int,
  val localPath: String,
  val totalBytes: Long,
  val downloadedAt: Long
)

// 2. DAOS

@Dao
interface NoorDao {

  // Sessions and Exchanges
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: SessionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertExchange(exchange: ExchangeEntity)

  @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
  fun getAllSessionsFlow(): Flow<List<SessionEntity>>

  @Query("SELECT * FROM exchanges WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getExchangesForSessionFlow(sessionId: String): Flow<List<ExchangeEntity>>

  @Query("SELECT * FROM exchanges ORDER BY timestamp DESC")
  fun getAllExchangesFlow(): Flow<List<ExchangeEntity>>

  // Bookmarks
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookmark(bookmark: BookmarkEntity)

  @Query("DELETE FROM bookmarks WHERE id = :id")
  suspend fun deleteBookmarkById(id: String)

  @Query("SELECT * FROM bookmarks ORDER BY savedAt DESC")
  fun getAllBookmarksFlow(): Flow<List<BookmarkEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE id = :id)")
  suspend fun isBookmarked(id: String): Boolean

  // Listening History
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertListeningHistory(history: ListeningHistoryEntity)

  @Query("SELECT * FROM listening_history ORDER BY playedAt DESC")
  fun getListeningHistoryFlow(): Flow<List<ListeningHistoryEntity>>

  // Downloaded Surahs
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDownloadedSurah(download: DownloadedSurahEntity)

  @Query("DELETE FROM downloaded_surahs WHERE surahNumber = :surahNumber")
  suspend fun deleteDownloadedSurah(surahNumber: Int)

  @Query("SELECT * FROM downloaded_surahs ORDER BY surahNumber ASC")
  fun getAllDownloadedSurahsFlow(): Flow<List<DownloadedSurahEntity>>

  @Query("SELECT EXISTS(SELECT 1 FROM downloaded_surahs WHERE surahNumber = :surahNumber)")
  suspend fun isSurahDownloaded(surahNumber: Int): Boolean
}

// 3. DATABASE

@Database(
  entities = [
    SessionEntity::class,
    ExchangeEntity::class,
    BookmarkEntity::class,
    ListeningHistoryEntity::class,
    DownloadedSurahEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class NoorDatabase : RoomDatabase() {
  abstract fun noorDao(): NoorDao

  companion object {
    @Volatile
    private var INSTANCE: NoorDatabase? = null

    fun getDatabase(context: Context): NoorDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          NoorDatabase::class.java,
          "noor_database"
        )
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
