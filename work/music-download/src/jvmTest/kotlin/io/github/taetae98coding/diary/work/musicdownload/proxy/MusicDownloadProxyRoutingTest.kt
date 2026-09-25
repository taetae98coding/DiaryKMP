package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.work.musicdownload.work.testVideoId
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsBytes
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentLength
import io.ktor.server.testing.testApplication
import io.ktor.utils.io.readByte
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.io.path.createTempFile
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

private val STREAM = byteArrayOf(9, 8, 7, 6, 5)
private const val PARTIAL_READ_STREAM_SIZE = 1_000_000

class MusicDownloadProxyRoutingTest :
    BehaviorSpec({
        Given("프록시가 제공 중이다") {
            When("연결 확인 요청이 들어오면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-001 제공 중임을 응답하고 어떤 영상도 받지 않는다") {
                    val handler = mockk<MusicDownloadProxyHandler>()

                    testApplication {
                        application { musicDownloadProxy(handler = handler) }

                        val response = client.get("/health")

                        response.status shouldBe HttpStatusCode.OK
                        response.bodyAsText() shouldBe "OK"
                    }
                }
            }

            When("거절된 영상 요청이 들어오면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-002 잘못된 요청으로 응답한다") {
                    val handler = handler(response = MusicDownloadProxyResponse.Rejected)

                    testApplication {
                        application { musicDownloadProxy(handler = handler) }

                        client.get("/music/short").status shouldBe HttpStatusCode.BadRequest
                    }
                }
            }

            When("받지 못한 영상 요청이 들어오면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-006 처리 실패로 응답한다") {
                    val handler = handler(response = MusicDownloadProxyResponse.Failed)

                    testApplication {
                        application { musicDownloadProxy(handler = handler) }

                        client.get("/music/${testVideoId()}").status shouldBe HttpStatusCode.InternalServerError
                    }
                }
            }

            When("완성된 파일이 있는 영상 요청이 들어오면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-003 TC-MUSIC-DOWNLOAD-PROXY-DATA-004 파일 전체와 그 크기를 응답한다") {
                    val file = createTempFile(suffix = ".mp4").apply { writeBytes(STREAM) }
                    val handler = handler(response = MusicDownloadProxyResponse.Completed(path = file.toString()))

                    testApplication {
                        application { musicDownloadProxy(handler = handler) }

                        val response = client.get("/music/${testVideoId()}")

                        response.status shouldBe HttpStatusCode.OK
                        response.contentLength() shouldBe STREAM.size.toLong()
                        response.bodyAsBytes() shouldBe STREAM
                    }
                }
            }

            When("파일을 전달하는 도중 요청한 쪽이 연결을 끊으면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DATA-014 프록시의 파일은 지워지지 않고 내용도 그대로 남는다") {
                    val content = ByteArray(PARTIAL_READ_STREAM_SIZE) { index -> index.toByte() }
                    val file = createTempFile(suffix = ".mp4").apply { writeBytes(content) }
                    val handler = handler(response = MusicDownloadProxyResponse.Completed(path = file.toString()))

                    testApplication {
                        application { musicDownloadProxy(handler = handler) }

                        client.prepareGet("/music/${testVideoId()}").execute { response ->
                            response.bodyAsChannel().readByte()
                        }
                    }

                    file.exists() shouldBe true
                    file.readBytes() shouldBe content
                }
            }
        }
    })

private fun handler(response: MusicDownloadProxyResponse): MusicDownloadProxyHandler {
    val handler = mockk<MusicDownloadProxyHandler>()
    coEvery { handler.handle(videoId = any()) } returns response

    return handler
}
