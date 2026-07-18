package io.github.taetae98coding.diary.library.fixturemonkey

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import com.navercorp.fixturemonkey.kotlin.setExp
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange
import kotlin.time.Instant

private const val SAMPLE_COUNT = 1_000
private const val MIN_DISTINCT_COUNT = 100

private val MIN_INSTANT = Instant.fromEpochSeconds(0)
private val MAX_INSTANT = Instant.fromEpochSeconds(4_102_444_800L)

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

class DiaryFixtureMonkeyTest :
    FunSpec({
        test("Instant를 표현 가능한 범위 안에서 만든다") {
            repeat(SAMPLE_COUNT) {
                val instant = fixtureMonkey.giveMeOne<Instant>()

                instant shouldBeGreaterThanOrEqualTo MIN_INSTANT
                instant shouldBeLessThanOrEqualTo MAX_INSTANT
            }
        }

        test("Instant를 초 정밀도로 만들어 에포크 밀리초와 에포크 초로 왕복해도 값이 보존된다") {
            repeat(SAMPLE_COUNT) {
                val instant = fixtureMonkey.giveMeOne<Instant>()

                Instant.fromEpochMilliseconds(instant.toEpochMilliseconds()) shouldBe instant
                Instant.fromEpochSeconds(instant.epochSeconds) shouldBe instant
            }
        }

        test("Instant를 담은 타입의 Instant도 매번 다른 값으로 만든다") {
            val instantList = List(SAMPLE_COUNT) { fixtureMonkey.giveMeOne<InstantHolder>().instant }

            instantList.distinct() shouldHaveAtLeastSize MIN_DISTINCT_COUNT
            instantList.forEach { instant ->
                instant shouldBeGreaterThanOrEqualTo MIN_INSTANT
                instant shouldBeLessThanOrEqualTo MAX_INSTANT
            }
        }

        test("고정한 Instant는 그대로 유지한다") {
            val instant = Instant.fromEpochSeconds(1_000)

            repeat(SAMPLE_COUNT) {
                val holder =
                    fixtureMonkey
                        .giveMeKotlinBuilder<InstantHolder>()
                        .setExp(InstantHolder::instant, instant)
                        .sample()

                holder.instant shouldBe instant
            }
        }

        test("LocalDateRange를 시작일이 종료일보다 뒤이지 않게 만든다") {
            repeat(SAMPLE_COUNT) {
                val dateRange = fixtureMonkey.giveMeOne<LocalDateRange>()

                dateRange.endInclusive shouldBeGreaterThanOrEqualTo dateRange.start
            }
        }

        test("LocalDateRange를 담은 타입의 기간도 매번 다른 값으로 만든다") {
            val dateRangeList = List(SAMPLE_COUNT) { fixtureMonkey.giveMeOne<DateRangeHolder>().dateRange }

            dateRangeList.distinct() shouldHaveAtLeastSize MIN_DISTINCT_COUNT
            dateRangeList.forEach { dateRange ->
                dateRange.endInclusive shouldBeGreaterThanOrEqualTo dateRange.start
            }
        }

        // LocalDateRange는 introspector로만 만들 수 있고 introspector는 고정한 값을 덮어쓴다.
        // 그래서 특정 기간이 필요하면 setExp가 아니라 copy로 바꿔 쓴다는 것을 이 테스트가 지킨다.
        test("copy로 바꾼 LocalDateRange는 그대로 유지한다") {
            val dateRange = LocalDate(2026, 7, 5)..LocalDate(2026, 7, 11)

            repeat(SAMPLE_COUNT) {
                val holder = fixtureMonkey.giveMeOne<DateRangeHolder>().copy(dateRange = dateRange)

                holder.dateRange shouldBe dateRange
            }
        }
    })

internal data class DateRangeHolder(
    val dateRange: LocalDateRange,
    val date: LocalDate,
)

internal data class InstantHolder(
    val instant: Instant,
)
