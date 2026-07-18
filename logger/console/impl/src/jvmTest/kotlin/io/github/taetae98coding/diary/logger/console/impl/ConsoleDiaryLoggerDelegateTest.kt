package io.github.taetae98coding.diary.logger.console.impl

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.console.api.ConsoleLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.io.ByteArrayOutputStream
import java.io.PrintStream

class ConsoleDiaryLoggerDelegateTest :
    BehaviorSpec({
        Given("콘솔 기록 수단이 등록되어 있다") {
            DiaryLogger.add(delegate = ConsoleDiaryLoggerDelegate())

            When("태그, 메시지, 오류 정보를 지정한 콘솔 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-011 지정한 태그, 메시지와 오류 내용이 개발 콘솔에 함께 남는다") {
                    // 빈 문자열이 생성되면 포함 검증이 무의미해지므로 접두사로 비어 있지 않음을 보장한다.
                    val tag = "tag${fixtureMonkey.giveMeOne<Int>()}"
                    val message = "message${fixtureMonkey.giveMeOne<Int>()}"
                    val errorMessage = "error${fixtureMonkey.giveMeOne<Int>()}"

                    val output =
                        captureConsole {
                            DiaryLogger.log(log = ConsoleLog(tag = tag, message = message, throwable = IllegalStateException(errorMessage)))
                        }

                    output shouldContain tag
                    output shouldContain message
                    output shouldContain errorMessage
                }
            }

            When("태그와 오류 정보를 지정하지 않은 콘솔 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-012 로그를 만든 클래스의 이름이 태그로, 메시지가 오류 없이 개발 콘솔에 남는다") {
                    val message = "message${fixtureMonkey.giveMeOne<Int>()}"

                    val output =
                        captureConsole {
                            DiaryLogger.log(log = ConsoleLog(message = message))
                        }

                    output shouldContain "[ConsoleDiaryLoggerDelegateTest] $message"
                    output shouldNotContain "Exception"
                }
            }

            When("콘솔 로그가 아닌 종류의 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-010 기본 태그와 로그 내용이 읽을 수 있는 문자열로 개발 콘솔에 남는다") {
                    val log = OtherLog(value = fixtureMonkey.giveMeOne<Int>())

                    val output =
                        captureConsole {
                            DiaryLogger.log(log = log)
                        }

                    output shouldContain "[Diary] $log"
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private data class OtherLog(
            val value: Int,
        ) : DiaryLog

        private fun captureConsole(block: () -> Unit): String {
            val originalOut = System.out
            val outputStream = ByteArrayOutputStream()
            System.setOut(PrintStream(outputStream))

            try {
                block()
            } finally {
                System.setOut(originalOut)
            }

            return outputStream.toString()
        }
    }
}
