package com.example.c001apk.compose.ui.home.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.util.isScrollingUp
import kotlinx.coroutines.launch

@Composable
internal fun HomeFeedActions(
    listState: LazyListState,
    isActive: Boolean,
    canPublish: Boolean,
    onPublish: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val scrollingUp = listState.isScrollingUp()
    val canScrollToTop = listState.canScrollBackward
    var returningToTop by remember { mutableStateOf(false) }
    val buttonColor = MaterialTheme.colorScheme.primaryContainer
    val iconColor = MaterialTheme.colorScheme.onPrimaryContainer

    AnimatedVisibility(
        visible = isActive && scrollingUp && (canPublish || canScrollToTop),
        enter = fadeIn(tween(180)) + slideInVertically(
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 500f),
            initialOffsetY = { it },
        ),
        exit = fadeOut(tween(140)) + slideOutVertically(
            animationSpec = tween(200, easing = FastOutLinearInEasing),
            targetOffsetY = { it },
        ),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AnimatedVisibility(
                visible = canScrollToTop,
                enter = fadeIn(tween(160)) + scaleIn(initialScale = 0.8f) +
                    expandVertically(expandFrom = Alignment.Bottom),
                exit = fadeOut(tween(120)) + scaleOut(targetScale = 0.8f) +
                    shrinkVertically(shrinkTowards = Alignment.Bottom),
            ) {
                FloatingActionButton(
                    modifier = Modifier.padding(bottom = if (canPublish) 12.dp else 0.dp),
                    containerColor = buttonColor,
                    contentColor = iconColor,
                    onClick = rememberHapticClick {
                        if (!returningToTop) {
                            returningToTop = true
                            scope.launch {
                                try {
                                    listState.animateScrollToItem(0)
                                } finally {
                                    returningToTop = false
                                }
                            }
                        }
                    },
                ) {
                    Icon(Icons.Rounded.ArrowUpward, contentDescription = "回到顶部")
                }
            }
            if (canPublish) {
                FloatingActionButton(
                    containerColor = buttonColor,
                    contentColor = iconColor,
                    onClick = rememberHapticClick(onClick = onPublish),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "发布动态")
                }
            }
        }
    }
}
