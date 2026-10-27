package com.example.c001apk.compose.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.c001apk.compose.ui.main.MainActivity
import com.example.c001apk.compose.util.LauncherIcon
import com.example.c001apk.compose.util.LauncherIconManager
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherIconSettingsTest {
    @get:Rule
    val compose = createEmptyComposeRule()

    @Test
    fun pickerCanCancelApplyAndShowTheSavedSelection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = LauncherIconManager(context)
        val original = manager.current
        try {
            manager.select(LauncherIcon.REVIVED)
            val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)!!
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            ActivityScenario.launch<MainActivity>(intent).use {
                compose.onAllNodesWithText("设置")[0].performClick()
                compose.onNodeWithText("应用图标").performClick()
                option("Revived").assertIsSelected()
                option("原版").performClick()
                compose.onNodeWithText("取消").performClick()
                assertEquals(LauncherIcon.REVIVED, manager.current)

                compose.onNodeWithText("应用图标").performClick()
                option("原版").performClick()
                compose.onNodeWithText("应用").performClick()
                compose.waitUntil(5_000) {
                    manager.current == LauncherIcon.CLASSIC &&
                        compose.onAllNodesWithText("应用").fetchSemanticsNodes().isEmpty()
                }
                compose.waitForIdle()

                compose.onNodeWithText("应用图标").performClick()
                option("原版").assertIsSelected()
                option("Revived").performClick()
                compose.onNodeWithText("应用").performClick()
                compose.waitUntil(5_000) { manager.current == LauncherIcon.REVIVED }
            }
        } finally {
            manager.select(original)
        }
    }

    private fun option(label: String) =
        compose.onNode(hasText(label) and hasAnyAncestor(isDialog()))
}
