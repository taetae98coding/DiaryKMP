package io.github.taetae98coding.diary.data.setting.repository

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMe
import io.github.taetae98coding.diary.core.gemini.network.api.GeminiException
import io.github.taetae98coding.diary.core.gemini.network.api.datasource.GeminiModelRemoteDataSource
import io.github.taetae98coding.diary.core.gemini.network.api.entity.GeminiModelRemoteEntity
import io.github.taetae98coding.diary.domain.setting.exception.GeminiApiKeyInvalidException
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class GeminiModelRepositoryImplTest :
    FunSpec({
        test("받아 온 모델을 순서 그대로 전달한다") {
            val apiKey = "testApiKey"
            val remoteList = fixtureMonkey.giveMe<GeminiModelRemoteEntity>(3)
            val remoteDataSource =
                mockk<GeminiModelRemoteDataSource> {
                    coEvery { getAvailableModel(apiKey) } returns remoteList
                }
            val repository = GeminiModelRepositoryImpl(geminiModelRemoteDataSource = remoteDataSource)

            val actual = repository.fetch(apiKey = apiKey)

            actual.map { model -> model.id } shouldBe remoteList.map { remote -> remote.id }
            actual.map { model -> model.displayName } shouldBe remoteList.map { remote -> remote.displayName }
            actual.map { model -> model.description } shouldBe remoteList.map { remote -> remote.description }
            coVerify(exactly = 1) { remoteDataSource.getAvailableModel(apiKey) }
        }

        test("빈 목록을 그대로 전달한다") {
            val repository =
                GeminiModelRepositoryImpl(
                    geminiModelRemoteDataSource =
                        mockk {
                            coEvery { getAvailableModel(any()) } returns emptyList()
                        },
                )

            repository.fetch(apiKey = "testApiKey").shouldBeEmpty()
        }

        test("인증 실패를 화면이 구분할 수 있는 도메인 실패로 바꿔 알린다") {
            val cause = GeminiException.InvalidApiKey(cause = IllegalStateException("unauthorized"))
            val repository =
                GeminiModelRepositoryImpl(
                    geminiModelRemoteDataSource =
                        mockk {
                            coEvery { getAvailableModel(any()) } throws cause
                        },
                )

            val thrown = shouldThrow<GeminiApiKeyInvalidException> { repository.fetch(apiKey = "testApiKey") }

            thrown.cause shouldBe cause
        }

        test("그 밖의 실패는 원인을 바꾸지 않고 그대로 전파한다") {
            val failure = IllegalStateException("server error")
            val repository =
                GeminiModelRepositoryImpl(
                    geminiModelRemoteDataSource =
                        mockk {
                            coEvery { getAvailableModel(any()) } throws failure
                        },
                )

            val thrown = shouldThrow<IllegalStateException> { repository.fetch(apiKey = "testApiKey") }

            thrown shouldBe failure
        }
    })
