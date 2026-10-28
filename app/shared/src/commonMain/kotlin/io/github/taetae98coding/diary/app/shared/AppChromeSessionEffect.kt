package io.github.taetae98coding.diary.app.shared

internal sealed interface AppChromeSessionEffect {
    data object ImportFailed : AppChromeSessionEffect
}
