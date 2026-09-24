package io.github.taetae98coding.diary.work.musicdownload.proxy

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import java.net.InetAddress

private const val PORT = 27_180

class MusicDownloadProxyAddressTest :
    FunSpec({
        test("TC-MUSIC-DOWNLOAD-PROXY-DOMAIN-001 사설 IPv4 주소마다 후보를 하나씩 만들고 나머지는 뺀다") {
            val addressList =
                listOf(
                    "192.168.0.10",
                    "10.0.0.5",
                    "172.16.4.2",
                    "127.0.0.1",
                    "8.8.8.8",
                    "fe80::1",
                ).map { host -> InetAddress.getByName(host) }

            addressList.toMusicDownloadProxyAddressList(port = PORT) shouldContainExactly
                listOf(
                    "http://192.168.0.10:$PORT",
                    "http://10.0.0.5:$PORT",
                    "http://172.16.4.2:$PORT",
                )
        }

        test("주소가 하나도 없으면 후보도 없다") {
            emptyList<InetAddress>().toMusicDownloadProxyAddressList(port = PORT) shouldContainExactly emptyList()
        }
    })
