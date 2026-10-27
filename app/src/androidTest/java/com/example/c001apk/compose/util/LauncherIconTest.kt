package com.example.c001apk.compose.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.core.content.res.ResourcesCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.main.MainActivity
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherIconTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val packageManager = context.packageManager
    private val manager = LauncherIconManager(context)
    private lateinit var originalStates: Map<LauncherIcon, Int>
    private lateinit var originalIcon: LauncherIcon

    @Before
    fun saveSelection() {
        originalIcon = manager.current
        originalStates = LauncherIcon.entries.associateWith {
            packageManager.getComponentEnabledSetting(it.component(context))
        }
    }

    @After
    fun restoreSelection() {
        manager.select(originalIcon)
        originalStates.forEach { (icon, state) ->
            packageManager.setComponentEnabledSetting(
                icon.component(context), state, PackageManager.DONT_KILL_APP,
            )
        }
    }

    @Test
    fun freshInstallDefaultsToRevived() {
        LauncherIcon.entries.forEach {
            packageManager.setComponentEnabledSetting(
                it.component(context),
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT,
                PackageManager.DONT_KILL_APP,
            )
        }
        assertEquals(LauncherIcon.REVIVED, manager.current)
        assertLauncher(LauncherIcon.REVIVED)
    }

    @Test
    fun repeatedSwitchesKeepOneLauncherAndWorkingDeepLinks() {
        val main = ComponentName(context, MainActivity::class.java)
        val originalMainState = packageManager.getComponentEnabledSetting(main)
        listOf(LauncherIcon.CLASSIC, LauncherIcon.REVIVED, LauncherIcon.CLASSIC).forEach { icon ->
            manager.select(icon)
            assertLauncher(icon)
            assertEquals(icon, LauncherIconManager(context).current)
            assertEquals(originalMainState, packageManager.getComponentEnabledSetting(main))
            val link = Intent(Intent.ACTION_VIEW, Uri.parse("coolmarket://feed/73700591"))
                .setPackage(context.packageName)
            val resolved = packageManager.resolveActivity(link, PackageManager.MATCH_DEFAULT_ONLY)
            assertEquals(MainActivity::class.java.name, resolved?.activityInfo?.name)
        }
    }

    @Test
    fun switchingFromLauncherKeepsTheRunningActivity() {
        manager.select(LauncherIcon.REVIVED)
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)!!
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            manager.select(LauncherIcon.CLASSIC)
            scenario.onActivity {
                assertFalse(it.isFinishing)
                assertFalse(it.isDestroyed)
            }
            assertLauncher(LauncherIcon.CLASSIC)
            manager.select(LauncherIcon.REVIVED)
            scenario.onActivity { assertFalse(it.isFinishing) }
            assertLauncher(LauncherIcon.REVIVED)
        }
    }

    @Test
    @SdkSuppress(minSdkVersion = 26)
    fun bothIconsUseExistingColorsInLightAndDarkModes() {
        for (night in listOf(Configuration.UI_MODE_NIGHT_NO, Configuration.UI_MODE_NIGHT_YES)) {
            val config = Configuration(context.resources.configuration).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or night
            }
            val themed = context.createConfigurationContext(config)
            for (icon in LauncherIcon.entries) {
                val drawable = ResourcesCompat.getDrawable(themed.resources, icon.iconRes, null)
                    as AdaptiveIconDrawable
                assertEquals(
                    themed.getColor(R.color.ic_launcher_background),
                    (drawable.background as ColorDrawable).color,
                )
                val bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888)
                drawable.foreground.setBounds(0, 0, 108, 108)
                drawable.foreground.draw(Canvas(bitmap))
                val pixels = IntArray(108 * 108)
                bitmap.getPixels(pixels, 0, 108, 0, 0, 108, 108)
                val foreground = pixels.first { it ushr 24 == 255 }
                assertEquals(themed.getColor(R.color.ic_launcher_foreground), foreground)
                assertNotEquals(foreground, (drawable.background as ColorDrawable).color)
                bitmap.recycle()
                if (Build.VERSION.SDK_INT >= 33) assertNotNull(drawable.monochrome)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun assertLauncher(icon: LauncherIcon) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setPackage(context.packageName)
        val launchers = packageManager.queryIntentActivities(intent, 0)
        assertEquals(listOf(icon.component(context).className), launchers.map { it.activityInfo.name })
        assertEquals(
            icon.component(context),
            packageManager.getLaunchIntentForPackage(context.packageName)?.component,
        )
        assertEquals(MainActivity::class.java.name, launchers.single().activityInfo.targetActivity)
        val roundIcon = if (icon == LauncherIcon.REVIVED) R.mipmap.ic_launcher_revived_round
        else R.mipmap.ic_launcher_round
        assertTrue(launchers.single().activityInfo.icon in setOf(icon.iconRes, roundIcon))
    }
}
