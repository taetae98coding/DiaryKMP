package io.github.taetae98coding.diary.domain.account.usecase

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageFormat
import io.github.taetae98coding.diary.core.testing.image.imageCropRegion
import io.github.taetae98coding.diary.domain.account.repository.UserDataRepository
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs

private const val MAX_SIDE_LENGTH_PX = 1024
private const val QUALITY_PERCENT = 90

class ChangeProfileImageUseCaseTest :
    BehaviorSpec({
        Given("프로필 이미지 반영 요청이 성공한다") {
            val parameter = parameter()
            val repository = mockk<UserDataRepository>()
            coEvery { repository.updateProfileImage(uri = any(), format = any(), cropRegion = any(), maxSideLength = any(), quality = any()) } just runs
            val useCase = ChangeProfileImageUseCase(userDataRepository = repository)

            When("고른 사진의 위치와 남길 영역으로 프로필 이미지 반영을 시작한다") {
                val result = useCase(parameter)

                Then("반영 성공이 전달되고 사용자 정보를 다시 확인하지 않는다") {
                    result.shouldBeSuccess()
                    coVerify(exactly = 0) { repository.refresh() }
                }

                Then("이미지는 JPEG로 바꾸고, 남긴 이미지의 최대 변 길이는 1024px이고 화질은 90이다") {
                    coVerify(exactly = 1) {
                        repository.updateProfileImage(uri = parameter.uri, format = ImageFormat.JPEG, cropRegion = parameter.cropRegion, maxSideLength = MAX_SIDE_LENGTH_PX, quality = QUALITY_PERCENT)
                    }
                }
            }
        }

        Given("프로필 이미지 반영 요청이 실패한다") {
            val parameter = parameter()
            val repository = mockk<UserDataRepository>()
            coEvery { repository.updateProfileImage(uri = any(), format = any(), cropRegion = any(), maxSideLength = any(), quality = any()) } throws IllegalStateException("upload failed")
            val useCase = ChangeProfileImageUseCase(userDataRepository = repository)

            When("고른 사진의 위치와 남길 영역으로 프로필 이미지 반영을 시작한다") {
                val result = useCase(parameter)

                Then("TC-PROFILE-IMAGE-DATA-004 TC-PROFILE-IMAGE-DATA-006 실패가 그대로 전달된다") {
                    result.shouldBeFailure()
                }
            }
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun parameter(): ChangeProfileImageUseCase.Parameter =
            ChangeProfileImageUseCase.Parameter(
                uri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}"),
                cropRegion = fixtureMonkey.imageCropRegion(),
            )
    }
}
