package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxySetting
import io.github.taetae98coding.diary.domain.setting.repository.MusicDownloadProxySettingRepository
import io.github.taetae98coding.diary.work.musicdownload.tool.DownloadToolPrepareResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.plugins.HttpTimeout
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import java.io.IOException

private const val ADDRESS = "http://192.168.0.10:27180"

class ProxyDownloadToolPreparerTest :
    BehaviorSpec({
        Given("프록시 주소가 저장되어 있지 않다") {
            When("준비한다") {
                Then("TC-345754f4-8765-4308-bcf4-73a49557bef4 프록시에 아무것도 요청하지 않고 주소 없음으로 끝난다") {
                    listOf("", "   ").forEach { address ->
                        val engine = MockEngine { respond(content = "OK") }
                        val preparer = preparer(address = address, engine = engine)

                        preparer.prepare() shouldBe DownloadToolPrepareResult.ProxyNotConfigured

                        engine.requestHistory.shouldBeEmpty()
                    }
                }
            }
        }

        Given("프록시 주소가 저장되어 있고 연결 확인이 성공한다") {
            When("준비한다") {
                Then("TC-845a4309-e3a8-4e34-aa90-04b6bbdf8938 TC-5636799b-df77-4583-80d9-9749b8d071e1 저장된 주소의 연결 확인 요청을 보내고 준비된 것으로 본다") {
                    listOf(ADDRESS, "$ADDRESS/", " $ADDRESS ").forEach { address ->
                        val engine = MockEngine { respond(content = "OK") }
                        val preparer = preparer(address = address, engine = engine)

                        preparer.prepare() shouldBe DownloadToolPrepareResult.Prepared

                        engine.requestHistory
                            .single()
                            .url
                            .toString() shouldBe "$ADDRESS/health"
                    }
                }
            }
        }

        Given("프록시에 연결할 수 없다") {
            When("준비한다") {
                Then("TC-727e36b5-7c5e-481d-a7cc-e52f4e426c55 연결할 수 없음으로 끝난다") {
                    val engineList =
                        listOf(
                            MockEngine { respondError(status = HttpStatusCode.InternalServerError) },
                            MockEngine { throw IOException("connection refused") },
                        )

                    engineList.forEach { engine ->
                        val preparer = preparer(address = ADDRESS, engine = engine)

                        preparer.prepare() shouldBe DownloadToolPrepareResult.ProxyUnreachable
                    }
                }
            }
        }

        Given("프록시가 연결 확인에 응답하기까지 시간이 걸린다") {
            When("준비한다") {
                Then("TC-af453dc1-4eb2-4b4c-9b69-f15d3f311110 10초 안에 응답하면 준비되고 10초가 지나면 연결할 수 없음으로 끝난다") {
                    val caseList =
                        listOf(
                            PROXY_HEALTH_TIMEOUT_MILLIS - 1 to DownloadToolPrepareResult.Prepared,
                            PROXY_HEALTH_TIMEOUT_MILLIS + 1 to DownloadToolPrepareResult.ProxyUnreachable,
                        )

                    caseList.forEach { (delayMillis, expected) ->
                        runTest {
                            val engine = delayedEngine(scheduler = testScheduler, delayMillis = delayMillis)
                            val preparer = preparer(address = ADDRESS, engine = engine)

                            preparer.prepare() shouldBe expected
                        }
                    }
                }
            }
        }
    })

private fun delayedEngine(
    scheduler: TestCoroutineScheduler,
    delayMillis: Long,
): MockEngine =
    MockEngine(
        MockEngineConfig().apply {
            dispatcher = StandardTestDispatcher(scheduler)
            addHandler {
                delay(delayMillis)
                respond(content = "OK")
            }
        },
    )

private fun preparer(
    address: String,
    engine: MockEngine,
): ProxyDownloadToolPreparer {
    val repository = mockk<MusicDownloadProxySettingRepository>()
    every { repository.get() } returns flowOf(MusicDownloadProxySetting(address = address))

    return ProxyDownloadToolPreparer(
        httpClient = HttpClient(engine) { install(HttpTimeout) },
        musicDownloadProxySettingRepository = repository,
    )
}
