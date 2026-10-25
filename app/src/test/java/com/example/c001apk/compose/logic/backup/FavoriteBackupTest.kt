package com.example.c001apk.compose.logic.backup

import com.example.c001apk.compose.logic.model.FeedEntity
import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter

class FavoriteBackupTest {
    private val favorite = FeedEntity(
        id = "12345",
        uid = "67890",
        uname = "测试用户",
        avatar = "https://example.com/avatar.jpg",
        device = "测试手机",
        message = "收藏内容\n<a href=\"/t/Android\">#Android#</a> 😀",
        pubDate = "1792890000",
        time = "1792891234567",
    )

    @Test
    fun roundTripPreservesContentAndOriginalFavoriteTime() {
        val favorites = listOf(favorite, favorite.copy(id = "23456", time = "1792890000000"))
        val json = encode(favorites)
        val document = JsonParser.parseString(json).asJsonObject
        assertEquals("c001apk-compose.favorites", document.get("format").asString)
        assertEquals(1, document.get("version").asInt)
        assertEquals(favorites, FavoriteBackup.read(StringReader(json)))
    }

    @Test
    fun supportsEmptyBackups() {
        assertEquals(emptyList<FeedEntity>(), FavoriteBackup.read(StringReader(encode(emptyList()))))
    }

    @Test
    fun rejectsUnrelatedJsonAndUnsupportedVersions() {
        for (json in listOf(
            "[]", "null", "[\"12345\"]", "{}",
            """{"format":"blacklist","version":1,"favorites":[]}""",
            """{"format":"c001apk-compose.favorites","version":2,"favorites":[]}""",
            """{"format":"c001apk-compose.favorites","version":1.5,"favorites":[]}""",
            """{"format":"c001apk-compose.favorites","version":1}""",
            """{"format":"c001apk-compose.favorites","version":1,"favorites":null}""",
        )) {
            assertThrows(InvalidFavoriteBackupException::class.java) {
                FavoriteBackup.read(StringReader(json))
            }
        }
    }

    @Test
    fun rejectsTruncatedFiles() {
        val json = encode(listOf(favorite)).dropLast(3)
        assertThrows(InvalidFavoriteBackupException::class.java) {
            FavoriteBackup.read(StringReader(json))
        }
    }

    @Test
    fun rejectsIncompleteRecordsBeforeReturningAnyFavorites() {
        for (field in listOf("id", "uid", "uname", "avatar", "device", "message", "pubDate", "time")) {
            val document = JsonParser.parseString(encode(listOf(favorite, favorite.copy(id = "2")))).asJsonObject
            document.getAsJsonArray("favorites")[1].asJsonObject.remove(field)
            assertThrows(InvalidFavoriteBackupException::class.java) {
                FavoriteBackup.read(StringReader(document.toString()))
            }
        }
    }

    @Test
    fun rejectsNullOrWrongTypesInRecords() {
        for (replacement in listOf("null", "[]", "123", "{\"id\":null}")) {
            val document = JsonParser.parseString(encode(listOf(favorite))).asJsonObject
            document.getAsJsonArray("favorites").set(0, JsonParser.parseString(replacement))
            assertThrows(InvalidFavoriteBackupException::class.java) {
                FavoriteBackup.read(StringReader(document.toString()))
            }
        }
        val document = JsonParser.parseString(encode(listOf(favorite))).asJsonObject
        document.getAsJsonArray("favorites")[0].asJsonObject.addProperty("message", 123)
        assertThrows(InvalidFavoriteBackupException::class.java) {
            FavoriteBackup.read(StringReader(document.toString()))
        }
    }

    @Test
    fun rejectsEmptyIdsAndInvalidFavoriteTimes() {
        for (item in listOf(
            favorite.copy(id = " "),
            favorite.copy(time = "invalid"),
            favorite.copy(time = "-1"),
        )) {
            assertThrows(InvalidFavoriteBackupException::class.java) {
                FavoriteBackup.read(StringReader(encode(listOf(item))))
            }
        }
    }

    @Test
    fun preservesDuplicatesForDatabaseMergeAndIgnoresExtraMetadata() {
        val favorites = listOf(favorite, favorite)
        val document = JsonParser.parseString(encode(favorites)).asJsonObject
        document.addProperty("exportedAt", "2026-10-25")
        document.getAsJsonArray("favorites")[0].asJsonObject.addProperty("extra", "metadata")
        assertEquals(favorites, FavoriteBackup.read(StringReader(document.toString())))
    }

    private fun encode(favorites: List<FeedEntity>) = StringWriter().also {
        FavoriteBackup.write(favorites, it)
    }.toString()
}
