package io.github.taetae98coding.diary.data.account.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.supabase.api.SupabaseUser
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SupabaseUserMapperTest :
    FunSpec({
        test("인증 제공자의 사용자 정보를 그대로 옮긴다") {
            listOf(fixtureMonkey.giveMeOne<SupabaseUser>(), fixtureMonkey.giveMeOne<SupabaseUser>().copy(profileImage = null))
                .forEach { user ->
                    user.toUserData() shouldBe UserData(id = user.id, email = user.email, profileImage = user.profileImage)
                }
        }
    })
