package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.playlist.api.MusicDetailNavKey
import io.github.taetae98coding.diary.feature.playlist.api.PlaylistHomeNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingGeminiNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldContainExactly
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

// 앱을 다시 실행하면 저장해 둔 화면 상태가 없으므로, 새 키로 저장 상태 없이 앱 상태를 다시 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1000dp-h800dp")
class AppRelaunchListDetailTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-63890756-9fd5-496a-b0d4-c326d3635b72 앱을 종료하고 다시 실행하면 완료된 메모 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailSelectionReset(
            topLevelNavigation = TopLevelNavigation.Memo,
            listPathKeyList = listOf(MemoFinishedListNavKey),
            detailNavKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    @Test
    fun `TC-5687ba74-d1fe-4484-9649-1d4def89f466 앱을 종료하고 다시 실행하면 완료된 태그 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailSelectionReset(
            topLevelNavigation = TopLevelNavigation.Tag,
            listPathKeyList = listOf(TagFinishedListNavKey),
            detailNavKey = TagDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    @Test
    fun `TC-ec63217f-2237-4f2a-bb43-9b5ebef7c6f9 앱을 종료하고 다시 실행하면 태그의 완료된 메모 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        val tagId = fixtureMonkey.giveMeOne<Uuid>()

        assertDetailSelectionReset(
            topLevelNavigation = TopLevelNavigation.Tag,
            listPathKeyList = listOf(TagDetailNavKey(id = tagId), TagMemoFinishedListNavKey(tagId = tagId)),
            detailNavKey = MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    @Test
    fun `TC-fb021d5d-b2b9-4322-8b3d-40c2d8d49f23 앱을 종료하고 다시 실행하면 곡 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailSelectionReset(
            topLevelNavigation = TopLevelNavigation.More,
            listPathKeyList = listOf(PlaylistHomeNavKey),
            detailNavKey = MusicDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    @Test
    fun `TC-b97af1a7-68a7-4b3b-b851-41ea78dc4bc2 앱을 종료하고 다시 실행하면 QR 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailSelectionReset(
            topLevelNavigation = TopLevelNavigation.More,
            listPathKeyList = listOf(QrHomeNavKey),
            detailNavKey = QrDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
        )
    }

    @Test
    fun `TC-abd2391e-99ec-4aab-a8dd-e5249eaf8c86 앱을 종료하고 다시 실행하면 설정 목록과 상세 선택 없이 시작 화면에서 시작한다`() {
        assertDetailSelectionReset(
            topLevelNavigation = TopLevelNavigation.More,
            listPathKeyList = listOf(SettingHomeNavKey),
            detailNavKey = SettingGeminiNavKey,
        )
    }

    private fun assertDetailSelectionReset(
        topLevelNavigation: TopLevelNavigation,
        listPathKeyList: List<ScreenNavKey>,
        detailNavKey: ScreenNavKey,
    ) {
        var launchCount by mutableIntStateOf(0)
        lateinit var appState: AppState
        composeRule.setContent {
            key(launchCount) {
                appState = rememberAppState()
            }
        }
        composeRule.runOnIdle {
            appState.navigateTo(topLevelNavigation)
            appState.backStack.addAll(listPathKeyList + detailNavKey)
        }
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, topLevelNavigation.key) + listPathKeyList + detailNavKey
        }

        composeRule.runOnIdle { launchCount += 1 }

        // 다시 진입한 목록의 상세가 선택 전 상태로 놓이는 것은 각 배치의 재진입 케이스가 검증한다.
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key)
        }
    }
}
