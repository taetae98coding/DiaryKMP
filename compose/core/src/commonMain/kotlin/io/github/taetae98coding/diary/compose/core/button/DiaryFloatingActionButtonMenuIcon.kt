package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.taetae98coding.diary.compose.icon.Checklist
import io.github.taetae98coding.diary.compose.icon.Deselect
import io.github.taetae98coding.diary.compose.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.icon.SelectAll
import io.github.taetae98coding.diary.compose.icon.Weekend

public enum class DiaryFloatingActionButtonMenuIcon {
    CHECKLIST,
    DAY_OFF,
    DESELECT,
    SELECT_ALL,
}

internal val DiaryFloatingActionButtonMenuIcon.imageVector: ImageVector
    get() =
        when (this) {
            DiaryFloatingActionButtonMenuIcon.CHECKLIST -> DiaryIcons.Checklist
            DiaryFloatingActionButtonMenuIcon.DAY_OFF -> DiaryIcons.Weekend
            DiaryFloatingActionButtonMenuIcon.DESELECT -> DiaryIcons.Deselect
            DiaryFloatingActionButtonMenuIcon.SELECT_ALL -> DiaryIcons.SelectAll
        }
