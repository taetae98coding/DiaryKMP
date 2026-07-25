package io.github.taetae98coding.diary.compose.core.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

public val DiaryFadeContentTransform: ContentTransform = fadeIn() togetherWith fadeOut()
