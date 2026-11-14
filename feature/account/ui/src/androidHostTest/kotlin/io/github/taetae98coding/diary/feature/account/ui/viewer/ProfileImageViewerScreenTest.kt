@file:OptIn(DelicateCoilApi::class)

package io.github.taetae98coding.diary.feature.account.ui.viewer

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.TouchInjectionScope
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import coil3.ColorImage
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.DelicateCoilApi
import coil3.request.ErrorResult
import coil3.test.FakeImageLoaderEngine
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.awaitCancellation
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProfileImageViewerScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val requestedImageList = mutableListOf<Any>()

    // 이미지 불러오기 결과를 주소마다 제어하므로, 테스트마다 서로 다른 주소를 만든다.
    private val readyUrl: String = imageUrl()
    private val loadingUrl: String = imageUrl()
    private val errorUrl: String = imageUrl()

    @Before
    fun setUp() {
        val engine =
            FakeImageLoaderEngine
                .Builder()
                .intercept({ data -> requestedImageList.add(data) && false }) { awaitCancellation() }
                .intercept(readyUrl, ColorImage(color = Color.Gray.toArgb(), width = IMAGE_SIZE, height = IMAGE_SIZE))
                .intercept({ data -> data == errorUrl }) { chain ->
                    ErrorResult(image = null, request = chain.request, throwable = IllegalStateException("load failed"))
                }.intercept({ data -> data == loadingUrl }) { awaitCancellation() }
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
    fun `TC-1feffbd6-0df1-40e2-87d6-b5fde7a246a7 진입할 때 받은 프로필 이미지를 불러온다`() {
        setScreen(imageUrl = readyUrl)
        composeRule.waitForIdle()

        requestedImageList shouldBe listOf(readyUrl)
        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-a724a3c7-9cb9-475b-989a-04940800a478 불러오는 중이면 진행 표시만 보인다`() {
        setScreen(imageUrl = loadingUrl)
        composeRule.waitForIdle()

        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-a724a3c7-9cb9-475b-989a-04940800a478 불러오면 이미지만 보인다`() {
        setScreen(imageUrl = readyUrl)
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).assertExists()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertDoesNotExist()
    }

    @Test
    fun `TC-a724a3c7-9cb9-475b-989a-04940800a478 불러오지 못하면 실패 문구만 보인다`() {
        setScreen(imageUrl = errorUrl)
        composeRule.waitForIdle()

        composeRule.onNodeWithText(DEFAULT_FAILED_MESSAGE).assertExists()
        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).assertDoesNotExist()
        composeRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목과 실패 문구를 표시한다`() {
        setScreen(imageUrl = errorUrl)
        composeRule.waitForIdle()

        composeRule.onNodeWithText("프로필 이미지").assertExists()
        composeRule.onNodeWithText("이미지를 불러올 수 없습니다.").assertExists()
        composeRule.onNodeWithContentDescription("뒤로가기").assertExists()
    }

    @Test
    fun `TC-c7dd899a-09ff-420b-8fba-ac6c152efb1d 뒤로가면 이전 화면으로 돌아간다`() {
        var navigateUpCount = 0
        setScreen(imageUrl = readyUrl, navigateUp = { navigateUpCount += 1 })

        composeRule.onNodeWithContentDescription(DEFAULT_NAVIGATE_UP_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateUpCount shouldBe 1
    }

    @Test
    fun `TC-7db52307-a554-4264-8902-45ff770f4bfe 화면이 재생성되어도 같은 이미지를 보여 준다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                ProfileImageViewerScreen(navigateUp = {}, imageUrl = readyUrl)
            }
        }
        composeRule.waitForIdle()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        requestedImageList.distinct() shouldBe listOf(readyUrl)
        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-7db52307-a554-4264-8902-45ff770f4bfe 화면이 재생성되면 배율은 처음 상태로 돌아간다`() {
        lateinit var zoomState: ProfileImageViewerZoomState
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                val state = rememberProfileImageViewerZoomState()

                SideEffect { zoomState = state }
                ProfileImageViewerScaffold(onEvent = {}, imageUrl = readyUrl, zoomState = state)
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).performTouchInput { zoomIn() }
        composeRule.runOnIdle { (zoomState.scale > ProfileImageViewerZoomState.MIN_SCALE) shouldBe true }

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.runOnIdle { zoomState.scale shouldBe ProfileImageViewerZoomState.MIN_SCALE }
        composeRule.onNodeWithContentDescription(DEFAULT_IMAGE_DESCRIPTION).assertExists()
    }

    @Test
    fun `TC-01f222b8-7b37-4983-8aff-712da3961761 불러오는 중이거나 불러오지 못한 동안에는 확대되지 않는다`() {
        val current = mutableStateOf(readyUrl)
        val zoomStateMap = mutableMapOf<String, ProfileImageViewerZoomState>()
        composeRule.setContent {
            DiaryTheme {
                key(current.value) {
                    val state = rememberProfileImageViewerZoomState()

                    SideEffect { zoomStateMap[current.value] = state }
                    ProfileImageViewerScaffold(
                        onEvent = {},
                        imageUrl = current.value,
                        modifier = Modifier.testTag(SCAFFOLD_TAG),
                        zoomState = state,
                    )
                }
            }
        }

        // 같은 조작이 불러온 이미지에서는 확대하는지 먼저 확인한다.
        listOf(readyUrl, loadingUrl, errorUrl).forEach { imageUrl ->
            composeRule.runOnIdle { current.value = imageUrl }
            composeRule.waitForIdle()

            composeRule.onNodeWithTag(SCAFFOLD_TAG).performTouchInput { zoomIn() }
            composeRule.waitForIdle()
        }

        composeRule.runOnIdle {
            (zoomStateMap.getValue(readyUrl).scale > ProfileImageViewerZoomState.MIN_SCALE) shouldBe true
            zoomStateMap.getValue(loadingUrl).scale shouldBe ProfileImageViewerZoomState.MIN_SCALE
            zoomStateMap.getValue(errorUrl).scale shouldBe ProfileImageViewerZoomState.MIN_SCALE
        }
    }

    private fun setScreen(
        imageUrl: String,
        navigateUp: () -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                ProfileImageViewerScreen(navigateUp = navigateUp, imageUrl = imageUrl)
            }
        }
    }

    private companion object {
        const val IMAGE_SIZE = 100
        const val DEFAULT_IMAGE_DESCRIPTION = "Profile image"
        const val DEFAULT_FAILED_MESSAGE = "Couldn't load the image."
        const val DEFAULT_NAVIGATE_UP_DESCRIPTION = "Navigate up"
        const val SCAFFOLD_TAG = "ProfileImageViewerScaffold"
    }
}

// 화면 가운데에서 두 손가락을 벌린다.
private fun TouchInjectionScope.zoomIn() {
    pinch(
        start0 = center - Offset(x = width / 8F, y = 0F),
        end0 = center - Offset(x = width / 3F, y = 0F),
        start1 = center + Offset(x = width / 8F, y = 0F),
        end1 = center + Offset(x = width / 3F, y = 0F),
    )
}

private fun imageUrl(): String = "https://example.com/${Arb.kotlinUuid().next()}.jpg"
