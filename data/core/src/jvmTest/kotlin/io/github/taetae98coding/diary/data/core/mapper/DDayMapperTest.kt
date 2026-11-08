package io.github.taetae98coding.diary.data.core.mapper

import io.github.taetae98coding.diary.core.database.api.dday.entity.DDayOverflowPolicyLocalEntity
import io.github.taetae98coding.diary.core.model.dday.DDayOverflowPolicy
import io.github.taetae98coding.diary.core.testing.dday.dday
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next

class DDayMapperTest :
    FunSpec({
        test("디데이는 로컬 모델을 거쳐도 바뀌지 않는다") {
            val dday = Arb.dday().next()

            dday.toLocal().toDomain() shouldBe dday
        }

        test("로컬 디데이는 도메인 모델을 거쳐도 바뀌지 않는다") {
            val local = Arb.ddayLocalEntity().next()

            local.toDomain().toLocal() shouldBe local
        }

        test("디데이를 로컬 모델로 바꿀 때 반복 간격을 그대로 옮긴다") {
            val dday = Arb.dday().next()

            dday.toLocal().detail.repeats shouldBe dday.detail.repeats
        }

        test("달력에 없는 날짜 처리는 같은 의미의 로컬 값과 짝을 이룬다") {
            mapOf(
                DDayOverflowPolicy.CLAMP_TO_MONTH_END to DDayOverflowPolicyLocalEntity.CLAMP_TO_MONTH_END,
                DDayOverflowPolicy.SKIP to DDayOverflowPolicyLocalEntity.SKIP,
                DDayOverflowPolicy.NEXT_MONTH_FIRST_DAY to DDayOverflowPolicyLocalEntity.NEXT_MONTH_FIRST_DAY,
            ).forEach { (policy, local) ->
                policy.toLocal() shouldBe local
                local.toDomain() shouldBe policy
            }
        }
    })
