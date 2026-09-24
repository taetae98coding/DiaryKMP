package io.github.taetae98coding.diary.data.web.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.browsercookie.api.datasource.ChromeCookieLocalDataSource
import io.github.taetae98coding.diary.core.browsercookie.api.entity.BrowserCookieLocalEntity
import io.github.taetae98coding.diary.data.web.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

private const val PROFILE_DIRECTORY = "Default"

class ChromeCookieRepositoryImplTest :
    FunSpec({
        test("요청한 도메인으로 Chrome 쿠키를 읽어 모델로 제공한다") {
            val entityList = fixtureMonkey.giveMe<BrowserCookieLocalEntity>(size = 3)
            val domainSet = setOf("example.com", ".example.com")
            val dataSource = mockk<ChromeCookieLocalDataSource>()
            coEvery { dataSource.findByDomain(profileDirectory = PROFILE_DIRECTORY, domainSet = domainSet) } returns entityList
            val repository = ChromeCookieRepositoryImpl(chromeCookieLocalDataSource = dataSource)

            repository.findByDomain(profileDirectory = PROFILE_DIRECTORY, domainSet = domainSet) shouldBe entityList.map { entity -> entity.toDomain() }

            coVerify(exactly = 1) { dataSource.findByDomain(profileDirectory = PROFILE_DIRECTORY, domainSet = domainSet) }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-008 읽기 실패를 그대로 알린다") {
            val failure = IllegalStateException("read failed")
            val dataSource = mockk<ChromeCookieLocalDataSource>()
            coEvery { dataSource.findByDomain(any(), any()) } throws failure
            val repository = ChromeCookieRepositoryImpl(chromeCookieLocalDataSource = dataSource)

            shouldThrow<IllegalStateException> {
                repository.findByDomain(profileDirectory = PROFILE_DIRECTORY, domainSet = setOf("example.com"))
            } shouldBe failure
        }
    })
