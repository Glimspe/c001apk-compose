package com.example.c001apk.compose.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.navigation.NavHostController
import androidx.navigation.compose.LocalOwnersProvider
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.c001apk.compose.ui.component.TransitionDurations

@Composable
internal fun RetainedMainScreen(
    navController: NavHostController,
    content: @Composable (isActive: Boolean) -> Unit,
) {
    val currentEntry by navController.currentBackStackEntryAsState()
    val mainEntry = remember(currentEntry) {
        runCatching { navController.getBackStackEntry(Router.MAIN.name) }.getOrNull()
    } ?: return
    val isActive = currentEntry?.destination?.route == Router.MAIN.name
    val visibility = animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = TransitionDurations.Normal.asTween(),
        label = "main-screen-visibility",
    )
    val stateHolder = rememberSaveableStateHolder()
    val windowInfo = LocalWindowInfo.current
    var keepContent by remember { mutableStateOf(true) }
    LaunchedEffect(isActive) {
        // 从弹窗或后台跳转时释放旧内容，让原页面的独立弹窗正常关闭。
        keepContent = isActive || windowInfo.isWindowFocused
    }
    if (!isActive && !keepContent) return

    // 保留主页面的组合和绘制缓存，避免返回首帧重建整页列表。
    mainEntry.LocalOwnersProvider(stateHolder) {
        Box(
            Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        if (isActive || visibility.value > 0f) placeable.placeRelative(0, 0)
                    }
                }
                .graphicsLayer {
                    alpha = visibility.value
                    translationX = -(1f - visibility.value) * size.width * 0.15f
                }
                .then(if (isActive) Modifier else Modifier.clearAndSetSemantics {}),
        ) {
            content(isActive)
        }
    }
}
