package com.example.c001apk.compose.util

import com.example.c001apk.compose.constant.Constants.MODE
import okhttp3.Request

private const val REPLY_VERSION_NAME = "16.6.4"
private const val REPLY_VERSION_CODE = 2609291L
private const val REPLY_API_VERSION = 16
private val coolMarketAgent = Regex("""\+CoolMarket/\S+""")

internal fun Request.withReplyApiCompatibility(): Request {
    if (url.host !in setOf("api.coolapk.com", "api2.coolapk.com") ||
        url.encodedPath != "/v6/feed/replyList"
    ) {
        return this
    }

    // 评论接口已停用旧版本参数；全局升级 API 会改变话题页的数据结构。
    return newBuilder().apply {
        if ((header("X-App-Code")?.toLongOrNull() ?: 0L) < REPLY_VERSION_CODE) {
            header("X-App-Version", REPLY_VERSION_NAME)
            header("X-App-Code", REPLY_VERSION_CODE.toString())
            header("X-App-Supported", REPLY_VERSION_CODE.toString())

            val agent = header("User-Agent").orEmpty()
            val appAgent = "+CoolMarket/$REPLY_VERSION_NAME-$REPLY_VERSION_CODE-$MODE"
            header(
                "User-Agent",
                if (coolMarketAgent.containsMatchIn(agent)) {
                    coolMarketAgent.replace(agent, appAgent)
                } else {
                    "$agent $appAgent".trim()
                }
            )
        }
        if ((header("X-Api-Version")?.toIntOrNull() ?: 0) < REPLY_API_VERSION) {
            header("X-Api-Version", REPLY_API_VERSION.toString())
        }
    }.build()
}
