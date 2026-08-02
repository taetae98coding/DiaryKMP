package io.github.taetae98coding.diary.domain.tag.usecase

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.domain.tag.repository.AccountTagRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

class FindTagUseCaseTest :
    BehaviorSpec({
        Given("로그인한 계정과 조회할 수 있는 태그가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tag = tag()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountTagRepository = mockk<AccountTagRepository>()
            every { accountTagRepository.find(account = account, tagId = tag.id) } returns flowOf(tag)
            val useCase =
                FindTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("태그를 조회한다") {
                Then("현재 계정의 태그를 반환한다") {
                    val result = useCase(parameter = tag.id).first()

                    result.shouldBeSuccess() shouldBe tag
                    verify(exactly = 1) {
                        accountTagRepository.find(account = account, tagId = tag.id)
                    }
                }
            }
        }

        Given("조회할 태그가 없다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val accountTagRepository = mockk<AccountTagRepository>()
            every { accountTagRepository.find(account = account, tagId = any()) } returns flowOf(null)
            val useCase =
                FindTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("태그를 조회한다") {
                Then("태그 없음을 반환한다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>()).first()

                    result.shouldBeSuccess().shouldBeNull()
                }
            }
        }

        Given("계정 조회가 실패하도록 준비되어 있다") {
            val throwable = IllegalStateException(fixtureMonkey.giveMeOne<String>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.failure(throwable))
            val accountTagRepository = mockk<AccountTagRepository>(relaxed = true)
            val useCase =
                FindTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("태그 조회를 실행한다") {
                Then("계정 조회 실패를 전달하고 태그를 조회하지 않는다") {
                    val result = useCase(parameter = fixtureMonkey.giveMeOne<Uuid>()).first()

                    result.shouldBeFailure() shouldBeSameInstanceAs throwable
                    verify(exactly = 0) {
                        accountTagRepository.find(account = any(), tagId = any())
                    }
                }
            }
        }

        Given("조회한 태그가 변경되는 Flow가 준비되어 있다") {
            val account = fixtureMonkey.giveMeOne<Account.User>()
            val tag = tag()
            val changedTag = tag.copy(detail = fixtureMonkey.giveMeOne<TagDetail>())
            val getAccountUseCase = mockk<GetAccountUseCase>()
            every { getAccountUseCase(parameter = Unit) } returns flowOf(Result.success(account))
            val tagFlow = MutableStateFlow<Tag?>(tag)
            val accountTagRepository = mockk<AccountTagRepository>()
            every { accountTagRepository.find(account = account, tagId = tag.id) } returns tagFlow
            val useCase =
                FindTagUseCase(
                    getAccountUseCase = getAccountUseCase,
                    accountTagRepository = accountTagRepository,
                )

            When("태그가 변경된다") {
                Then("변경된 태그를 이어서 반환한다") {
                    useCase(parameter = tag.id).test {
                        awaitItem().shouldBeSuccess() shouldBe tag

                        tagFlow.value = changedTag

                        awaitItem().shouldBeSuccess() shouldBe changedTag
                        cancelAndIgnoreRemainingEvents()
                    }
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun tag(): Tag =
            fixtureMonkey
                .giveMeKotlinBuilder<Tag>()
                .setExp(Tag::updatedAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .setExp(Tag::createdAt, Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>()))
                .sample()
    }
}
