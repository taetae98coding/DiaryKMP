package io.github.taetae98coding.diary.domain.integrity.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict
import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdictValue
import io.github.taetae98coding.diary.domain.integrity.repository.PlayIntegrityRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.analytics.api.AnalyticsEventLog
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class LogPlayIntegrityUseCaseTest :
    BehaviorSpec({
        Given("서버가 Google의 모든 필드를 겹친 구조로 담은 판정 결과를 돌려준다") {
            val value = List(12) { fixtureMonkey.giveMeOne<String>() }
            val sdkVersion = fixtureMonkey.giveMeOne<Int>()
            val verdict =
                verdict(
                    "requestDetails" to
                        group(
                            "requestPackageName" to text(value[0]),
                            "requestHash" to text(value[1]),
                            "timestampMillis" to text(value[2]),
                        ),
                    "appIntegrity" to
                        group(
                            "appRecognitionVerdict" to text(value[3]),
                            "packageName" to text(value[4]),
                            "certificateSha256Digest" to valueList(text(value[5])),
                            "versionCode" to text(value[6]),
                        ),
                    "deviceIntegrity" to
                        group(
                            "deviceRecognitionVerdict" to valueList(text(value[7])),
                            "deviceAttributes" to group("sdkVersion" to PlayIntegrityVerdictValue.Number(value = sdkVersion)),
                            "recentDeviceActivity" to group("deviceActivityLevel" to text(value[8])),
                        ),
                    "accountDetails" to group("appLicensingVerdict" to text(value[9])),
                    "environmentDetails" to
                        group(
                            "appAccessRiskVerdict" to group("appsDetected" to valueList(text(value[10]))),
                            "playProtectVerdict" to text(value[11]),
                        ),
                )
            val logList = recordLog()

            When("판정을 확인한다") {
                val result = useCase(verdict = verdict)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-001 play_integrity 원격 분석 로그 하나에 모든 필드를 한 단계 목록으로 남긴다") {
                    result.shouldBeSuccess()
                    val analyticsLogList = logList.filterIsInstance<AnalyticsEventLog>()
                    analyticsLogList shouldHaveSize 1
                    analyticsLogList.single().name shouldBe "play_integrity"
                    analyticsLogList.single().parameters shouldBe
                        JsonObject(
                            mapOf(
                                "request_package_name" to JsonPrimitive(value[0]),
                                "request_hash" to JsonPrimitive(value[1]),
                                "timestamp_millis" to JsonPrimitive(value[2]),
                                "app_recognition_verdict" to JsonPrimitive(value[3]),
                                "package_name" to JsonPrimitive(value[4]),
                                "certificate_sha256_digest" to JsonPrimitive(value[5]),
                                "version_code" to JsonPrimitive(value[6]),
                                "device_recognition_verdict" to JsonPrimitive(value[7]),
                                "sdk_version" to JsonPrimitive(sdkVersion),
                                "device_activity_level" to JsonPrimitive(value[8]),
                                "app_licensing_verdict" to JsonPrimitive(value[9]),
                                "apps_detected" to JsonPrimitive(value[10]),
                                "play_protect_verdict" to JsonPrimitive(value[11]),
                            ),
                        )
                }
            }
        }

        Given("판정 결과의 서로 다른 위치에 같은 이름의 필드가 있다") {
            val recallValue = fixtureMonkey.giveMeOne<Boolean>()
            val recallWriteDate = fixtureMonkey.giveMeOne<Int>()
            val appRecognitionVerdict = fixtureMonkey.giveMeOne<String>()
            val verdict =
                verdict(
                    "appIntegrity" to group("appRecognitionVerdict" to text(appRecognitionVerdict)),
                    "deviceIntegrity" to
                        group(
                            "deviceRecall" to
                                group(
                                    "values" to group("flag" to PlayIntegrityVerdictValue.Flag(value = recallValue)),
                                    "writeDates" to group("flag" to PlayIntegrityVerdictValue.Number(value = recallWriteDate)),
                                ),
                        ),
                )
            val logList = recordLog()

            When("판정을 확인한다") {
                useCase(verdict = verdict)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-002 겹치는 필드만 바로 위 필드 이름을 붙여 구분한다") {
                    logList.filterIsInstance<AnalyticsEventLog>().single().parameters shouldBe
                        JsonObject(
                            mapOf(
                                "app_recognition_verdict" to JsonPrimitive(appRecognitionVerdict),
                                "values_flag" to JsonPrimitive(recallValue.toString()),
                                "write_dates_flag" to JsonPrimitive(recallWriteDate),
                            ),
                        )
                }
            }
        }

        Given("판정 결과에 여러 값을 가진 필드가 있다") {
            val valueList = List(2) { "value${fixtureMonkey.giveMeOne<Int>()}" }
            val verdict =
                verdict(
                    "deviceIntegrity" to group("deviceRecognitionVerdict" to valueList(*valueList.map { value -> text(value) }.toTypedArray())),
                )
            val logList = recordLog()

            When("판정을 확인한다") {
                useCase(verdict = verdict)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-003 받은 순서대로 쉼표로 이은 문자열 하나로 남긴다") {
                    logList.filterIsInstance<AnalyticsEventLog>().single().parameters["device_recognition_verdict"] shouldBe
                        JsonPrimitive(valueList.joinToString(separator = ","))
                }
            }
        }

        Given("판정 결과에 숫자, 문자열, 참, 거짓 값이 있다") {
            val number = fixtureMonkey.giveMeOne<Long>()
            val text = fixtureMonkey.giveMeOne<Long>().toString()
            val verdict =
                verdict(
                    "number" to PlayIntegrityVerdictValue.Number(value = number),
                    "text" to text(text),
                    "yes" to PlayIntegrityVerdictValue.Flag(value = true),
                    "no" to PlayIntegrityVerdictValue.Flag(value = false),
                )
            val logList = recordLog()

            When("판정을 확인한다") {
                useCase(verdict = verdict)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-004 숫자와 문자열은 종류를 지키고 참·거짓은 문자열로 남긴다") {
                    logList.filterIsInstance<AnalyticsEventLog>().single().parameters shouldBe
                        JsonObject(
                            mapOf(
                                "number" to JsonPrimitive(number),
                                "text" to JsonPrimitive(text),
                                "yes" to JsonPrimitive("true"),
                                "no" to JsonPrimitive("false"),
                            ),
                        )
                }
            }
        }

        Given("판정 결과에 빈 필드와 값이 있는 필드가 함께 있다") {
            val appRecognitionVerdict = fixtureMonkey.giveMeOne<String>()
            val deviceIntegrityList =
                listOf(
                    group("deviceRecognitionVerdict" to valueList()),
                    group(),
                    group("deviceAttributes" to group()),
                    group("deviceRecognitionVerdict" to PlayIntegrityVerdictValue.Null),
                )

            When("판정을 확인한다") {
                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-005 빈 필드에서 나온 이름은 담지 않는다") {
                    deviceIntegrityList.forEach { deviceIntegrity ->
                        val logList = recordLog()
                        val verdict =
                            verdict(
                                "appIntegrity" to group("appRecognitionVerdict" to text(appRecognitionVerdict)),
                                "deviceIntegrity" to deviceIntegrity,
                            )

                        useCase(verdict = verdict)(parameter = Unit)

                        logList.filterIsInstance<AnalyticsEventLog>().single().parameters shouldBe
                            JsonObject(mapOf("app_recognition_verdict" to JsonPrimitive(appRecognitionVerdict)))
                    }
                }
            }
        }

        Given("판정 결과에 알려지지 않은 새 필드가 있다") {
            val newValue = fixtureMonkey.giveMeOne<String>()
            val verdict = verdict("environmentDetails" to group("newVerdict" to text(newValue)))
            val logList = recordLog()

            When("판정을 확인한다") {
                useCase(verdict = verdict)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-006 새 필드도 그대로 남긴다") {
                    logList.filterIsInstance<AnalyticsEventLog>().single().parameters["new_verdict"] shouldBe JsonPrimitive(newValue)
                }
            }
        }

        Given("Play Integrity를 제공하지 않는 플랫폼이다") {
            val logList = recordLog()

            When("판정을 확인한다") {
                val result = useCase(verdict = null)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-007 원격 분석 로그를 남기지 않고 오류 없이 끝난다") {
                    result.shouldBeSuccess()
                    logList.filterIsInstance<AnalyticsEventLog>().shouldBeEmpty()
                }
            }
        }

        Given("토큰 발급이나 서버 요청이 실패한다") {
            val repository = mockk<PlayIntegrityRepository>()
            coEvery { repository.fetch() } throws IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val logList = recordLog()

            When("판정을 확인한다") {
                val result = LogPlayIntegrityUseCase(playIntegrityRepository = repository)(parameter = Unit)

                Then("TC-PLAY-INTEGRITY-LOGGING-DOMAIN-008 TC-PLAY-INTEGRITY-LOGGING-DOMAIN-009 원격 분석 로그와 오류 보고 없이 오류를 던지지 않고 끝난다") {
                    result.shouldBeFailure()
                    logList.filterIsInstance<AnalyticsEventLog>().shouldBeEmpty()
                    logList.filterIsInstance<CrashlyticsLog>().shouldBeEmpty()
                }
            }
        }
    }) {
    private companion object {
        fun useCase(verdict: PlayIntegrityVerdict?): LogPlayIntegrityUseCase {
            val repository = mockk<PlayIntegrityRepository>()
            coEvery { repository.fetch() } returns verdict

            return LogPlayIntegrityUseCase(playIntegrityRepository = repository)
        }

        fun verdict(vararg field: Pair<String, PlayIntegrityVerdictValue>): PlayIntegrityVerdict = PlayIntegrityVerdict(fieldMap = mapOf(*field))

        fun group(vararg field: Pair<String, PlayIntegrityVerdictValue>): PlayIntegrityVerdictValue.Group = PlayIntegrityVerdictValue.Group(fieldMap = mapOf(*field))

        fun valueList(vararg value: PlayIntegrityVerdictValue): PlayIntegrityVerdictValue.ValueList = PlayIntegrityVerdictValue.ValueList(valueList = value.toList())

        fun text(value: String): PlayIntegrityVerdictValue.Text = PlayIntegrityVerdictValue.Text(value = value)

        fun recordLog(): List<DiaryLog> {
            val logList = mutableListOf<DiaryLog>()
            val delegate = mockk<DiaryLoggerDelegate>()
            every { delegate.log(log = any()) } answers { logList += firstArg<DiaryLog>() }
            DiaryLogger.add(delegate = delegate)

            return logList
        }
    }
}
