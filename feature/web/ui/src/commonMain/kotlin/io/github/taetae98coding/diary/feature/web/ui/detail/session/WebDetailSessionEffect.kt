package io.github.taetae98coding.diary.feature.web.ui.detail.session

internal sealed interface WebDetailSessionEffect {
    data object ImportFailed : WebDetailSessionEffect
}
