package io.github.taetae98coding.diary.data.browser.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.browser.cookie.api.datasource.ChromeProfileLocalDataSource
import io.github.taetae98coding.diary.core.browser.cookie.api.entity.ChromeProfileLocalEntity
import io.github.taetae98coding.diary.data.browser.mapper.toDomain
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class ChromeProfileRepositoryImplTest :
    FunSpec({
        test("TC-d3d1ce85-3ec3-48e3-8398-35f9c7fc71bb 읽은 프로필을 순서대로 모델로 제공한다") {
            val entityList = fixtureMonkey.giveMe<ChromeProfileLocalEntity>(size = 3)
            val dataSource = mockk<ChromeProfileLocalDataSource>()
            coEvery { dataSource.readProfileList() } returns entityList
            val repository = ChromeProfileRepositoryImpl(chromeProfileLocalDataSource = dataSource)

            repository.readProfileList() shouldBe entityList.map { entity -> entity.toDomain() }
        }

        test("TC-44472fb1-aacc-49fa-9f71-0f4130a3781d 읽기 실패를 그대로 알린다") {
            val failure = IllegalStateException("read failed")
            val dataSource = mockk<ChromeProfileLocalDataSource>()
            coEvery { dataSource.readProfileList() } throws failure
            val repository = ChromeProfileRepositoryImpl(chromeProfileLocalDataSource = dataSource)

            shouldThrow<IllegalStateException> { repository.readProfileList() } shouldBe failure
        }
    })
