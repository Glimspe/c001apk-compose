package com.example.c001apk.compose.ui.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import com.example.c001apk.compose.util.LauncherIcon
import com.example.c001apk.compose.util.LauncherIconManager
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun LauncherIconImage(modifier: Modifier = Modifier, icon: LauncherIcon? = null) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val selected = icon ?: remember(context) { LauncherIconManager(context).current }
    val drawable = remember(selected, context, configuration) {
        ResourcesCompat.getDrawable(context.resources, selected.iconRes, context.theme)
    }
    Image(
        painter = rememberDrawablePainter(drawable),
        contentDescription = null,
        modifier = modifier,
    )
}
