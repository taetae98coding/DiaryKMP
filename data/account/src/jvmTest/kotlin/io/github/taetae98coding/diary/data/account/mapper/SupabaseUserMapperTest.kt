package io.github.taetae98coding.diary.data.account.mapper

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.UserData
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
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
                .map { user -> user.copy(providerSet = emptySet()) }
                .forEach { user ->
                    user.toUserData() shouldBe UserData(id = user.id, email = user.email, profileImage = user.profileImage, loginProviderSet = emptySet())
                }
        }

        test("연결된 로그인 수단 중 Google과 Apple만 옮기고 모르는 수단은 버린다") {
            val user = fixtureMonkey.giveMeOne<SupabaseUser>().copy(providerSet = setOf("google", "apple", "email"))

            user.toUserData().loginProviderSet shouldBe setOf(LoginProvider.GOOGLE, LoginProvider.APPLE)
        }
    })
