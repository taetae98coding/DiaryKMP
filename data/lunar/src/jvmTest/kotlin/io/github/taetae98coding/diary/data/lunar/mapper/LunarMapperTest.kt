package io.github.taetae98coding.diary.data.lunar.mapper

import io.github.taetae98coding.diary.core.calendar.database.api.entity.LunarDateLocalEntity
import io.github.taetae98coding.diary.core.calendar.network.api.entity.LunarDateRemoteEntity
import io.github.taetae98coding.diary.core.model.lunar.LunarDate
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.next
import kotlinx.datetime.LocalDate

class LunarMapperTest :
    FunSpec({
        test("remote to local") {
            val solarYear = Arb.int().next()
            val remote =
                LunarDateRemoteEntity(
                    solar = LocalDate(2026, 2, 17),
                    year = Arb.int().next(),
                    month = Arb.int().next(),
                    day = Arb.int().next(),
                    isLeapMonth = Arb.boolean().next(),
                )

            remote.toLocal(solarYear = solarYear) shouldBe
                LunarDateLocalEntity(
                    solar = remote.solar,
                    solarYear = solarYear,
                    lunarYear = remote.year,
                    lunarMonth = remote.month,
                    lunarDay = remote.day,
                    isLeapMonth = remote.isLeapMonth,
                )
        }

        test("local to domain") {
            val local =
                LunarDateLocalEntity(
                    solar = LocalDate(2026, 2, 17),
                    solarYear = Arb.int().next(),
                    lunarYear = Arb.int().next(),
                    lunarMonth = Arb.int().next(),
                    lunarDay = Arb.int().next(),
                    isLeapMonth = Arb.boolean().next(),
                )

            local.toDomain() shouldBe
                LunarDate(
                    solar = local.solar,
                    year = local.lunarYear,
                    month = local.lunarMonth,
                    day = local.lunarDay,
                    isLeapMonth = local.isLeapMonth,
                )
        }
    })
