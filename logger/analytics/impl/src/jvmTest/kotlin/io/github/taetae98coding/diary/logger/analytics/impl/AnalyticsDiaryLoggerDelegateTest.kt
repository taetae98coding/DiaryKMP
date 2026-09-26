package io.github.taetae98coding.diary.logger.analytics.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.analytics.api.ScreenViewLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldBeEmpty
import io.mockk.clearMocks
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import kotlinx.serialization.json.JsonObject
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class AnalyticsDiaryLoggerDelegateTest :
    BehaviorSpec({
        Given("원격 분석 기록 수단이 등록되어 있고 실행 중인 플랫폼은 Google Analytics 4로 기록할 수 없는 플랫폼이다") {
            DiaryLogger.add(delegate = AnalyticsDiaryLoggerDelegate())

            When("화면 이름을 담은 원격 분석 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-014 어디에도 원격 분석이 남지 않고 전달은 오류 없이 완료된다") {
                    // 빈 문자열이 생성되면 검증이 무의미해지므로 접두사로 비어 있지 않음을 보장한다.
                    val screenName = "screen${fixtureMonkey.giveMeOne<Int>()}"

                    val output =
                        captureOutput {
                            shouldNotThrowAny {
                                DiaryLogger.log(log = ScreenViewLog(screenName = screenName))
                            }
                        }

                    output.shouldBeEmpty()
                }
            }
        }

        Given("원격 분석 기록 수단이 등록되어 있다") {
            val recordScreenView = mockk<(String) -> Unit>(relaxed = true)
            val recordEvent = mockk<(String, Map<String, Any>) -> Unit>(relaxed = true)
            DiaryLogger.add(delegate = AnalyticsDiaryLoggerDelegate(recordScreenView = recordScreenView, recordEvent = recordEvent))

            When("화면 조회 로그와 원격 분석 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-001 원격 분석 기록 수단이 두 로그를 남긴다") {
                    clearMocks(recordScreenView, recordEvent)
                    val screenViewLog = ScreenViewLog(screenName = fixtureMonkey.giveMeOne<String>())
                    val eventLog = AnalyticsEventLog(name = fixtureMonkey.giveMeOne<String>(), parameters = JsonObject(emptyMap()))

                    DiaryLogger.log(log = screenViewLog)
                    DiaryLogger.log(log = eventLog)

                    verify(exactly = 1) { recordScreenView(screenViewLog.screenName) }
                    verify(exactly = 1) { recordEvent(eventLog.name, any()) }
                    confirmVerified(recordScreenView, recordEvent)
                }
            }

            When("원격 분석 로그가 아닌 종류의 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-001 TC-APP-LOGGING-DOMAIN-003 원격 분석 기록 수단은 그 로그를 남기지 않고 전달은 오류 없이 완료된다") {
                    clearMocks(recordScreenView, recordEvent)

                    shouldNotThrowAny {
                        DiaryLogger.log(log = OtherLog(value = fixtureMonkey.giveMeOne<Int>()))
                    }

                    verify(exactly = 0) { recordScreenView(any()) }
                    verify(exactly = 0) { recordEvent(any(), any()) }
                }
            }
        }
    }) {
    public companion object {
        private data class OtherLog(
            val value: Int,
        ) : DiaryLog

        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun captureOutput(block: () -> Unit): String {
            val originalOut = System.out
            val originalErr = System.err
            val outputStream = ByteArrayOutputStream()
            val printStream = PrintStream(outputStream)
            System.setOut(printStream)
            System.setErr(printStream)

            try {
                block()
            } finally {
                System.setOut(originalOut)
                System.setErr(originalErr)
            }

            return outputStream.toString()
        }
    }
}
