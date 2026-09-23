package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

private const val TOLERANCE = 0.0001F
private val FrameSize = Size(width = 300F, height = 300F)

class ProfileImageEditStateTest :
    FunSpec({
        test("TC-PROFILE-IMAGE-EDIT-DOMAIN-001 처음 상태의 남길 영역은 사진 가운데의 짧은 변 정사각형이다") {
            listOf(
                Triple(400, 200, ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F)),
                Triple(200, 400, ImageCropRegion(left = 0F, top = 0.25F, right = 1F, bottom = 0.75F)),
                Triple(300, 300, ImageCropRegion.FULL),
            ).forEach { (width, height, expected) ->
                val state = readyState(width = width, height = height)

                state.cropRegion().shouldNotBeNull().shouldBeCloseTo(expected)
            }
        }

        test("TC-PROFILE-IMAGE-EDIT-DOMAIN-002 사진을 어느 방향으로 밀어도 남길 영역은 사진 안에 머문다") {
            listOf(
                Offset(x = -FrameSize.width * 3, y = 0F) to ImageCropRegion(left = 0.5F, top = 0F, right = 1F, bottom = 1F),
                Offset(x = FrameSize.width * 3, y = 0F) to ImageCropRegion(left = 0F, top = 0F, right = 0.5F, bottom = 1F),
                Offset(x = 0F, y = -FrameSize.height * 3) to ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F),
            ).forEach { (pan, expected) ->
                val state = readyState(width = 400, height = 200)

                state.transform(pan = pan, zoomChange = 1F, centroid = FrameSize.center(), frameSize = FrameSize)

                state.cropRegion().shouldNotBeNull().shouldBeCloseTo(expected)
            }
        }

        test("TC-PROFILE-IMAGE-EDIT-DOMAIN-003 처음 상태보다 작게 줄일 수 없다") {
            val state = readyState(width = 400, height = 200)

            state.zoomBy(factor = 0.2F, focal = FrameSize.center(), frameSize = FrameSize)

            state.zoom shouldBe ProfileImageEditState.MIN_ZOOM
            state.cropRegion().shouldNotBeNull().shouldBeCloseTo(ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F))
        }

        test("TC-PROFILE-IMAGE-EDIT-DOMAIN-004 남길 영역의 한 변은 처음 상태의 5분의 1까지만 줄어든다") {
            val state = readyState(width = 400, height = 200)

            state.zoomBy(factor = 100F, focal = FrameSize.center(), frameSize = FrameSize)

            state.zoom shouldBe ProfileImageEditState.MAX_ZOOM
            val region = state.cropRegion().shouldNotBeNull()
            region.width shouldBe (0.5F / ProfileImageEditState.MAX_ZOOM plusOrMinus TOLERANCE)
            region.height shouldBe (1F / ProfileImageEditState.MAX_ZOOM plusOrMinus TOLERANCE)
        }

        test("초점 아래의 사진 지점은 확대한 뒤에도 같은 자리에 남는다") {
            val state = readyState(width = 400, height = 400)
            val focal = Offset(x = FrameSize.width * 0.25F, y = FrameSize.height * 0.25F)
            val before = state.cropRegion().shouldNotBeNull()
            val pointX = before.left + 0.25F * before.width
            val pointY = before.top + 0.25F * before.height

            state.zoomBy(factor = 2F, focal = focal, frameSize = FrameSize)

            val after = state.cropRegion().shouldNotBeNull()
            after.width shouldBe (0.5F plusOrMinus TOLERANCE)
            after.left + 0.25F * after.width shouldBe (pointX plusOrMinus TOLERANCE)
            after.top + 0.25F * after.height shouldBe (pointY plusOrMinus TOLERANCE)
        }

        test("TC-PROFILE-IMAGE-EDIT-FEATURE-001 사진을 고르기 전에는 사진이 없고 반영할 영역이 없다") {
            val state = ProfileImageEditState()

            state.uri.shouldBeNull()
            state.photo shouldBe ProfileImageEditPhoto.Empty
            state.isReady shouldBe false
            state.cropRegion().shouldBeNull()
        }

        test("TC-PROFILE-IMAGE-EDIT-FEATURE-012 사진을 고르면 불러오는 상태가 되고 읽으면 처음 상태의 영역이 잡힌다") {
            val state = ProfileImageEditState()
            val uri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}")

            state.changePhoto(uri = uri)

            state.uri shouldBe uri
            state.photo shouldBe ProfileImageEditPhoto.Loading

            state.onPhotoLoaded(width = 400, height = 200)

            state.cropRegion().shouldNotBeNull().shouldBeCloseTo(ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F))
        }

        test("TC-PROFILE-IMAGE-EDIT-FEATURE-003 다른 사진으로 바꾸면 불러오는 상태로 돌아가고 영역이 처음 상태로 돌아간다") {
            val state = readyState(width = 400, height = 200)
            state.transform(pan = Offset(x = -FrameSize.width, y = 0F), zoomChange = 2F, centroid = FrameSize.center(), frameSize = FrameSize)
            val newUri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}")

            state.changePhoto(uri = newUri)

            state.uri shouldBe newUri
            state.photo shouldBe ProfileImageEditPhoto.Loading
            state.isReady shouldBe false
            state.cropRegion().shouldBeNull()
            state.zoom shouldBe ProfileImageEditState.MIN_ZOOM
            state.centerX shouldBe 0.5F
            state.centerY shouldBe 0.5F
        }

        test("사진을 읽지 못하면 반영할 영역이 없다") {
            val state = ProfileImageEditState(initialUri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}"))

            state.onPhotoUnreadable()

            state.photo shouldBe ProfileImageEditPhoto.Unreadable
            state.cropRegion().shouldBeNull()
        }

        test("사진을 읽기 전에는 조작이 상태를 바꾸지 않는다") {
            val state = ProfileImageEditState(initialUri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}"))

            state.transform(pan = Offset(x = 50F, y = 50F), zoomChange = 2F, centroid = FrameSize.center(), frameSize = FrameSize)
            state.zoomBy(factor = 2F, focal = FrameSize.center(), frameSize = FrameSize)

            state.zoom shouldBe ProfileImageEditState.MIN_ZOOM
            state.centerX shouldBe 0.5F
            state.centerY shouldBe 0.5F
        }

        test("TC-PROFILE-IMAGE-EDIT-DOMAIN-007 사진을 고르기 전에 저장한 값으로 복원하면 사진이 없는 상태다") {
            val state = ProfileImageEditState()

            val saved = with(ProfileImageEditState.Saver) { SaverScope { true }.save(state) }.shouldNotBeNull()
            val restored = ProfileImageEditState.Saver.restore(saved).shouldNotBeNull()

            restored.uri.shouldBeNull()
            restored.photo shouldBe ProfileImageEditPhoto.Empty
        }

        test("저장한 값으로 복원하면 사진과 확대 배율, 중심이 같다") {
            val state = readyState(width = 400, height = 200)
            state.transform(pan = Offset(x = -40F, y = 0F), zoomChange = 2F, centroid = FrameSize.center(), frameSize = FrameSize)

            val saved = with(ProfileImageEditState.Saver) { SaverScope { true }.save(state) }.shouldNotBeNull()
            val restored = ProfileImageEditState.Saver.restore(saved).shouldNotBeNull()

            restored.uri shouldBe state.uri
            restored.zoom shouldBe state.zoom
            restored.centerX shouldBe state.centerX
            restored.centerY shouldBe state.centerY
            restored.photo shouldBe ProfileImageEditPhoto.Loading
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey =
            diaryFixtureMonkey()

        private fun readyState(
            width: Int,
            height: Int,
        ): ProfileImageEditState =
            ProfileImageEditState(initialUri = FileUri("content://photo/${fixtureMonkey.giveMeOne<String>()}")).apply {
                onPhotoLoaded(width = width, height = height)
            }

        private fun Size.center(): Offset = Offset(x = width / 2, y = height / 2)

        private fun ImageCropRegion.shouldBeCloseTo(expected: ImageCropRegion) {
            left shouldBe (expected.left plusOrMinus TOLERANCE)
            top shouldBe (expected.top plusOrMinus TOLERANCE)
            right shouldBe (expected.right plusOrMinus TOLERANCE)
            bottom shouldBe (expected.bottom plusOrMinus TOLERANCE)
        }
    }
}
