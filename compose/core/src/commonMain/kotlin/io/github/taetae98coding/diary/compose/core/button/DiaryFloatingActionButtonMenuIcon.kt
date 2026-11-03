package io.github.taetae98coding.diary.compose.core.button

import androidx.compose.ui.graphics.vector.ImageVector
import io.github.taetae98coding.diary.compose.core.icon.Checklist
import io.github.taetae98coding.diary.compose.core.icon.Deselect
import io.github.taetae98coding.diary.compose.core.icon.DiaryIcons
import io.github.taetae98coding.diary.compose.core.icon.SelectAll
import io.github.taetae98coding.diary.compose.core.icon.Weekend

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
