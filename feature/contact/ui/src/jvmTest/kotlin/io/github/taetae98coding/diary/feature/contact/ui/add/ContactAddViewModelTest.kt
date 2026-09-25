@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.contact.ui.add

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.domain.contact.exception.ContactNameBlankException
import io.github.taetae98coding.diary.domain.contact.exception.ContactPhoneNumberBlankException
import io.github.taetae98coding.diary.domain.contact.usecase.AddContactUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.uuid.Uuid

class ContactAddViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-CONTACT-ADD-DOMAIN-008 추가 처리 중 전달된 추가 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val firstDetail = detail()
                val secondDetail = detail()
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.add(detail = firstDetail)
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                viewModel.add(detail = secondDetail)
                runCurrent()

                coVerify(exactly = 1) { useCase(AddContactUseCase.Parameter(detail = firstDetail)) }
                coVerify(exactly = 0) { useCase(AddContactUseCase.Parameter(detail = secondDetail)) }

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()
            }
        }

        test("TC-CONTACT-ADD-FEATURE-007 추가에 성공하면 성공 Effect를 한 번 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val addedId = fixtureMonkey.giveMeOne<Uuid>()
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(any()) } returns Result.success(addedId)
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail())
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactAddEffect.AddSucceeded(id = addedId)
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-CONTACT-ADD-FEATURE-009 이름이 공백이면 이름 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(any()) } returns Result.failure(ContactNameBlankException())
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail(name = "  "))
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactAddEffect.NameBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-CONTACT-ADD-FEATURE-009 번호가 공백인 전화번호 항목이 있으면 전화번호 미입력 Effect를 보내고 진행 상태를 해제한다") {
            runTest(mainDispatcher) {
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(any()) } returns Result.failure(ContactPhoneNumberBlankException())
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail().copy(phoneNumberList = listOf(ContactPhoneNumber(number = "  "))))
                    advanceUntilIdle()

                    awaitItem() shouldBe ContactAddEffect.PhoneNumberBlank
                    expectNoEvents()
                }

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-CONTACT-ADD-FEATURE-030 기기 저장에 실패하면 Effect 없이 진행 상태만 해제하고 같은 내용으로 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val detail = detail().copy(description = "description-${fixtureMonkey.giveMeOne<String>()}", phoneNumberList = listOf(ContactPhoneNumber(number = "phone-${fixtureMonkey.giveMeOne<String>()}")))
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException())
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.effect.test {
                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.add(detail = detail)
                    advanceUntilIdle()

                    expectNoEvents()
                }

                coVerify(exactly = 2) { useCase(AddContactUseCase.Parameter(detail = detail)) }
            }
        }

        test("TC-CONTACT-ADD-FEATURE-008 추가를 처리하는 동안 진행 상태를 유지하고 완료 후 해제한다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Result<Uuid>>()
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(any()) } coAnswers { completion.await() }
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.add(detail = detail())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Result.success(fixtureMonkey.giveMeOne<Uuid>()))
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("추가가 취소되면 진행 상태를 해제하고 다시 추가할 수 있다") {
            runTest(mainDispatcher) {
                val firstDetail = detail()
                val secondDetail = detail()
                val useCase = mockk<AddContactUseCase>()
                coEvery { useCase(AddContactUseCase.Parameter(detail = firstDetail)) } throws CancellationException()
                coEvery { useCase(AddContactUseCase.Parameter(detail = secondDetail)) } returns Result.success(fixtureMonkey.giveMeOne<Uuid>())
                val viewModel = ContactAddViewModel(addContactUseCase = useCase)

                viewModel.add(detail = firstDetail)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()

                viewModel.add(detail = secondDetail)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(AddContactUseCase.Parameter(detail = firstDetail)) }
                coVerify(exactly = 1) { useCase(AddContactUseCase.Parameter(detail = secondDetail)) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun detail(name: String = "name-${fixtureMonkey.giveMeOne<String>()}"): ContactDetail = ContactDetail.EMPTY.copy(name = name)
    }
}
