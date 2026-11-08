package io.github.taetae98coding.diary.work.sync.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayOverflowPolicyRemoteEntity
import io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRepeatRemoteEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.datetime.DateTimeUnit

class DDayRemoteMapperTest :
    FunSpec({
        test("로컬 디데이는 원격 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.ddayLocalEntity().next()

            local.toRemote().toLocal() shouldBe local
        }

        test("일 간격은 일 수만, 개월 간격은 개월 수만 담긴 원격 간격이 된다") {
            DateTimeUnit.DayBased(days = 100).toRemote() shouldBe DDayRepeatRemoteEntity.Days(days = 100)
            DateTimeUnit.MonthBased(months = 24).toRemote() shouldBe DDayRepeatRemoteEntity.Months(months = 24)
            DDayRepeatRemoteEntity.Days(days = 100).toLocal() shouldBe DateTimeUnit.DayBased(days = 100)
            DDayRepeatRemoteEntity.Months(months = 24).toLocal() shouldBe DateTimeUnit.MonthBased(months = 24)
        }

        test("응답에 같은 간격이 겹쳐 오면 기기에는 하나만 남는다") {
            val local = Arb.ddayLocalEntity().next()
            val remote = local.toRemote().let { value -> value.copy(detail = value.detail.copy(repeats = List(2) { DDayRepeatRemoteEntity.Days(days = 7) })) }

            remote.toLocal().detail.repeats shouldBe setOf(DateTimeUnit.DayBased(days = 7))
        }

        test("달력에 없는 날짜 처리는 같은 의미의 원격 값과 짝을 이룬다") {
            mapOf(
                DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END to DDayOverflowPolicyRemoteEntity.CLAMP_TO_MONTH_END,
                DDayOverflowPolicyLocalEntity.SKIP to DDayOverflowPolicyRemoteEntity.SKIP,
                DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY to DDayOverflowPolicyRemoteEntity.NEXT_MONTH_FIRST_DAY,
            ).forEach { (local, remote) ->
                local.toRemote() shouldBe remote
                remote.toLocal() shouldBe local
            }
        }
    })
