@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.feature.more.ui.profile

import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.domain.account.usecase.ChangeProfileImageUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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

class ProfileImageEditViewModelTest : FunSpec() {
    private lateinit var mainDispatcher: TestDispatcher

    init {
        beforeTest {
            mainDispatcher = StandardTestDispatcher()
            Dispatchers.setMain(mainDispatcher)
        }

        afterTest {
            Dispatchers.resetMain()
        }

        test("TC-PROFILE-IMAGE-EDIT-FEATURE-005 반영을 처리하는 동안 진행 상태가 되고 끝나면 돌아온다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Unit>()
                val useCase = mockk<ChangeProfileImageUseCase>()
                coEvery { useCase(any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = ProfileImageEditViewModel(changeProfileImageUseCase = useCase)

                viewModel.changeProfileImage(uri = fileUri(), cropRegion = cropRegion())
                runCurrent()

                viewModel.uiState.value.isInProgress
                    .shouldBeTrue()

                completion.complete(Unit)
                advanceUntilIdle()

                viewModel.uiState.value.isInProgress
                    .shouldBeFalse()
            }
        }

        test("TC-PROFILE-IMAGE-EDIT-DOMAIN-005 반영 처리 중에 전달된 반영 요청은 처리하지 않는다") {
            runTest(mainDispatcher) {
                val completion = CompletableDeferred<Unit>()
                val useCase = mockk<ChangeProfileImageUseCase>()
                coEvery { useCase(any()) } coAnswers { Result.success(completion.await()) }
                val viewModel = ProfileImageEditViewModel(changeProfileImageUseCase = useCase)

                viewModel.changeProfileImage(uri = fileUri(), cropRegion = cropRegion())
                runCurrent()
                viewModel.changeProfileImage(uri = fileUri(), cropRegion = cropRegion())
                runCurrent()
                completion.complete(Unit)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(any()) }
            }
        }

        test("TC-PROFILE-IMAGE-EDIT-FEATURE-006 반영에 성공하면 성공을 알린다") {
            runTest(mainDispatcher) {
                val useCase = mockk<ChangeProfileImageUseCase>()
                coEvery { useCase(any()) } returns Result.success(Unit)
                val viewModel = ProfileImageEditViewModel(changeProfileImageUseCase = useCase)

                viewModel.effect.test {
                    viewModel.changeProfileImage(uri = fileUri(), cropRegion = cropRegion())
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileImageEditEffect.ChangeSucceeded
                    expectNoEvents()
                }
            }
        }

        test("TC-PROFILE-IMAGE-EDIT-FEATURE-007 반영에 실패하면 실패를 알리고 다시 반영할 수 있다") {
            runTest(mainDispatcher) {
                val useCase = mockk<ChangeProfileImageUseCase>()
                coEvery { useCase(any()) } returns Result.failure(IllegalStateException("profile image error"))
                val viewModel = ProfileImageEditViewModel(changeProfileImageUseCase = useCase)

                viewModel.effect.test {
                    viewModel.changeProfileImage(uri = fileUri(), cropRegion = cropRegion())
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileImageEditEffect.ChangeFailed
                    viewModel.uiState.value.isInProgress
                        .shouldBeFalse()

                    viewModel.changeProfileImage(uri = fileUri(), cropRegion = cropRegion())
                    advanceUntilIdle()

                    awaitItem() shouldBe ProfileImageEditEffect.ChangeFailed
                    coVerify(exactly = 2) { useCase(any()) }
                }
            }
        }

        test("TC-PROFILE-IMAGE-EDIT-DATA-001 사진의 위치와 남길 영역을 그대로 담아 반영을 요청한다") {
            runTest(mainDispatcher) {
                val uri = fileUri()
                val cropRegion = cropRegion()
                val useCase = mockk<ChangeProfileImageUseCase>()
                coEvery { useCase(any()) } returns Result.success(Unit)
                val viewModel = ProfileImageEditViewModel(changeProfileImageUseCase = useCase)

                viewModel.changeProfileImage(uri = uri, cropRegion = cropRegion)
                advanceUntilIdle()

                coVerify(exactly = 1) { useCase(ChangeProfileImageUseCase.Parameter(uri = uri, cropRegion = cropRegion)) }
            }
        }
    }

    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun fileUri(): FileUri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}")

        private fun cropRegion(): ImageCropRegion = ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F)
    }
}
