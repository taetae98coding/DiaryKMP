package io.github.taetae98coding.diary.data.account.repository

import io.github.taetae98coding.diary.core.datastore.api.account.datasource.LoginProviderLocalDataSource
import io.github.taetae98coding.diary.core.datastore.api.account.entity.LoginProviderLocalEntity
import io.github.taetae98coding.diary.core.model.authentication.LoginProvider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

class LoginProviderRepositoryImplTest :
    FunSpec({
        test("저장한 로그인 수단을 같은 수단으로 읽는다") {
            listOf(
                LoginProviderLocalEntity.GOOGLE to LoginProvider.GOOGLE,
                LoginProviderLocalEntity.APPLE to LoginProvider.APPLE,
                null to null,
            ).forEach { (local, expected) ->
                val dataSource = mockk<LoginProviderLocalDataSource>()
                coEvery { dataSource.read() } returns local

                LoginProviderRepositoryImpl(loginProviderLocalDataSource = dataSource).read() shouldBe expected
            }
        }

        test("로그인 수단을 지정하고 지운다") {
            val dataSource = mockk<LoginProviderLocalDataSource>()
            coEvery { dataSource.upsert(provider = any()) } just Runs
            val repository = LoginProviderRepositoryImpl(loginProviderLocalDataSource = dataSource)

            repository.set(provider = LoginProvider.APPLE)
            repository.set(provider = LoginProvider.GOOGLE)
            repository.unset()

            coVerify(exactly = 1) { dataSource.upsert(provider = LoginProviderLocalEntity.APPLE) }
            coVerify(exactly = 1) { dataSource.upsert(provider = LoginProviderLocalEntity.GOOGLE) }
            coVerify(exactly = 1) { dataSource.upsert(provider = null) }
        }
    })
