package com.example.c001apk.compose.ui.main

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.pressBack
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainDeepLinkTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val firstUrl = "https://www.coolapk1s.com/feed/73700591"
    private val secondUrl = "https://www.coolapk.com/feed/72251715"

    @Test
    fun coldLinkOpensOnceAndReturnsToHome() {
        ActivityScenario.launch<MainActivity>(intent(firstUrl)).use {
            assertDetail()
            pressBack()
            assertHome()
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 29)
    fun linksDeliveredBeforeFirstCompositionAreNotLost() {
        var delivered = false
        val listener = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityPostCreated(activity: Activity, savedInstanceState: Bundle?) {
                if (activity is MainActivity && !delivered) {
                    delivered = true
                    deliverLinks(activity, firstUrl, secondUrl)
                }
            }

            override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        }
        application.registerActivityLifecycleCallbacks(listener)
        try {
            ActivityScenario.launch<MainActivity>(intent()).use { scenario ->
                scenario.onActivity { assertTrue(delivered) }
                assertDetail()
                pressBack()
                assertDetail()
                pressBack()
                assertHome()
            }
        } finally {
            application.unregisterActivityLifecycleCallbacks(listener)
        }
    }

    @Test
    fun newIntentAfterStartupOpensTheLink() {
        ActivityScenario.launch<MainActivity>(intent()).use { scenario ->
            assertHome()
            scenario.onActivity { deliverLinks(it, firstUrl) }
            assertDetail()
            pressBack()
            assertHome()
        }
    }

    @Test
    fun recreationDoesNotOpenTheConsumedLinkAgain() {
        ActivityScenario.launch<MainActivity>(intent(firstUrl)).use { scenario ->
            assertDetail()
            scenario.recreate()
            assertDetail()
            pressBack()
            assertHome()
        }
    }

    private fun intent(url: String? = null) = Intent(application, MainActivity::class.java).apply {
        action = if (url == null) Intent.ACTION_MAIN else Intent.ACTION_VIEW
        data = url?.let(Uri::parse)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    private fun deliverLinks(activity: Activity, vararg urls: String) {
        val launchIntent = activity.intent
        try {
            urls.forEach { instrumentation.callActivityOnNewIntent(activity, intent(it)) }
            assertEquals(urls.last(), activity.intent.dataString)
        } finally {
            // ActivityScenario 按启动 Intent 追踪生命周期，校验更新后还原其标识。
            activity.intent = launchIntent
        }
    }

    private fun assertDetail() {
        compose.waitForIdle()
        compose.onNodeWithContentDescription("编辑首页板块").assertDoesNotExist()
    }

    private fun assertHome() {
        compose.waitUntil(timeoutMillis = 10_000) {
            compose.onAllNodesWithContentDescription("编辑首页板块").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription("编辑首页板块").assertExists()
    }
}
