package io.github.taetae98coding.diary.feature.playlist.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1200dp")
class MusicAddScreenRetentionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-c1c4e40e-ab7f-48ee-aacc-6d65f93511c5 화면이 재생성되어도 작성 중이던 내용을 유지한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            DiaryTheme {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    viewModel = fetchEffectViewModel(fetchedEffect()),
                )
            }
        }
        composeRule.fillAllInput()
        composeRule.clickFetch()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.titleInput().assert(hasText(FETCHED_TITLE))
        composeRule.artistInput().assert(hasText(FETCHED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.thumbnailPreviewCount() shouldBe 1
    }

    @Test
    fun `TC-dec657af-b1e3-4fea-a2b4-bc62fe0cee25 메모리 정리 뒤 복원해도 작성 중이던 내용을 모두 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            // 메모리 정리 뒤에는 화면 상태를 들고 있던 객체도 새로 만들어지므로 복원할 때마다 새 인스턴스를 쓴다.
            val viewModel = remember { screenTestViewModel() }

            DiaryTheme {
                MusicAddScreen(
                    navigateUp = {},
                    componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.fillAllInput()

        restorationTester.emulateSavedInstanceStateRestore()
        composeRule.waitForIdle()

        composeRule.titleInput().assert(hasText(TYPED_TITLE))
        composeRule.artistInput().assert(hasText(TYPED_ARTIST))
        composeRule.linkInput().assert(hasText(TYPED_LINK))
        composeRule.thumbnailPreviewCount() shouldBe 1
    }

    @Test
    fun `TC-09f2f4b8-425d-447d-8551-809b35bb8ad5 앱을 다시 실행해 진입하면 새 입력 상태로 시작한다`() {
        var launchCount by mutableIntStateOf(0)
        val viewModelList = mutableListOf<MusicAddViewModel>()
        // 앱을 다시 실행하면 저장해 둔 화면 상태가 없고 화면 상태를 들고 있던 객체도 새로 만들어지므로, 새 키로 저장 상태 없이 새 인스턴스와 함께 화면을 다시 구성한다.
        composeRule.setContent {
            key(launchCount) {
                val viewModel = remember { screenTestViewModel().also(viewModelList::add) }

                DiaryTheme {
                    MusicAddScreen(
                        navigateUp = {},
                        componentVisibleProvider = { MusicAddScaffoldComponentVisible() },
                        viewModel = viewModel,
                    )
                }
            }
        }
        composeRule.fillAllInput()
        composeRule.titleInput().assert(hasText(TYPED_TITLE))

        composeRule.runOnIdle { launchCount += 1 }
        composeRule.waitForIdle()

        viewModelList.size shouldBe 2
        composeRule.titleInput().assert(hasText(""))
        composeRule.artistInput().assert(hasText(""))
        composeRule.linkInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT
        composeRule.thumbnailPreviewCount() shouldBe 0
    }
}
