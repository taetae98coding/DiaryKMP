@file:OptIn(DelicateCoilApi::class)

package io.github.taetae98coding.diary.feature.more.ui.profile

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import coil3.ColorImage
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.request.ErrorResult
import coil3.test.FakeImageLoaderEngine
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.core.model.image.ImageCropRegion
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPicker
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProfileImageEditScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setUp() {
        val engine =
            FakeImageLoaderEngine
                .Builder()
                .intercept(LANDSCAPE_URI, ColorImage(color = IMAGE_COLOR, width = LANDSCAPE_WIDTH, height = LANDSCAPE_HEIGHT))
                .intercept(PORTRAIT_URI, ColorImage(color = IMAGE_COLOR, width = LANDSCAPE_HEIGHT, height = LANDSCAPE_WIDTH))
                .intercept(SQUARE_URI, ColorImage(color = IMAGE_COLOR, width = SQUARE_SIZE, height = SQUARE_SIZE))
                .intercept({ data -> data == UNREADABLE_URI }) { chain ->
                    ErrorResult(image = null, request = chain.request, throwable = IllegalStateException("Unreadable photo."))
                }.intercept({ data -> data == LOADING_URI }) { awaitCancellation() }
                .build()

        SingletonImageLoader.setUnsafe(
            ImageLoader
                .Builder(RuntimeEnvironment.getApplication())
                .components { add(engine) }
                .build(),
        )
    }

    @After
    fun tearDown() {
        SingletonImageLoader.reset()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-001 진입하면 사진이 없는 상태로 시작하고 반영을 실행할 수 없다`() {
        setProfileImageEditScreen(viewModel = screenTestViewModel())

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-002 사진 선택을 실행하면 사진 선택 도구를 연다`() {
        val photoPicker = screenTestPhotoPicker()
        setProfileImageEditScreen(viewModel = screenTestViewModel(), photoPicker = photoPicker)

        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { photoPicker.open() }
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-012 사진을 고르면 그 사진이 표시되고 반영을 실행할 수 있다`() {
        setProfileImageEditScreen(viewModel = screenTestViewModel(), photoPicker = screenTestPhotoPicker(LANDSCAPE_URI))

        choosePhoto()
        awaitReady()

        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-014 고른 사진을 읽는 동안 불러오는 중을 표시하고 반영을 실행할 수 없다`() {
        setProfileImageEditScreen(viewModel = screenTestViewModel(), photoPicker = screenTestPhotoPicker(LOADING_URI))

        choosePhoto()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-003 다른 사진을 고르면 새 사진의 처음 상태로 바뀐다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI, SQUARE_URI))
        choosePhoto()
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput { swipeLeft() }

        choosePhoto()
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val regions = capturedRegions(viewModel = viewModel, uri = FileUri(SQUARE_URI))
        regions.size shouldBe 1
        regions.single() shouldBe ImageCropRegion.FULL
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-004 사진 선택을 취소하면 보고 있던 사진과 영역을 유지한다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI, null))
        choosePhoto()
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput { swipeLeft() }

        choosePhoto()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).assertIsDisplayed()
        val region = capturedRegions(viewModel = viewModel, uri = FileUri(LANDSCAPE_URI)).single()
        region.right shouldBe (1F plusOrMinus TOLERANCE)
        region.width shouldBe (LANDSCAPE_INITIAL_WIDTH plusOrMinus TOLERANCE)
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-013 사진이 없을 때 사진 선택을 취소하면 사진이 없는 상태를 유지한다`() {
        setProfileImageEditScreen(viewModel = screenTestViewModel(), photoPicker = screenTestPhotoPicker(null))

        choosePhoto()

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-005 반영 중에는 진행 상태를 표시하고 다른 조작을 할 수 없다`() {
        val uiState = MutableStateFlow(ProfileImageEditUiState())
        val viewModel = screenTestViewModel(uiState = uiState)
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI))
        choosePhoto()
        awaitReady()

        composeRule.runOnIdle { uiState.value = ProfileImageEditUiState(isInProgress = true) }
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).assertIsNotEnabled()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 0) { viewModel.changeProfileImage(uri = any(), cropRegion = any()) }
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-006 반영에 성공하면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val effect = MutableSharedFlow<ProfileImageEditEffect>(extraBufferCapacity = 1)
        val viewModel = screenTestViewModel(effect = effect)
        every { viewModel.changeProfileImage(uri = any(), cropRegion = any()) } answers { effect.tryEmit(ProfileImageEditEffect.ChangeSucceeded) }
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI), navigateUp = { navigateUpCount += 1 })
        choosePhoto()
        awaitReady()

        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-007 반영에 실패하면 화면을 유지하고 실패를 알린다`() {
        var navigateUpCount = 0
        val effect = MutableSharedFlow<ProfileImageEditEffect>(extraBufferCapacity = 1)
        val viewModel = screenTestViewModel(effect = effect)
        every { viewModel.changeProfileImage(uri = any(), cropRegion = any()) } answers { effect.tryEmit(ProfileImageEditEffect.ChangeFailed) }
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI), navigateUp = { navigateUpCount += 1 })
        choosePhoto()
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput { swipeLeft() }

        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 0
        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).assertIsEnabled()

        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val regions = capturedRegions(viewModel = viewModel, uri = FileUri(LANDSCAPE_URI))
        regions.size shouldBe 2
        regions[1] shouldBe regions[0]
        regions[1].right shouldBe (1F plusOrMinus TOLERANCE)
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-008 사진을 읽을 수 없으면 읽기 실패를 알리고 반영을 실행할 수 없다`() {
        setProfileImageEditScreen(viewModel = screenTestViewModel(), photoPicker = screenTestPhotoPicker(UNREADABLE_URI))

        choosePhoto()
        awaitText(DEFAULT_UNREADABLE_MESSAGE)

        composeRule.onNodeWithText(DEFAULT_UNREADABLE_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).assertIsEnabled()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-009 읽을 수 없는 상태에서 읽을 수 있는 사진을 고르면 반영을 실행할 수 있다`() {
        setProfileImageEditScreen(viewModel = screenTestViewModel(), photoPicker = screenTestPhotoPicker(UNREADABLE_URI, LANDSCAPE_URI))
        choosePhoto()
        awaitText(DEFAULT_UNREADABLE_MESSAGE)

        choosePhoto()
        awaitReady()

        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).assertIsDisplayed()
        composeRule.onNodeWithText(DEFAULT_UNREADABLE_MESSAGE).assertDoesNotExist()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-010 뒤로가면 반영하지 않고 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI), navigateUp = { navigateUpCount += 1 })
        choosePhoto()
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput { swipeLeft() }

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
        verify(exactly = 0) { viewModel.changeProfileImage(uri = any(), cropRegion = any()) }
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-FEATURE-011 반영 중에 뒤로가면 결과를 기다리지 않고 돌아간다`() {
        var navigateUpCount = 0
        val uiState = MutableStateFlow(ProfileImageEditUiState())
        val viewModel = screenTestViewModel(uiState = uiState)
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI), navigateUp = { navigateUpCount += 1 })
        choosePhoto()
        awaitReady()
        composeRule.runOnIdle { uiState.value = ProfileImageEditUiState(isInProgress = true) }

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DOMAIN-001 처음 상태의 남길 영역은 사진 가운데의 짧은 변 정사각형이다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(
            viewModel = viewModel,
            photoPicker = screenTestPhotoPicker(*INITIAL_REGION_CASES.map { (uri, _) -> uri }.toTypedArray()),
        )

        INITIAL_REGION_CASES.forEach { (uri, expected) ->
            choosePhoto()
            awaitReady()

            composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
            composeRule.waitForIdle()

            capturedRegions(viewModel = viewModel, uri = FileUri(uri)).single().shouldBeCloseTo(expected)
        }
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DOMAIN-002 사진을 어느 방향으로 밀어도 남길 영역은 사진 안에 머문다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI))
        choosePhoto()
        awaitReady()

        PUSH_CASES.forEach { (isStartDirection, _) ->
            repeat(PUSH_COUNT) {
                composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput {
                    if (isStartDirection) swipeLeft() else swipeRight()
                }
            }
            composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
            composeRule.waitForIdle()
        }

        val regions = capturedRegions(viewModel = viewModel, uri = FileUri(LANDSCAPE_URI))
        regions.size shouldBe PUSH_CASES.size
        regions.zip(PUSH_CASES).forEach { (region, case) -> region.shouldBeCloseTo(case.second) }
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DOMAIN-003 처음 상태보다 작게 줄일 수 없다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI))
        choosePhoto()
        awaitReady()

        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput {
            pinch(
                start0 = Offset(center.x - width * WIDE_PINCH_RATIO, center.y),
                end0 = Offset(center.x - width * NARROW_PINCH_RATIO, center.y),
                start1 = Offset(center.x + width * WIDE_PINCH_RATIO, center.y),
                end1 = Offset(center.x + width * NARROW_PINCH_RATIO, center.y),
            )
        }
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        capturedRegions(viewModel = viewModel, uri = FileUri(LANDSCAPE_URI)).single().shouldBeCloseTo(LANDSCAPE_INITIAL_REGION)
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DOMAIN-004 남길 영역의 한 변은 처음 상태의 5분의 1까지만 줄어든다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI))
        choosePhoto()
        awaitReady()

        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput {
            pinch(
                start0 = Offset(center.x - width * NARROW_PINCH_RATIO, center.y),
                end0 = Offset(center.x - width * WIDE_PINCH_RATIO, center.y),
                start1 = Offset(center.x + width * NARROW_PINCH_RATIO, center.y),
                end1 = Offset(center.x + width * WIDE_PINCH_RATIO, center.y),
            )
        }
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val region = capturedRegions(viewModel = viewModel, uri = FileUri(LANDSCAPE_URI)).single()
        region.width shouldBe (LANDSCAPE_INITIAL_WIDTH / MAX_ZOOM plusOrMinus TOLERANCE)
        region.height shouldBe (1F / MAX_ZOOM plusOrMinus TOLERANCE)
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DOMAIN-006 화면이 재생성되어도 사진과 남길 영역을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        val regions = mutableListOf<ImageCropRegion>()
        var latestState: ProfileImageEditState? = null
        restorationTester.setContent {
            DiaryTheme {
                val state = rememberProfileImageEditState()

                latestState = state
                ProfileImageEditScaffold(
                    onEvent = { event ->
                        if (event is ProfileImageEditScaffoldEvent.ClickApply) {
                            regions += checkNotNull(state.cropRegion())
                        }
                    },
                    state = state,
                )
            }
        }
        composeRule.runOnIdle { checkNotNull(latestState).changePhoto(uri = FileUri(LANDSCAPE_URI)) }
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).performTouchInput { swipeLeft() }
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        awaitReady()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_PHOTO_DESCRIPTION).assertIsDisplayed()
        regions.size shouldBe 2
        regions[1] shouldBe regions[0]
        regions[1] shouldNotBe LANDSCAPE_INITIAL_REGION
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DOMAIN-007 사진을 고르기 전에 화면이 재생성되어도 사진이 없는 상태를 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                ProfileImageEditScaffold(onEvent = {})
            }
        }
        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertIsDisplayed()

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(DEFAULT_EMPTY_MESSAGE).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).assertDoesNotExist()
    }

    @Test
    fun `TC-PROFILE-IMAGE-EDIT-DATA-001 반영을 실행하면 사진의 위치와 비율로 표현한 남길 영역으로 프로필 이미지를 바꾸도록 요청한다`() {
        val viewModel = screenTestViewModel()
        setProfileImageEditScreen(viewModel = viewModel, photoPicker = screenTestPhotoPicker(LANDSCAPE_URI))
        choosePhoto()
        awaitReady()

        composeRule.onNodeWithContentDescription(DEFAULT_APPLY_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        val regions = capturedRegions(viewModel = viewModel, uri = FileUri(LANDSCAPE_URI))
        regions.size shouldBe 1
        regions.single().shouldBeCloseTo(LANDSCAPE_INITIAL_REGION)
    }

    private fun setProfileImageEditScreen(
        viewModel: ProfileImageEditViewModel,
        photoPicker: PhotoPicker = screenTestPhotoPicker(),
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                ProfileImageEditScreen(
                    navigateUp = navigateUp,
                    photoPicker = photoPicker,
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun choosePhoto() {
        composeRule.onNodeWithText(DEFAULT_CHOOSE_PHOTO_LABEL).performClick()
        composeRule.waitForIdle()
    }

    private fun awaitReady() {
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithContentDescription(DEFAULT_APPLY_DESCRIPTION).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun awaitText(text: String) {
        composeRule.waitUntil(timeoutMillis = LOAD_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    public companion object {
        private const val LOAD_TIMEOUT_MILLIS = 5_000L
        private const val TOLERANCE = 0.001F
        private const val MAX_ZOOM = 5F
        private const val WIDE_PINCH_RATIO = 0.45F
        private const val NARROW_PINCH_RATIO = 0.05F
        private const val PUSH_COUNT = 2
        private const val IMAGE_COLOR = 0xFF3366CC.toInt()
        private const val LANDSCAPE_WIDTH = 400
        private const val LANDSCAPE_HEIGHT = 200
        private const val SQUARE_SIZE = 300
        private const val LANDSCAPE_INITIAL_WIDTH = 0.5F
        private const val LANDSCAPE_URI = "content://media/external/images/media/1"
        private const val PORTRAIT_URI = "content://media/external/images/media/2"
        private const val SQUARE_URI = "content://media/external/images/media/3"
        private const val UNREADABLE_URI = "content://media/external/images/media/4"
        private const val LOADING_URI = "content://media/external/images/media/5"
        private const val DEFAULT_PHOTO_DESCRIPTION = "Photo being edited"
        private const val DEFAULT_APPLY_DESCRIPTION = "Done"
        private const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        private const val DEFAULT_CHOOSE_PHOTO_LABEL = "Choose photo"
        private const val DEFAULT_EMPTY_MESSAGE = "Choose a photo to get started."
        private const val DEFAULT_UNREADABLE_MESSAGE = "Couldn't open the photo."
        private const val DEFAULT_FAILED_MESSAGE = "Couldn't update the profile photo."
        private val LANDSCAPE_INITIAL_REGION = ImageCropRegion(left = 0.25F, top = 0F, right = 0.75F, bottom = 1F)
        private val INITIAL_REGION_CASES =
            listOf(
                LANDSCAPE_URI to LANDSCAPE_INITIAL_REGION,
                PORTRAIT_URI to ImageCropRegion(left = 0F, top = 0.25F, right = 1F, bottom = 0.75F),
                SQUARE_URI to ImageCropRegion.FULL,
            )
        private val PUSH_CASES =
            listOf(
                true to ImageCropRegion(left = 0.5F, top = 0F, right = 1F, bottom = 1F),
                false to ImageCropRegion(left = 0F, top = 0F, right = 0.5F, bottom = 1F),
            )

        private fun ImageCropRegion.shouldBeCloseTo(expected: ImageCropRegion) {
            left shouldBe (expected.left plusOrMinus TOLERANCE)
            top shouldBe (expected.top plusOrMinus TOLERANCE)
            right shouldBe (expected.right plusOrMinus TOLERANCE)
            bottom shouldBe (expected.bottom plusOrMinus TOLERANCE)
        }

        private fun screenTestViewModel(
            uiState: MutableStateFlow<ProfileImageEditUiState> = MutableStateFlow(ProfileImageEditUiState()),
            effect: Flow<ProfileImageEditEffect> = emptyFlow(),
        ): ProfileImageEditViewModel {
            val viewModel = mockk<ProfileImageEditViewModel>()
            every { viewModel.uiState } returns uiState
            every { viewModel.effect } returns effect
            every { viewModel.changeProfileImage(uri = any(), cropRegion = any()) } returns Unit
            return viewModel
        }

        // 사진 선택 도구는 열 때마다 순서대로 다음 값을 돌려주고, 마지막 값 뒤에는 그 값을 반복한다.
        private fun screenTestPhotoPicker(vararg pickedUris: String?): PhotoPicker {
            val photoPicker = mockk<PhotoPicker>()
            val results = pickedUris.map { uri -> uri?.let(::FileUri) }.ifEmpty { listOf(null) }
            coEvery { photoPicker.open() } returnsMany results andThen results.last()
            return photoPicker
        }

        private fun capturedRegions(
            viewModel: ProfileImageEditViewModel,
            uri: FileUri,
        ): List<ImageCropRegion> {
            val regions = mutableListOf<ImageCropRegion>()
            verify { viewModel.changeProfileImage(uri = uri, cropRegion = capture(regions)) }
            return regions
        }
    }
}
