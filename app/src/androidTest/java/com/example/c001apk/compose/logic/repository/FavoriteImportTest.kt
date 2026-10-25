package com.example.c001apk.compose.logic.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.logic.backup.FavoriteBackup
import com.example.c001apk.compose.logic.backup.InvalidFavoriteBackupException
import com.example.c001apk.compose.logic.database.HistoryFavoriteDatabase
import com.example.c001apk.compose.logic.model.FeedEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.StringReader

@RunWith(AndroidJUnit4::class)
class FavoriteImportTest {
    private lateinit var favorites: HistoryFavoriteDatabase
    private lateinit var history: HistoryFavoriteDatabase
    private lateinit var repo: HistoryFavoriteRepo

    @Before
    fun setUp() {
        fun database() = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), HistoryFavoriteDatabase::class.java
        ).build()
        favorites = database()
        history = database()
        repo = HistoryFavoriteRepo(history.historyFavoriteDao(), favorites.historyFavoriteDao())
    }

    @After
    fun tearDown() {
        favorites.close()
        history.close()
    }

    @Test
    fun mergesWithoutOverwritingExistingFavoritesOrTouchingHistory() = runBlocking {
        val original = entry("1", "本机内容", "1792890000000")
        val newEntry = entry("2", "备份内容", "1792891000000")
        val historyEntry = entry("3", "浏览历史", "1792892000000")
        repo.insertFavorite(original)
        repo.insertHistory(historyEntry)

        val imported = repo.importFavorites(listOf(original.copy(message = "旧备份"), newEntry, newEntry))

        assertEquals(1, imported)
        assertEquals(listOf(newEntry, original), repo.loadAllFavorites())
        assertEquals(listOf(historyEntry), history.historyFavoriteDao().loadAllList())
        assertEquals(0, repo.importFavorites(listOf(original, newEntry)))
        assertEquals(listOf(newEntry, original), repo.loadAllFavorites())
    }

    @Test
    fun invalidBackupLeavesExistingFavoritesUntouched() = runBlocking {
        val original = entry("1", "本机内容", "1792890000000")
        repo.insertFavorite(original)
        assertThrows(InvalidFavoriteBackupException::class.java) {
            runBlocking {
                val parsed = FavoriteBackup.read(StringReader("""{"format":"c001apk-compose.favorites","version":1,"favorites":[null]}"""))
                repo.importFavorites(parsed)
            }
        }
        assertEquals(listOf(original), repo.loadAllFavorites())
    }

    private fun entry(id: String, message: String, time: String) = FeedEntity(
        id = id, uid = "100", uname = "用户", avatar = "", device = "",
        message = message, pubDate = "1792890000", time = time,
    )
}
