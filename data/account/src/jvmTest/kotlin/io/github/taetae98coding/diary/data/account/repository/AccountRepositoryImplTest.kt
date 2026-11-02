package io.github.taetae98coding.diary.data.account.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.authentication.AppleAuthorization
import io.github.taetae98coding.diary.core.network.api.account.datasource.AccountRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.account.entity.AppleAuthorizationRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class AccountRepositoryImplTest :
    FunSpec({
        test("TC-8f644268-bf99-49ba-9d69-db161bfe2ed8 Apple 인가 코드와 식별 값을 그대로 탈퇴 요청에 담는다") {
            val authorization = fixtureMonkey.giveMeOne<AppleAuthorization>()
            val remoteDataSource = mockk<AccountRemoteDataSource>()
            coEvery { remoteDataSource.delete(appleAuthorization = any()) } just Runs

            AccountRepositoryImpl(accountRemoteDataSource = remoteDataSource).delete(appleAuthorization = authorization)
            AccountRepositoryImpl(accountRemoteDataSource = remoteDataSource).delete(appleAuthorization = null)

            coVerify(exactly = 1) {
                remoteDataSource.delete(appleAuthorization = AppleAuthorizationRemoteEntity(authorizationCode = authorization.code, clientId = authorization.clientId))
            }
            coVerify(exactly = 1) { remoteDataSource.delete(appleAuthorization = null) }
        }
    })
