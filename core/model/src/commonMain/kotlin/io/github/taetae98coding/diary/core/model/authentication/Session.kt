package io.github.taetae98coding.diary.core.model.authentication

public sealed interface Session {
    public data object Authenticated : Session

    public data object NotAuthenticated : Session
}
