package io.github.taetae98coding.diary.compose.map.web

internal inline fun <R> MapHttpServer.use(block: (MapHttpServer) -> R): R {
    start()

    return try {
        block(this)
    } finally {
        stop()
    }
}
