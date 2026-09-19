@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.theme

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.fillWidth
import androidx.compose.ui.unit.dp

private const val DIMMED_ALPHA = 0.38F
private val BottomSheetRowMinHeight = 56.dp

// 이름과 속성은 docs/design/styles.md가 소유한다. 문서의 이름과 이 객체의 프로퍼티 이름을 일대일로 맞춘다.
public object DiaryStyles {
    public val bottomSheetTitle: Style =
        Style {
            fillWidth()
            contentPadding(horizontal = dimens.bottomSheetHorizontalPadding, vertical = dimens.bottomSheetTitleVerticalPadding)
        }

    public val bottomSheetContent: Style =
        Style {
            fillWidth()
            contentPaddingBottom(dimens.bottomSheetBottomPadding)
        }

    public val bottomSheetSection: Style =
        Style {
            fillWidth()
            contentPaddingHorizontal(dimens.bottomSheetHorizontalPadding)
        }

    public val bottomSheetRow: Style =
        Style {
            minHeight(BottomSheetRowMinHeight)
            contentPaddingHorizontal(dimens.bottomSheetHorizontalPadding)
        }

    public val cardContent: Style =
        Style {
            fillWidth()
            contentPadding(dimens.cardContentPadding)
        }
}

public fun StyleScope.dimmed() {
    alpha(DIMMED_ALPHA)
}

private val StyleScope.dimens: DiaryDimens
    get() = LocalDiaryDimens.currentValue
