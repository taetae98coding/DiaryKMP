package io.github.taetae98coding.diary.library.fixture

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveAtLeastSize
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.take
import kotlin.time.Instant

private const val SAMPLE_COUNT = 1_000
private const val MIN_DISTINCT_COUNT = 100

private val MIN_INSTANT = Instant.fromEpochSeconds(0)
private val MAX_INSTANT = Instant.fromEpochSeconds(4_102_444_800L)

class DiaryArbTest :
    FunSpec({
        test("Instant를 1970년부터 2100년 사이에서 만든다") {
            Arb.secondPrecisionInstant().take(SAMPLE_COUNT).forEach { instant ->
                instant shouldBeGreaterThanOrEqualTo MIN_INSTANT
                instant shouldBeLessThanOrEqualTo MAX_INSTANT
            }
        }

        test("Instant를 초 정밀도로 만들어 에포크 밀리초와 에포크 초로 왕복해도 값이 보존된다") {
            Arb.secondPrecisionInstant().take(SAMPLE_COUNT).forEach { instant ->
                Instant.fromEpochMilliseconds(instant.toEpochMilliseconds()) shouldBe instant
                Instant.fromEpochSeconds(instant.epochSeconds) shouldBe instant
            }
        }

        test("Instant를 매번 다른 값으로 만든다") {
            Arb
                .secondPrecisionInstant()
                .take(SAMPLE_COUNT)
                .toList()
                .distinct() shouldHaveAtLeastSize MIN_DISTINCT_COUNT
        }

        test("Uuid를 겹치지 않게 만든다") {
            Arb
                .kotlinUuid()
                .take(SAMPLE_COUNT)
                .toList()
                .distinct() shouldHaveSize SAMPLE_COUNT
        }

        test("nonBlankString은 비어 있지도 공백만 있지도 않은 문자열을 만든다") {
            Arb.nonBlankString().take(SAMPLE_COUNT).forEach { value -> value.isNotBlank() shouldBe true }
        }

        test("nonBlankString은 매번 다른 값을 만든다") {
            Arb
                .nonBlankString()
                .take(SAMPLE_COUNT)
                .toList()
                .distinct() shouldHaveAtLeastSize MIN_DISTINCT_COUNT
        }

        test("LocalDateRange를 시작일이 종료일보다 뒤이지 않게 만든다") {
            Arb.localDateRange().take(SAMPLE_COUNT).forEach { dateRange ->
                dateRange.endInclusive shouldBeGreaterThanOrEqualTo dateRange.start
            }
        }

        test("LocalDateRange를 매번 다른 값으로 만든다") {
            Arb
                .localDateRange()
                .take(SAMPLE_COUNT)
                .toList()
                .distinct() shouldHaveAtLeastSize MIN_DISTINCT_COUNT
        }

        test("LocalDateTime을 매번 다른 값으로 만든다") {
            List(SAMPLE_COUNT) { Arb.kotlinxLocalDateTime().next() }.distinct() shouldHaveAtLeastSize MIN_DISTINCT_COUNT
        }
    })
