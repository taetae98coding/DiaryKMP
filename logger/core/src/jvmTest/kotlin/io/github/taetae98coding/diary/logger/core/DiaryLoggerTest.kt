package io.github.taetae98coding.diary.logger.core

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class DiaryLoggerTest :
    BehaviorSpec({
        Given("등록된 기록 수단이 없다") {
            When("로그를 공통 창구에 전달한다") {
                Then("전달은 오류 없이 완료된다") {
                    shouldNotThrowAny {
                        DiaryLogger.log(log = FirstLog(value = fixtureMonkey.giveMeOne<Int>()))
                    }
                }
            }
        }

        Given("기록 수단 A와 B가 등록되어 있다") {
            val delegateA = recordingDelegate()
            val delegateB = recordingDelegate()
            DiaryLogger.add(delegate = delegateA.delegate)
            DiaryLogger.add(delegate = delegateB.delegate)

            When("서로 다른 종류의 로그를 차례로 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-020 두 로그가 종류와 관계없이 A와 B 모두에 전달된다") {
                    val firstLog = FirstLog(value = fixtureMonkey.giveMeOne<Int>())
                    val secondLog = SecondLog(value = fixtureMonkey.giveMeOne<Int>())

                    DiaryLogger.log(log = firstLog)
                    DiaryLogger.log(log = secondLog)

                    delegateA.logList.shouldContainExactly(firstLog, secondLog)
                    delegateB.logList.shouldContainExactly(firstLog, secondLog)
                }
            }
        }

        Given("같은 종류를 담당하는 기록 수단 두 개가 등록되어 있다") {
            val first = recordingDelegate()
            val second = recordingDelegate()
            DiaryLogger.add(delegate = first.delegate)
            DiaryLogger.add(delegate = second.delegate)

            When("그 종류의 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-002 두 기록 수단 모두에 같은 로그가 남는다") {
                    val log = FirstLog(value = fixtureMonkey.giveMeOne<Int>())

                    DiaryLogger.log(log = log)

                    first.logList.shouldContainExactly(log)
                    second.logList.shouldContainExactly(log)
                }
            }
        }

        Given("기록 수단이 아직 등록되지 않았다") {
            val recording = recordingDelegate()

            When("그 수단이 담당하는 종류의 로그를 공통 창구에 전달한 뒤 수단을 등록한다") {
                Then("TC-APP-LOGGING-DOMAIN-004 등록 이후에도 앞서 전달한 로그는 그 수단에 남지 않는다") {
                    DiaryLogger.log(log = FirstLog(value = fixtureMonkey.giveMeOne<Int>()))

                    DiaryLogger.add(delegate = recording.delegate)

                    recording.logList.shouldBeEmpty()
                }
            }
        }

        Given("하나의 기록 수단을 두 번 등록했다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)
            DiaryLogger.add(delegate = recording.delegate)

            When("그 수단이 담당하는 종류의 로그를 공통 창구에 한 번 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-006 그 수단에 로그가 한 번만 남는다") {
                    val log = FirstLog(value = fixtureMonkey.giveMeOne<Int>())

                    DiaryLogger.log(log = log)

                    recording.logList.shouldContainExactly(log)
                }
            }
        }

        Given("로그를 남길 때마다 실패하는 기록 수단과 정상 동작하는 기록 수단이 함께 등록되어 있다") {
            val failingDelegate = mockk<DiaryLoggerDelegate>()
            every { failingDelegate.log(log = any()) } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = failingDelegate)
            DiaryLogger.add(delegate = recording.delegate)

            When("그 종류의 로그를 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-008 정상 동작하는 수단에는 로그가 남고 전달은 오류 없이 완료된다") {
                    val log = FirstLog(value = fixtureMonkey.giveMeOne<Int>())

                    shouldNotThrowAny {
                        DiaryLogger.log(log = log)
                    }

                    recording.logList.shouldContainExactly(log)
                    verify(exactly = 1) { failingDelegate.log(log = log) }
                }
            }
        }

        Given("모든 종류를 담당하는 기록 수단이 등록되어 있다") {
            val recording = recordingDelegate()
            DiaryLogger.add(delegate = recording.delegate)

            When("하나의 흐름에서 로그 여러 개를 순서대로 공통 창구에 전달한다") {
                Then("TC-APP-LOGGING-DOMAIN-009 그 수단에 로그가 전달한 순서대로 남는다") {
                    val logList =
                        List(size = 3) {
                            FirstLog(value = fixtureMonkey.giveMeOne<Int>())
                        }

                    logList.forEach { log -> DiaryLogger.log(log = log) }

                    recording.logList.shouldContainExactly(logList)
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private data class FirstLog(
            val value: Int,
        ) : DiaryLog

        private data class SecondLog(
            val value: Int,
        ) : DiaryLog

        private class RecordingDelegate(
            val delegate: DiaryLoggerDelegate,
            val logList: List<DiaryLog>,
        )

        private fun recordingDelegate(): RecordingDelegate {
            val logList = mutableListOf<DiaryLog>()
            val delegate = mockk<DiaryLoggerDelegate>()

            every { delegate.log(log = any()) } answers {
                logList += firstArg<DiaryLog>()
            }

            return RecordingDelegate(delegate = delegate, logList = logList)
        }
    }
}
