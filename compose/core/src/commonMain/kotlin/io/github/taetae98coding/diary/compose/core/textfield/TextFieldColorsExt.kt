package io.github.taetae98coding.diary.compose.core.textfield

import androidx.compose.material3.TextFieldColors
import androidx.compose.ui.graphics.Color

public fun TextFieldColors.transparentIndicator(): TextFieldColors =
    copy(
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        errorIndicatorColor = Color.Transparent,
    )

public fun TextFieldColors.transparentContainer(): TextFieldColors =
    copy(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
    )
