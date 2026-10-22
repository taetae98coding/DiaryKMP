package io.github.taetae98coding.diary.app.shared

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.testing.TestLifecycleOwner
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
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

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RememberAppStateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-a1456939-ef2d-44a8-b187-db9eccce49e9 앱을 캘린더 목적지에서 시작한다`() {
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
    fun `TC-af129482-c473-4ec8-a9df-79c72a9de480 넓은 창에서 세부 화면과 함께 표시되면 내비게이션을 유지한다`() {
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
    fun `TC-e499dc91-528f-4d46-a621-c4f9867ec27a 좁은 창에서 세부 화면으로 이동하면 내비게이션을 숨긴다`() {
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
    fun `TC-7e17dbec-9193-4e60-b99a-5db2f754b712 화면 재생성 후 목적지와 전환 이력을 복원한다`() {
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
    fun `TC-c7cf0275-7d03-4a99-ad22-fafc877cc5a8 목적지 위에 열려 있던 TagAdd 화면을 화면 재생성 후 복원한다`() {
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
    fun `TC-68464a70-d5a2-4cbb-a393-b24dddda10fd 다른 앱에 다녀와도 보던 목적지와 그 위에 열린 화면이 그대로 남는다`() {
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
    fun `TC-0c98d16c-241e-4b8e-a459-6dd782ce11d4 화면 재생성 후 연락처 목록과 함께 놓인 상세 선택을 유지한다`() {
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
    fun `TC-3702fa15-098d-46a9-a36e-ec1d5b5aa318 시스템이 앱을 정리했다가 다시 만들어도 연락처 목록과 함께 놓인 상세 선택과 뒤로가기 기록을 유지한다`() {
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
    fun `TC-99c06fa7-892e-4ac3-ab5c-f159e79473c6 앱이 백그라운드에 다녀와도 연락처 목록과 함께 놓인 상세 선택을 유지한다`() {
        val detailNavKey = ContactDetailNavKey(id = Uuid.random())
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = listOf(ContactHomeNavKey, detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, ContactHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe ContactHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-cca9e6c2-7793-45a7-88e9-b83b66b8f98c 화면 재생성 후 파일 목록과 함께 놓인 상세 선택을 유지한다`() {
        val detailNavKey = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val restorationTester = StateRestorationTester(composeRule)
        lateinit var appState: AppState
        restorationTester.setContent {
            appState = rememberAppState()
        }
        composeRule.runOnIdle {
            appState.navigateTo(TopLevelNavigation.More)
            appState.backStack.add(FileHomeNavKey)
            appState.backStack.add(detailNavKey)
        }

        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, FileHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe FileHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-d74e781c-d7cb-4d80-a96d-c3c66fa897c7 시스템이 앱을 정리했다가 다시 만들어도 파일 목록과 함께 놓인 상세 선택과 뒤로가기 기록을 유지한다`() {
        val detailNavKey = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = listOf(FileHomeNavKey, detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, FileHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe FileHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6fbc8782-e3e3-4c31-8f30-856af745434f 앱이 백그라운드에 다녀와도 파일 목록과 함께 놓인 상세 선택을 유지한다`() {
        val detailNavKey = FileDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = listOf(FileHomeNavKey, detailNavKey))

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, FileHomeNavKey, detailNavKey)
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe FileHomeNavKey
        }
    }

    @Test
    fun `TC-b5f45006-3374-483a-a120-7ecfef59ed4d 필터를 연 채 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 필터는 열린 채로 남는다`() {
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(TagHomeFilterNavKey))

        composeRule.runOnIdle {
            appState.backStack.last() shouldBe TagHomeFilterNavKey
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TagHomeNavKey
        }
    }

    @Test
    fun `TC-b5f45006-3374-483a-a120-7ecfef59ed4d 필터를 연 채 앱이 백그라운드에 다녀와도 필터는 열린 채로 남는다`() {
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = listOf(TagHomeFilterNavKey))

        composeRule.runOnIdle {
            appState.backStack.last() shouldBe TagHomeFilterNavKey
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe TagHomeNavKey
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-d5f32641-3fc3-4a11-8d45-cc992296b975 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 태그 목록과 함께 놓인 상세 선택과 뒤로가기 기록을 유지한다`() {
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
    fun `TC-d5f32641-3fc3-4a11-8d45-cc992296b975 앱이 백그라운드에 다녀와도 태그 목록과 함께 놓인 상세 선택을 유지한다`() {
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
    fun `TC-9983c108-678b-49fe-b0af-fc7d90664706 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 완료된 메모 목록과 함께 놓인 메모 상세 선택과 뒤로가기 기록을 유지한다`() {
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
    fun `TC-9983c108-678b-49fe-b0af-fc7d90664706 앱이 백그라운드에 다녀와도 완료된 메모 목록과 함께 놓인 메모 상세 선택을 유지한다`() {
        val pathKeyList = tagMemoFinishedListPathKeyList()
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.Tag, pathKeyList = pathKeyList)

        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, TagHomeNavKey) + pathKeyList
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe pathKeyList[1]
        }
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6c520cf1-1bdc-4ac8-8cb7-67e732496b14 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 ContactMemoFinishedList와 함께 놓인 메모 상세 선택과 뒤로가기 기록을 유지한다`() {
        val pathKeyList = entityMemoFinishedListPathKeyListCases()[0]
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = pathKeyList)

        assertEntityMemoFinishedListDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6c520cf1-1bdc-4ac8-8cb7-67e732496b14 앱이 백그라운드에 다녀와도 ContactMemoFinishedList와 함께 놓인 메모 상세 선택을 유지한다`() {
        val pathKeyList = entityMemoFinishedListPathKeyListCases()[0]
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = pathKeyList)

        assertEntityMemoFinishedListDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6c520cf1-1bdc-4ac8-8cb7-67e732496b14 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 PlaceMemoFinishedList와 함께 놓인 메모 상세 선택과 뒤로가기 기록을 유지한다`() {
        val pathKeyList = entityMemoFinishedListPathKeyListCases()[1]
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = pathKeyList)

        assertEntityMemoFinishedListDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6c520cf1-1bdc-4ac8-8cb7-67e732496b14 앱이 백그라운드에 다녀와도 PlaceMemoFinishedList와 함께 놓인 메모 상세 선택을 유지한다`() {
        val pathKeyList = entityMemoFinishedListPathKeyListCases()[1]
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = pathKeyList)

        assertEntityMemoFinishedListDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6c520cf1-1bdc-4ac8-8cb7-67e732496b14 화면이 재생성되거나 시스템이 앱을 정리했다가 다시 만들어도 WebMemoFinishedList와 함께 놓인 메모 상세 선택과 뒤로가기 기록을 유지한다`() {
        val pathKeyList = entityMemoFinishedListPathKeyListCases()[2]
        val appState = restoredAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = pathKeyList)

        assertEntityMemoFinishedListDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    @Test
    @Config(qualifiers = "w1000dp-h800dp")
    fun `TC-6c520cf1-1bdc-4ac8-8cb7-67e732496b14 앱이 백그라운드에 다녀와도 WebMemoFinishedList와 함께 놓인 메모 상세 선택을 유지한다`() {
        val pathKeyList = entityMemoFinishedListPathKeyListCases()[2]
        val appState = backgroundReturnedAppState(topLevelNavigation = TopLevelNavigation.More, pathKeyList = pathKeyList)

        assertEntityMemoFinishedListDetailKept(appState = appState, pathKeyList = pathKeyList)
    }

    private fun assertEntityMemoFinishedListDetailKept(
        appState: AppState,
        pathKeyList: List<ScreenNavKey>,
    ) {
        composeRule.runOnIdle {
            appState.backStack shouldContainExactly listOf(TopLevelNavigation.DEFAULT.key, MoreHomeNavKey) + pathKeyList
            appState.backStack.removeLast()
            appState.backStack.last() shouldBe pathKeyList[2]
        }
    }

    private fun entityMemoFinishedListPathKeyListCases(): List<List<ScreenNavKey>> {
        val contactId = Uuid.random()
        val placeId = Uuid.random()
        val webId = Uuid.random()

        return listOf(
            listOf(ContactHomeNavKey, ContactDetailNavKey(id = contactId), ContactMemoFinishedListNavKey(contactId = contactId), MemoDetailNavKey(id = Uuid.random())),
            listOf(PlaceHomeNavKey, PlaceDetailNavKey(id = placeId), PlaceMemoFinishedListNavKey(placeId = placeId), MemoDetailNavKey(id = Uuid.random())),
            listOf(WebHomeNavKey, WebDetailNavKey(id = webId), WebMemoFinishedListNavKey(webId = webId), MemoDetailNavKey(id = Uuid.random())),
        )
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
    fun `TC-6be9adcd-df35-4fb3-886c-8b2b6762a465 시스템이 앱을 정리한 뒤 다시 만들면 상세 영역의 메모 상세와 뒤로가기 기록이 그대로다`() {
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
