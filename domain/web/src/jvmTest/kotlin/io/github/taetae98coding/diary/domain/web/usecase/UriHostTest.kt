package io.github.taetae98coding.diary.domain.web.usecase

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UriHostTest :
    FunSpec({
        test("주소에서 호스트만 소문자로 읽는다") {
            val cases =
                mapOf(
                    "https://mail.example.com/inbox" to "mail.example.com",
                    "https://Example.COM" to "example.com",
                    "http://example.com:8080/path?q=1#top" to "example.com",
                    "https://user:pass@example.com/" to "example.com",
                    "https://[2001:db8::1]:443/path" to "2001:db8::1",
                    "https://example.com?query" to "example.com",
                )

            cases.forEach { (uri, host) ->
                uri.uriHostOrNull() shouldBe host
            }
        }

        test("호스트를 알 수 없는 주소는 null이다") {
            listOf("", "not a url", "mailto:someone@example.com", "https://", "://example.com").forEach { uri ->
                uri.uriHostOrNull() shouldBe null
            }
        }

        test("호스트와 상위 도메인마다 점 없는 이름과 점으로 시작하는 이름을 만든다") {
            val cases =
                mapOf(
                    "mail.example.com" to setOf("mail.example.com", ".mail.example.com", "example.com", ".example.com"),
                    "example.com" to setOf("example.com", ".example.com"),
                    "localhost" to emptySet(),
                )

            cases.forEach { (host, domainSet) ->
                host.cookieDomainSet() shouldBe domainSet
            }
        }
    })
