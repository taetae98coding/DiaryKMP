package io.github.taetae98coding.diary.logger.crashlytics.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldBeEmpty
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class CrashlyticsDiaryLoggerDelegateTest :
    BehaviorSpec({
        Given("오류 보고 기록 수단이 등록되어 있고 실행 중인 플랫폼은 Firebase Crashlytics가 오류를 수집할 수 없는 플랫폼이다") {
            DiaryLogger.add(delegate = CrashlyticsDiaryLoggerDelegate())

            When("오류 정보와 메시지를 지정한 오류 보고 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-013 어디에도 오류 보고가 남지 않고 전달은 오류 없이 완료된다") {
                    // 빈 문자열이 생성되면 검증이 무의미해지므로 접두사로 비어 있지 않음을 보장한다.
                    val message = "message${fixtureMonkey.giveMeOne<Int>()}"
                    val errorMessage = "error${fixtureMonkey.giveMeOne<Int>()}"

                    val output =
                        captureOutput {
                            shouldNotThrowAny {
                                DiaryLogger.log(
                                    log = CrashlyticsLog(message = message, throwable = IllegalStateException(errorMessage)),
                                )
                            }
                        }

                    output.shouldBeEmpty()
                }
            }
        }
    }) {
    public companion object {
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
