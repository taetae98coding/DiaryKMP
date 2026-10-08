package io.github.taetae98coding.diary.compose.map.web

import com.navercorp.fixturemonkey.FixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.library.fixturemonkey.nonBlankString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import java.net.HttpURLConnection
import java.net.URI

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class MapHttpServerTest :
    FunSpec({
        test("서버 하나로 지도마다 등록한 문서를 함께 제공한다") {
            val firstHtml = fixtureMonkey.nonBlankString()
            val secondHtml = fixtureMonkey.nonBlankString()

            MapHttpServer(naverMapResourceLoader = mockk()).use { server ->
                val firstPage = server.register(html = firstHtml)
                val secondPage = server.register(html = secondHtml)

                firstPage.url shouldNotBe secondPage.url
                URI(firstPage.url).readText() shouldBe firstHtml
                URI(secondPage.url).readText() shouldBe secondHtml
            }
        }

        test("등록을 닫은 지도 문서는 더 제공하지 않는다") {
            MapHttpServer(naverMapResourceLoader = mockk()).use { server ->
                val page = server.register(html = fixtureMonkey.nonBlankString())

                page.close()

                val connection = URI(page.url).toURL().openConnection() as HttpURLConnection
                connection.responseCode shouldBe HttpURLConnection.HTTP_NOT_FOUND
                connection.disconnect()
            }
        }

        test("시작하기 전에는 지도 문서를 등록할 수 없다") {
            shouldThrow<IllegalStateException> {
                MapHttpServer(naverMapResourceLoader = mockk()).register(html = fixtureMonkey.nonBlankString())
            }
        }
    })

private fun URI.readText(): String =
    toURL()
        .openStream()
        .bufferedReader()
        .use { reader -> reader.readText() }
