@file:OptIn(ExperimentalFoundationStyleApi::class)

package io.github.taetae98coding.diary.compose.core.theme

import androidx.compose.foundation.style.ExperimentalFoundationStyleApi
import androidx.compose.foundation.style.Style
import androidx.compose.foundation.style.StyleScope
import androidx.compose.foundation.style.contentPadding
import androidx.compose.foundation.style.contentPaddingHorizontal
import androidx.compose.foundation.style.fillWidth
import androidx.compose.material3.ShapeDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private const val DIMMED_ALPHA = 0.38F
private val BottomSheetRowMinHeight = 56.dp
private val QrImageQuietZone = 16.dp

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

    public val qrImage: Style =
        Style {
            shape(ShapeDefaults.Medium)
            clip()
            background(Color.White)
            contentPadding(QrImageQuietZone)
        }
}

public fun StyleScope.dimmed() {
    alpha(DIMMED_ALPHA)
}

private val StyleScope.dimens: DiaryDimens
    get() = LocalDiaryDimens.currentValue
