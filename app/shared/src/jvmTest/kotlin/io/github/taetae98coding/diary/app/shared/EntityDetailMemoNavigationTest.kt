package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.AppNavKeySavedStateConfiguration
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceAddNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlin.uuid.Uuid

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class EntityDetailMemoNavigationTest :
    FunSpec({
        test("MemoAdd ScreenNavKey를 저장하고 복원하면 초기 연락처, 장소, 웹 ID가 유지된다") {
            val keyList: List<ScreenNavKey> =
                listOf(
                    MemoAddNavKey(initialContactId = fixtureMonkey.giveMeOne<Uuid>()),
                    MemoAddNavKey(initialPlaceId = fixtureMonkey.giveMeOne<Uuid>()),
                    MemoAddNavKey(initialWebId = fixtureMonkey.giveMeOne<Uuid>()),
                )

            keyList.forEach { key ->
                val savedState =
                    encodeToSavedState(
                        value = key,
                        configuration = AppNavKeySavedStateConfiguration,
                    )

                withClue(key) {
                    decodeFromSavedState<ScreenNavKey>(
                        savedState = savedState,
                        configuration = AppNavKeySavedStateConfiguration,
                    ) shouldBe key
                }
            }
        }

        test("TC-fd0c7f3a-df61-4e6a-925a-44d5e803d546 TC-b1ad2a25-587e-407b-8ec2-272511eb793f TC-9ccf649e-904e-4c51-90b7-e15c2a2456af 넓은 화면에서도 상세 메모 탭에서 이어진 화면에서는 내비게이션을 숨긴다") {
            val id = fixtureMonkey.giveMeOne<Uuid>()
            val moreBackStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key)
            val detailBackStackList =
                listOf(
                    moreBackStack + ContactHomeNavKey + ContactDetailNavKey(id = id) to MemoAddNavKey(initialContactId = id),
                    moreBackStack + PlaceHomeNavKey + PlaceDetailNavKey(id = id) to MemoAddNavKey(initialPlaceId = id),
                    moreBackStack + WebHomeNavKey + WebDetailNavKey(id = id) to MemoAddNavKey(initialWebId = id),
                )
            val backStackCases =
                detailBackStackList.flatMap { (detailBackStack, memoAddKey) ->
                    listOf(
                        detailBackStack + memoAddKey,
                        detailBackStack + MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                    )
                }

            backStackCases.forEach { keys ->
                val appState = createAppState(keys = keys)

                withClue(keys) {
                    appState.isNavigationVisible.shouldBeFalse()
                    appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
                }
            }
        }

        test(
            "TC-e9e30493-4fea-45ac-aa7b-13d43f9f057a TC-b24a6b4a-42c6-41a7-aeac-da02eb2c44e0 TC-0716933d-7223-4e1f-8374-751bae112bab " +
                "넓은 화면에서도 TagDetail의 목록 탭에서 이어진 추가와 상세 화면에서는 내비게이션을 숨긴다",
        ) {
            val tagId = fixtureMonkey.giveMeOne<Uuid>()
            val tagDetailBackStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagDetailNavKey(id = tagId))
            val backStackCases =
                listOf(
                    tagDetailBackStack + MemoAddNavKey(primaryTagId = tagId),
                    tagDetailBackStack + MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                    tagDetailBackStack + WebAddNavKey(initialTagId = tagId),
                    tagDetailBackStack + WebDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                    tagDetailBackStack + PlaceAddNavKey(initialTagId = tagId),
                    tagDetailBackStack + PlaceDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>()),
                )

            backStackCases.forEach { keys ->
                val appState = createAppState(keys = keys)

                withClue(keys) {
                    appState.isNavigationVisible.shouldBeFalse()
                    appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
                }
            }
        }
    })

private fun createAppState(keys: List<ScreenNavKey>): AppState =
    AppState(
        backStack = NavBackStack(*keys.toTypedArray()),
        scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
        reselectEvent = TopLevelReselectEvent(),
        paneScaffoldDirectiveProvider = {
            PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
        },
    )
