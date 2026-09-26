package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RememberAppStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-002 앱을 캘린더 목적지에서 시작한다`() {
        lateinit var appState: AppState
        composeRule.setContent {
            appState = rememberAppState()
        }

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.Calendar.key)
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Calendar
            appState.isNavigationVisible.shouldBeTrue()
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-007 넓은 창에서 세부 화면과 함께 표시되면 내비게이션을 유지한다`() {
        lateinit var appState: AppState
        composeRule.setContent {
            appState = rememberAppState()
        }

        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.backStack.add(MemoAddNavKey())
        }

        composeRule.runOnIdle {
            appState.isNavigationVisible.shouldBeTrue()
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-FEATURE-004 좁은 창에서 세부 화면으로 이동하면 내비게이션을 숨긴다`() {
        lateinit var appState: AppState
        composeRule.setContent {
            appState = rememberAppState()
        }

        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.backStack.add(MemoAddNavKey())
        }

        composeRule.runOnIdle {
            appState.isNavigationVisible.shouldBeFalse()
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-009 화면 재생성 후 목적지와 전환 이력을 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.More)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.More.key,
                )
            appState.backStack.removeLast()
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }
    }

    @Test
    fun `요청 키를 가진 TagAdd 내비게이션 키를 화면 재생성 후 복원한다`() {
        val navKey = TagAddNavKey(requestKey = Uuid.random())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.add(navKey)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Tag.key,
                    navKey,
                )
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-017 목적지 위에 열려 있던 TagAdd 화면을 화면 재생성 후 복원한다`() {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.add(TagAddNavKey())
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Tag.key,
                    TagAddNavKey(),
                )
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            appState.isNavigationVisible.shouldBeFalse()
        }
    }

    @Test
    fun `TC-TOP-LEVEL-NAVIGATION-DOMAIN-018 다른 앱에 다녀와도 보던 목적지와 그 위에 열린 화면이 그대로 남는다`() {
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(TagAddNavKey()))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagAddNavKey())
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            appState.isNavigationVisible.shouldBeFalse()
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TopLevelNavigation.Tag.key
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TopLevelNavigation.DEFAULT.key
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-007 화면 재생성 후 연락처 목록과 함께 놓인 상세 선택을 유지한다`() {
        val detailNavKey = ContactDetailNavKey(id = Uuid.random())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.More)
            appState.backStack.add(ContactHomeNavKey)
            appState.backStack.add(detailNavKey)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.More.key,
                    ContactHomeNavKey,
                    detailNavKey,
                )
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe ContactHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-010 시스템이 앱을 정리했다가 다시 만들어도 연락처 목록과 함께 놓인 상세 선택과 뒤로가기 기록을 유지한다`() {
        val detailNavKey = ContactDetailNavKey(id = Uuid.random())
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = listOf(ContactHomeNavKey, detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, ContactHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe ContactHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-011 앱이 백그라운드에 다녀와도 연락처 목록과 함께 놓인 상세 선택을 유지한다`() {
        val detailNavKey = ContactDetailNavKey(id = Uuid.random())
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = listOf(ContactHomeNavKey, detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, ContactHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe ContactHomeNavKey
        }
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-019 필터를 연 채 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 필터는 열린 채로 남는다`() {
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(TagHomeFilterNavKey))

        composeRule.runOnIdle {
            appState.backStack.last() shouldBe TagHomeFilterNavKey
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TagHomeNavKey
        }
    }

    @Test
    fun `TC-TAG-HOME-DOMAIN-019 필터를 연 채 앱이 백그라운드에 다녀와도 필터는 열린 채로 남는다`() {
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(TagHomeFilterNavKey))

        composeRule.runOnIdle {
            appState.backStack.last() shouldBe TagHomeFilterNavKey
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TagHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-TAG-LIST-DETAIL-DOMAIN-005 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 태그 목록과 함께 놓인 상세 선택과 뒤로가기 기록을 유지한다`() {
        val detailNavKey = TagDetailNavKey(id = Uuid.random())
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TagHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TagHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-TAG-LIST-DETAIL-DOMAIN-005 앱이 백그라운드에 다녀와도 태그 목록과 함께 놓인 상세 선택을 유지한다`() {
        val detailNavKey = TagDetailNavKey(id = Uuid.random())
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TagHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TagHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-DOMAIN-002 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 완료된 메모 목록과 함께 놓인 메모 상세 선택과 뒤로가기 기록을 유지한다`() {
        val pathKeyList = tagMemoFinishedListPathKeyList()
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = pathKeyList)

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TagHomeNavKey) + pathKeyList
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe pathKeyList[1]
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-TAG-MEMO-FINISHED-LIST-DETAIL-DOMAIN-002 앱이 백그라운드에 다녀와도 완료된 메모 목록과 함께 놓인 메모 상세 선택을 유지한다`() {
        val pathKeyList = tagMemoFinishedListPathKeyList()
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = pathKeyList)

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TagHomeNavKey) + pathKeyList
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe pathKeyList[1]
        }
    }

    private fun tagMemoFinishedListPathKeyList(): List<ScreenNavKey> {
        val tagId = Uuid.random()

        return listOf(TagDetailNavKey(id = tagId), TagMemoFinishedListNavKey(tagId = tagId), MemoDetailNavKey(id = Uuid.random()))
    }

    private fun restoredAppState(
        topLevelNavigation: TopLevelNavigation,
        pathKeyList: List<ScreenNavKey>,
    ): AppState {
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(topLevelNavigation)
            appState.backStack.addAll(pathKeyList)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        return appState
    }

    private fun backgroundReturnedAppState(
        topLevelNavigation: TopLevelNavigation,
        pathKeyList: List<ScreenNavKey>,
    ): AppState {
        val lifecycleOwner = TestLifecycleOwner(Lifecycle.State.RESUMED)
        lateinit var appState: AppState
        composeRule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner) {
                appState = rememberAppState()
            }
        }
        composeRule.runOnIdle {
            appState.navigateTo(topLevelNavigation)
            appState.backStack.addAll(pathKeyList)
        }

        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.CREATED }
        composeRule.runOnIdle { lifecycleOwner.currentState = Lifecycle.State.RESUMED }

        return appState
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-MEMO-LIST-DETAIL-DOMAIN-004 시스템이 앱을 정리한 뒤 다시 만들면 상세 영역의 메모 상세와 뒤로가기 기록이 그대로다`() {
        val detailNavKey = MemoDetailNavKey(id = Uuid.random())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.backStack.add(detailNavKey)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    detailNavKey,
                )
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TopLevelNavigation.Memo.key
        }
    }
}
