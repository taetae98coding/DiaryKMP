package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.github.taetae98coding.diary.core.model.playlist.MusicDownloadProxyStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import java.net.InetAddress

private const val START_TIMEOUT_MILLIS = 30_000L
private val SERVING_ADDRESS_REGEX = Regex("""http://192\.168\.0\.10:(\d+)""")

class MusicDownloadProxyServerTest :
    BehaviorSpec({
        Given("이 기기에 사설 IPv4 주소가 하나 있다") {
            When("프록시를 시작하면") {
                Then("TC-MUSIC-DOWNLOAD-PROXY-DOMAIN-004 TC-MUSIC-DOWNLOAD-PROXY-DOMAIN-001 운영체제가 고른 포트로 제공 중이 되고 사설 IPv4 주소로만 후보를 만든다") {
                    val server = server()

                    val status = withTimeout(START_TIMEOUT_MILLIS) { server.status.filterNotNull().first() }

                    val address = status.shouldBeInstanceOf<MusicDownloadProxyStatus.Serving>().addressList.single()
                    address shouldMatch SERVING_ADDRESS_REGEX
                    SERVING_ADDRESS_REGEX
                        .find(address)
                        ?.groupValues
                        ?.get(1)
                        ?.toInt() shouldNotBe 0
                }

                Then("TC-MUSIC-DOWNLOAD-PROXY-DOMAIN-005 시작한 뒤 네트워크가 바뀌어도 후보를 바꾸지 않고 주소를 다시 확인하지 않는다") {
                    var addressLookupCount = 0
                    val addressListQueue =
                        ArrayDeque(
                            listOf(
                                listOf(InetAddress.getByName("192.168.0.10")),
                                listOf(InetAddress.getByName("10.0.0.5")),
                            ),
                        )
                    val server =
                        server(
                            networkAddressSource = {
                                addressLookupCount += 1
                                addressListQueue.removeFirst()
                            },
                        )
                    val started = withTimeout(START_TIMEOUT_MILLIS) { JvmMusicDownloadProxyManager(musicDownloadProxyServer = server).status.first() }
                    val checkedAgain = withTimeout(START_TIMEOUT_MILLIS) { JvmMusicDownloadProxyManager(musicDownloadProxyServer = server).status.first() }

                    checkedAgain shouldBe started
                    started.shouldBeInstanceOf<MusicDownloadProxyStatus.Serving>().addressList.single() shouldMatch SERVING_ADDRESS_REGEX
                    addressLookupCount shouldBe 1
                }

                Then("두 번 시작해도 각자 제공 중이 된다") {
                    val first = withTimeout(START_TIMEOUT_MILLIS) { server().status.filterNotNull().first() }
                    val second = withTimeout(START_TIMEOUT_MILLIS) { server().status.filterNotNull().first() }

                    first.shouldBeInstanceOf<MusicDownloadProxyStatus.Serving>()
                    second.shouldBeInstanceOf<MusicDownloadProxyStatus.Serving>()
                    (first == second) shouldBe false
                }
            }
        }
    })

private fun server(
    networkAddressSource: NetworkAddressSource =
        NetworkAddressSource {
            listOf(InetAddress.getByName("192.168.0.10"), InetAddress.getByName("127.0.0.1"))
        },
): MusicDownloadProxyServer =
    MusicDownloadProxyServer(
        handler = mockk(),
        networkAddressSource = networkAddressSource,
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default + CoroutineExceptionHandler { _, _ -> }),
        dispatcher = Dispatchers.IO,
    )
