package io.github.taetae98coding.diary.data.browser.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.browsercookie.api.datasource.InAppBrowserCookieLocalDataSource
import io.github.taetae98coding.diary.core.model.browser.BrowserCookie
import io.github.taetae98coding.diary.data.browser.mapper.toLocal
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class InAppBrowserCookieRepositoryImplTest :
    FunSpec({
        test("쿠키를 로컬 엔티티로 바꿔 앱 안 웹 표시 수단에 넘긴다") {
            val cookieList = fixtureMonkey.giveMe<BrowserCookie>(size = 3)
            val dataSource = mockk<InAppBrowserCookieLocalDataSource>()
            coEvery { dataSource.upsert(any()) } just runs
            val repository = InAppBrowserCookieRepositoryImpl(inAppBrowserCookieLocalDataSource = dataSource)

            repository.upsert(cookieList = cookieList)

            coVerify(exactly = 1) { dataSource.upsert(cookieList = cookieList.map { cookie -> cookie.toLocal() }) }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-009 넘기기 실패를 그대로 알린다") {
            val failure = IllegalStateException("store failed")
            val dataSource = mockk<InAppBrowserCookieLocalDataSource>()
            coEvery { dataSource.upsert(any()) } throws failure
            val repository = InAppBrowserCookieRepositoryImpl(inAppBrowserCookieLocalDataSource = dataSource)

            shouldThrow<IllegalStateException> {
                repository.upsert(cookieList = fixtureMonkey.giveMe<BrowserCookie>(size = 1))
            } shouldBe failure
        }

        test("지우기를 앱 안 웹 표시 수단에 그대로 위임한다") {
            val dataSource = mockk<InAppBrowserCookieLocalDataSource>()
            coEvery { dataSource.deleteAll() } just runs
            val repository = InAppBrowserCookieRepositoryImpl(inAppBrowserCookieLocalDataSource = dataSource)

            repository.deleteAll()

            coVerify(exactly = 1) { dataSource.deleteAll() }
        }

        test("TC-CHROME-SESSION-IMPORT-DOMAIN-020 지우기 실패를 그대로 알린다") {
            val failure = IllegalStateException("clear failed")
            val dataSource = mockk<InAppBrowserCookieLocalDataSource>()
            coEvery { dataSource.deleteAll() } throws failure
            val repository = InAppBrowserCookieRepositoryImpl(inAppBrowserCookieLocalDataSource = dataSource)

            shouldThrow<IllegalStateException> {
                repository.deleteAll()
            } shouldBe failure
        }
    })
