package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import io.github.taetae98coding.diary.work.musicdownload.tool.tempMusicFilePath
import io.github.taetae98coding.diary.work.musicdownload.work.testDownloadTarget
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readBytes

private const val ADDRESS = "http://192.168.0.10:27180"
private val STREAM = ByteArray(size = 100) { index -> index.toByte() }

class ProxyMusicDownloaderTest :
    BehaviorSpec({
        Given("프록시가 파일 전체와 크기를 응답한다") {
            When("영상을 받으면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-008 저장된 주소로 그 곡의 영상 ID를 요청한다") {
                    val target = testDownloadTarget()
                    val engine = fileEngine()
                    val downloader = downloader(engine = engine)

                    downloader.download(target = target, path = tempMusicFilePath(target.videoId), onProgress = {})

                    engine.requestHistory
                        .single()
                        .url
                        .toString() shouldBe "$ADDRESS/music/${target.videoId}"
                }

                Then("TC-MUSIC-DOWNLOAD-DATA-009 받은 내용을 영상 ID의 파일로 저장하고 받다 만 파일은 남기지 않는다") {
                    val target = testDownloadTarget()
                    val path = tempMusicFilePath(target.videoId)
                    val downloader = downloader(engine = fileEngine())

                    val isDownloaded = downloader.download(target = target, path = path, onProgress = {})

                    isDownloaded shouldBe true
                    Path.of(path.completed).readBytes() shouldBe STREAM
                    Path.of(path.downloading).exists() shouldBe false
                }

                Then("TC-MUSIC-DOWNLOAD-FEATURE-019 응답의 크기에 대해 받은 만큼을 백분율로 알린다") {
                    val target = testDownloadTarget()
                    val progressList = mutableListOf<Float>()
                    val downloader = downloader(engine = fileEngine())

                    downloader.download(target = target, path = tempMusicFilePath(target.videoId), onProgress = { value -> progressList += value })

                    progressList shouldContain 1F
                    progressList.all { value -> value in 0F..1F } shouldBe true
                }
            }
        }

        Given("프록시가 실패로 응답한다") {
            When("영상을 받으면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-011 실패로 끝나고 파일이 남지 않는다") {
                    val statusList = listOf(HttpStatusCode.InternalServerError, HttpStatusCode.BadRequest)

                    statusList.forEach { status ->
                        val target = testDownloadTarget()
                        val path = tempMusicFilePath(target.videoId)
                        val downloader = downloader(engine = MockEngine { respondError(status = status) })

                        downloader.download(target = target, path = path, onProgress = {}) shouldBe false

                        Path.of(path.completed).exists() shouldBe false
                        Path.of(path.downloading).exists() shouldBe false
                    }
                }
            }
        }

        Given("프록시가 파일을 일부까지만 전달한 뒤 끊긴다") {
            When("영상을 받으면") {
                Then("TC-MUSIC-DOWNLOAD-DATA-006 실패로 끝나고 파일이 남지 않는다") {
                    val target = testDownloadTarget()
                    val path = tempMusicFilePath(target.videoId)
                    val engine =
                        MockEngine {
                            respond(
                                content = ByteReadChannel(STREAM.copyOf(newSize = STREAM.size / 2)),
                                headers = headersOf(HttpHeaders.ContentLength, STREAM.size.toString()),
                            )
                        }
                    val downloader = downloader(engine = engine)

                    downloader.download(target = target, path = path, onProgress = {}) shouldBe false

                    Path.of(path.completed).exists() shouldBe false
                    Path.of(path.downloading).exists() shouldBe false
                }
            }
        }
    })

private fun fileEngine(): MockEngine =
    MockEngine {
        respond(
            content = ByteReadChannel(STREAM),
            headers = headersOf(HttpHeaders.ContentLength, STREAM.size.toString()),
        )
    }

private fun downloader(engine: MockEngine): ProxyMusicDownloader {
    val repository = mockk<MusicDownloadProxySettingRepository>()
    every { repository.get() } returns flowOf(MusicDownloadProxySetting(address = ADDRESS))

    return ProxyMusicDownloader(
        httpClient = HttpClient(engine),
        musicDownloadProxySettingRepository = repository,
        dispatcher = Dispatchers.Default,
    )
}
