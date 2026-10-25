package com.example.c001apk.compose.logic.backup

import com.example.c001apk.compose.logic.model.FeedEntity
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import java.io.Reader
import java.io.Writer

object FavoriteBackup {
    private const val FORMAT = "c001apk-compose.favorites"
    private const val VERSION = 1
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun write(favorites: List<FeedEntity>, writer: Writer) {
        val entries = JsonArray()
        favorites.forEach { favorite ->
            entries.add(JsonObject().apply {
                addProperty("id", favorite.id)
                addProperty("uid", favorite.uid)
                addProperty("uname", favorite.uname)
                addProperty("avatar", favorite.avatar)
                addProperty("device", favorite.device)
                addProperty("message", favorite.message)
                addProperty("pubDate", favorite.pubDate)
                addProperty("time", favorite.time)
            })
        }
        val backup = JsonObject().apply {
            addProperty("format", FORMAT)
            addProperty("version", VERSION)
            add("favorites", entries)
        }
        gson.toJson(backup, writer)
    }

    fun read(reader: Reader): List<FeedEntity> {
        val root = try {
            JsonParser.parseReader(reader)
        } catch (e: JsonParseException) {
            throw InvalidFavoriteBackupException("备份文件损坏或格式不正确")
        }
        if (!root.isJsonObject) {
            throw InvalidFavoriteBackupException("这不是本地收藏备份文件")
        }
        val backup = root.asJsonObject
        if (backup.string("format") != FORMAT) {
            throw InvalidFavoriteBackupException("这不是本地收藏备份文件")
        }
        val version = backup.get("version")
        if (version == null || !version.isJsonPrimitive ||
            !version.asJsonPrimitive.isNumber || version.asString != VERSION.toString()
        ) {
            throw InvalidFavoriteBackupException("不支持此备份版本，请升级应用后重试")
        }
        val entries = backup.get("favorites")
        if (entries == null || !entries.isJsonArray) {
            throw InvalidFavoriteBackupException("备份文件缺少收藏列表")
        }

        // 完整校验通过后才交给数据库一次性合并。
        return entries.asJsonArray.mapIndexed { index, entry ->
            fun invalid(): Nothing = throw InvalidFavoriteBackupException("第 ${index + 1} 条收藏数据不完整")
            if (!entry.isJsonObject) invalid()
            val item = entry.asJsonObject
            fun field(name: String): String = item.string(name) ?: invalid()
            val id = field("id").takeIf { it.isNotBlank() } ?: invalid()
            val time = field("time").takeIf { (it.toLongOrNull() ?: -1L) >= 0L } ?: invalid()
            FeedEntity(
                id = id,
                uid = field("uid"),
                uname = field("uname"),
                avatar = field("avatar"),
                device = field("device"),
                message = field("message"),
                pubDate = field("pubDate"),
                time = time,
            )
        }
    }

    private fun JsonObject.string(name: String): String? = get(name)
        ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
        ?.asString
}

class InvalidFavoriteBackupException(message: String) : Exception(message)
