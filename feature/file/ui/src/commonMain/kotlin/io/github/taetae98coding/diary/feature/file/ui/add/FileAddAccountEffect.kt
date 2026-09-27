package io.github.taetae98coding.diary.feature.file.ui.add

internal sealed interface FileAddAccountEffect {
    data object BecameGuest : FileAddAccountEffect
}
