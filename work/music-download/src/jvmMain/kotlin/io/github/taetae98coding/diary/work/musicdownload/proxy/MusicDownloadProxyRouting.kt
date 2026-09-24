package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondFile
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import java.io.File

private const val VIDEO_ID_PARAMETER = "videoId"

internal fun Application.musicDownloadProxy(handler: MusicDownloadProxyHandler) {
    routing {
        get(MUSIC_DOWNLOAD_PROXY_HEALTH_PATH) {
            call.respondText(text = "OK")
        }

        get("$MUSIC_DOWNLOAD_PROXY_MUSIC_PATH/{$VIDEO_ID_PARAMETER}") {
            val videoId = call.parameters[VIDEO_ID_PARAMETER].orEmpty()

            when (val response = handler.handle(videoId = videoId)) {
                is MusicDownloadProxyResponse.Rejected -> call.respond(HttpStatusCode.BadRequest)
                is MusicDownloadProxyResponse.Failed -> call.respond(HttpStatusCode.InternalServerError)
                is MusicDownloadProxyResponse.Completed -> call.respondFile(File(response.path))
            }
        }
    }
}
