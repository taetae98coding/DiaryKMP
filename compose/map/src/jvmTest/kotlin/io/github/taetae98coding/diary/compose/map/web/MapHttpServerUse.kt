package io.github.taetae98coding.diary.compose.map.web

internal suspend inline fun <R> MapHttpServer.use(block: (MapHttpServer) -> R): R =
    try {
        block(this)
    } finally {
        close()
    }
