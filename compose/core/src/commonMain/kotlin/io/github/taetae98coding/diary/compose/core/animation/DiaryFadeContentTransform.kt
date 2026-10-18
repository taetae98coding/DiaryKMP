package io.github.taetae98coding.diary.compose.core.animation

import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith

// fadeIn·fadeOut의 기본 완급과 같다. 화면 전환을 따라 움직여야 하는 다른 표현이 같은 값을 쓰도록 이름을 붙인다.
public val DiaryFadeAnimationSpec: FiniteAnimationSpec<Float> = spring(stiffness = Spring.StiffnessMediumLow)

public val DiaryFadeContentTransform: ContentTransform =
    fadeIn(animationSpec = DiaryFadeAnimationSpec) togetherWith fadeOut(animationSpec = DiaryFadeAnimationSpec)
