package io.github.taetae98coding.diary.data.web.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.core.web.network.api.datasource.WebPageRemoteDataSource
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageHeaderRemoteEntity
import io.github.taetae98coding.diary.core.web.network.api.entity.WebPageRemoteEntity
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

class WebPageRepositoryImplTest :
    FunSpec({
        test("TC-c0f80397-8368-4688-8bda-5f9884bf9f96 TC-178ebe51-6c6f-4137-8b09-b4009f5bd484 저장된 URL과 요청 헤더를 순서 그대로 원격에 전달한다") {
            val url = "https://developer.android.com/${fixtureMonkey.giveMeOne<Int>()}"
            val headerList =
                listOf(
                    WebHeader(name = "X-Diary", value = "first"),
                    WebHeader(name = "X-Diary", value = "second"),
                    WebHeader(name = "X-Empty", value = ""),
                )
            val remoteDataSource = mockk<WebPageRemoteDataSource>()
            coEvery { remoteDataSource.get(url = any(), headerList = any()) } returns remotePage()
            val repository = WebPageRepositoryImpl(webPageRemoteDataSource = remoteDataSource)

            repository.fetch(url = url, headerList = headerList)

            coVerify(exactly = 1) {
                remoteDataSource.get(
                    url = url,
                    headerList =
                        listOf(
                            WebPageHeaderRemoteEntity(name = "X-Diary", value = "first"),
                            WebPageHeaderRemoteEntity(name = "X-Diary", value = "second"),
                            WebPageHeaderRemoteEntity(name = "X-Empty", value = ""),
                        ),
                )
            }
        }

        test("TC-069417bf-bcdf-4e50-9f52-6337d1805387 받은 응답의 주소를 기준 주소로, 본문을 그대로 전달한다") {
            val remotePage = remotePage()
            val remoteDataSource = mockk<WebPageRemoteDataSource>()
            coEvery { remoteDataSource.get(url = any(), headerList = any()) } returns remotePage
            val repository = WebPageRepositoryImpl(webPageRemoteDataSource = remoteDataSource)

            repository.fetch(url = remotePage.url, headerList = emptyList()) shouldBe
                WebPage(baseUrl = remotePage.url, body = remotePage.body)
        }

        test("TC-db6726ec-6a9a-440c-bef1-a36f4c3196ca 원격 요청이 실패하면 실패를 그대로 전파한다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val remoteDataSource = mockk<WebPageRemoteDataSource>()
            coEvery { remoteDataSource.get(url = any(), headerList = any()) } throws throwable
            val repository = WebPageRepositoryImpl(webPageRemoteDataSource = remoteDataSource)

            shouldThrow<IllegalStateException> {
                repository.fetch(url = fixtureMonkey.giveMeOne<String>(), headerList = emptyList())
            } shouldBeSameInstanceAs throwable
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun remotePage(): WebPageRemoteEntity = fixtureMonkey.giveMeOne<WebPageRemoteEntity>()
    }
}
