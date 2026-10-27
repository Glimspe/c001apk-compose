package com.example.c001apk.compose.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.c001apk.compose.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class LauncherIcon(
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int,
    private val alias: String,
    val enabledByDefault: Boolean,
) {
    REVIVED(R.string.icon_revived, R.mipmap.ic_launcher_revived, "RevivedLauncher", true),
    CLASSIC(R.string.icon_classic, R.mipmap.ic_launcher, "ClassicLauncher", false);

    fun component(context: Context) =
        ComponentName(context.packageName, "${context.packageName}.$alias")
}

@Singleton
class LauncherIconManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val packageManager = context.packageManager

    // PackageManager 保存选择，避免应用偏好与桌面入口的状态不一致。
    val current: LauncherIcon
        get() = LauncherIcon.entries.firstOrNull(::isEnabled) ?: LauncherIcon.REVIVED

    fun select(icon: LauncherIcon) {
        if (LauncherIcon.entries.all { isEnabled(it) == (it == icon) }) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.setComponentEnabledSettings(LauncherIcon.entries.map {
                PackageManager.ComponentEnabledSetting(
                    it.component(context),
                    if (it == icon) PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            })
        } else {
            // 旧系统先启用新入口，整个切换过程始终有可用的桌面入口。
            packageManager.setComponentEnabledSetting(
                icon.component(context),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            LauncherIcon.entries.filter { it != icon }.forEach {
                packageManager.setComponentEnabledSetting(
                    it.component(context),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }

    private fun isEnabled(icon: LauncherIcon): Boolean =
        when (packageManager.getComponentEnabledSetting(icon.component(context))) {
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> icon.enabledByDefault
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            else -> false
        }
}
