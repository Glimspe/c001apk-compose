package com.example.c001apk.compose.ui.main

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import kotlin.math.roundToInt

@Composable
internal fun FeedSplitLayout(
    showDetail: Boolean,
    detailContent: @Composable () -> Unit,
    mainContent: @Composable () -> Unit,
) {
    val transition = updateTransition(showDetail, label = "feed-split")
    val progress = transition.animateFloat(
        transitionSpec = { spring(dampingRatio = 1f, stiffness = 350f) },
        label = "pane-progress",
    ) { visible -> if (visible) 1f else 0f }
    val keepDetail = transition.currentState || transition.targetState

    Layout(
        modifier = Modifier.fillMaxSize().clipToBounds(),
        content = {
            Box(modifier = Modifier.fillMaxSize()) { mainContent() }
            if (keepDetail) {
                Box(modifier = Modifier.fillMaxSize()) { detailContent() }
            }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val detailWidth = width / 2
        val mainWidth = width - (detailWidth * progress.value.coerceIn(0f, 1f)).roundToInt()
        val main = measurables[0].measure(
            constraints.copy(minWidth = mainWidth, maxWidth = mainWidth)
        )
        // 详情保持半屏宽度滑入，避免动画中反复压缩文本与图片。
        val detail = measurables.getOrNull(1)?.measure(
            constraints.copy(minWidth = detailWidth, maxWidth = detailWidth)
        )
        layout(width, constraints.maxHeight) {
            main.placeRelative(0, 0)
            detail?.placeRelative(mainWidth, 0)
        }
    }
}
