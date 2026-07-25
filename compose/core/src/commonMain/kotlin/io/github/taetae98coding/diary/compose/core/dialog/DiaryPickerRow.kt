package io.github.taetae98coding.diary.compose.core.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.taetae98coding.diary.compose.core.color.DiaryColorIndicator
import io.github.taetae98coding.diary.compose.core.preview.ComponentPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme

@Composable
public fun DiaryPickerRow(
    onSelectedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    color: Color? = null,
    isSelected: Boolean = false,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .clip(CircleShape)
                .toggleable(
                    value = isSelected,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = onSelectedChange,
                ).minimumInteractiveComponentSize()
                .padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = null,
            enabled = enabled,
        )
        color?.let { value -> DiaryColorIndicator(color = value) }
        Column(modifier = Modifier.weight(1F)) {
            Text(
                text = label.orLineReservation(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            description?.let { value ->
                Text(
                    text = value.orLineReservation(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = DiaryTheme.typography.bodySmall,
                )
            }
        }
        trailing?.invoke()
    }
}

// 빈 문구는 한 줄 높이를 차지하지 않아 자리 표시 항목이 준비된 항목보다 낮아지므로, 보이지 않는 문자로 한 줄을 남긴다.
private const val LINE_RESERVATION_TEXT: String = "\u200B"

private fun String.orLineReservation(): String = ifEmpty { LINE_RESERVATION_TEXT }

@ComponentPreview
@Composable
private fun DiaryPickerRowPreview() {
    DiaryTheme {
        Surface {
            DiaryPickerRow(
                onSelectedChange = {},
                label = "업무",
                color = Color(color = 0xFF3A7BD5.toInt()),
                isSelected = true,
            )
        }
    }
}
