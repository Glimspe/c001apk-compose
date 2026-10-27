package com.example.c001apk.compose.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.c001apk.compose.R
import com.example.c001apk.compose.ui.component.LauncherIconImage
import com.example.c001apk.compose.ui.component.rememberHapticClick
import com.example.c001apk.compose.util.LauncherIcon

@Composable
fun LauncherIconDialog(
    current: LauncherIcon,
    isApplying: Boolean,
    onConfirm: (LauncherIcon) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by rememberSaveable { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = { if (!isApplying) onDismiss() },
        title = { Text(stringResource(R.string.app_icon)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LauncherIcon.entries.forEach { icon ->
                        val isSelected = selected == icon
                        val background by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerLow,
                            label = "iconSelection",
                        )
                        val select = rememberHapticClick { selected = icon }
                        val shape = RoundedCornerShape(20.dp)
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(shape)
                                .selectable(
                                    selected = isSelected,
                                    enabled = !isApplying,
                                    role = Role.RadioButton,
                                    onClick = select,
                                ),
                            shape = shape,
                            color = background,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            ),
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                LauncherIconImage(Modifier.size(64.dp), icon)
                                Text(stringResource(icon.labelRes))
                                RadioButton(selected = isSelected, onClick = null)
                            }
                        }
                    }
                }
                Text(
                    stringResource(R.string.icon_theme_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            TextButton(enabled = !isApplying, onClick = { onConfirm(selected) }) {
                Text("应用")
            }
        },
        dismissButton = {
            TextButton(enabled = !isApplying, onClick = onDismiss) { Text("取消") }
        },
    )
}
