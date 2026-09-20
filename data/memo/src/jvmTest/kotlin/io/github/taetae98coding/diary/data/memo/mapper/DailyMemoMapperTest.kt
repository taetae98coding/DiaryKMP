package io.github.taetae98coding.diary.data.memo.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.memo.entity.DailyMemoLocalEntity
import io.github.taetae98coding.diary.core.model.memo.DailyMemo
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class DailyMemoMapperTest :
    FunSpec({
        test("local to domain") {
            val local = fixtureMonkey.giveMeOne<DailyMemoLocalEntity>()

            local.toDomain() shouldBe
                DailyMemo(
                    id = local.id,
                    title = local.title,
                )
        }
    })

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()
