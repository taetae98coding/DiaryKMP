package io.github.taetae98coding.diary

import io.github.taetae98coding.diary.app.shared.navigation.AppDeepLink

public data object DiaryDeepLink {
    public fun open(deepLink: String) {
        AppDeepLink.open(deepLink = deepLink)
    }
}
