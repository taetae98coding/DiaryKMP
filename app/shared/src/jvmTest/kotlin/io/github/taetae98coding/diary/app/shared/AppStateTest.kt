package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import app.cash.turbine.test
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.topLevelNavigationList
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceDetailNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceHomeNavKey
import io.github.taetae98coding.diary.feature.place.api.PlaceMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.routine.api.RoutineAddNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingHomeNavKey
import io.github.taetae98coding.diary.feature.setting.api.SettingMapNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagFinishedListNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.github.taetae98coding.diary.feature.web.api.WebDetailNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebMemoFinishedListNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlin.uuid.Uuid

class AppStateTest :
    FunSpec({
        test("TC-ff5a0d79-ad63-400e-9f38-1d0028dddd3b 주요 목적지 순서") {
            topLevelNavigationList shouldContainExactly
                listOf(
                    TopLevelNavigation.Memo,
                    TopLevelNavigation.Tag,
                    TopLevelNavigation.Calendar,
                    TopLevelNavigation.Routine,
                    TopLevelNavigation.More,
                )
        }

        test("주요 목적지 목록은 모든 목적지를 한 번씩만 담는다") {
            topLevelNavigationList.toSet() shouldBe TopLevelNavigation.entries.toSet()
            topLevelNavigationList.size shouldBe TopLevelNavigation.entries.size
        }

        test("TC-03c304af-90f4-4d6e-98d3-9a7af49b28bf 선택한 주요 목적지로 전환") {
            TopLevelNavigation.entries.forEach { current ->
                TopLevelNavigation.entries
                    .filterNot { destination -> destination == current }
                    .forEach { destination ->
                        val appState = createAppState(current.key)

                        appState.navigateTo(destination)

                        appState.currentTopLevelNavigation shouldBe destination
                        appState.backStack.toList() shouldBe expectedBackStack(destination)
                    }
            }
        }

        test("TC-aeaa51e5-0fb1-4174-8c7e-d63a5d15cb3a 현재 목적지 재선택 시 화면 유지") {
            TopLevelNavigation.entries.forEach { destination ->
                val appState = createAppState(*expectedBackStack(destination).toTypedArray())
                val initialBackStack = appState.backStack.toList()

                appState.navigateTo(destination)

                appState.backStack.toList() shouldBe initialBackStack
                appState.currentTopLevelNavigation shouldBe destination
            }
        }

        test("TC-58d3fd49-552c-4ed0-9ba4-a9b8b6c061a3 상세 자리에 기본으로 놓인 화면은 이어진 화면으로 보지 않는다") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    isListDetailTwoPane = true,
                )
            val initialBackStack = appState.backStack.toList()

            appState.reselectEvent.flowOf(TopLevelNavigation.Memo).test {
                appState.navigateTo(TopLevelNavigation.Memo)

                awaitItem()
                expectNoEvents()
            }

            appState.backStack.toList() shouldBe initialBackStack
        }

        test("TC-da0880d8-c8a2-464e-9647-2892a77fcc97 이어진 화면이 열려 있으면 닫고 돌아오기만 한다") {
            openedScreenCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.reselectEvent.flowOf(case.topLevelNavigation).test {
                    appState.navigateTo(case.topLevelNavigation)

                    expectNoEvents()
                }

                appState.backStack.toList() shouldBe expectedBackStack(case.topLevelNavigation)
            }
        }

        test("TC-68db6c25-ae6e-4f49-a1a3-bea2ee56c335 이어진 화면을 닫은 뒤 다시 선택하면 돌아갈 자리를 알린다") {
            openedScreenCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.navigateTo(case.topLevelNavigation)

                appState.reselectEvent.flowOf(case.topLevelNavigation).test {
                    appState.navigateTo(case.topLevelNavigation)

                    awaitItem()
                    expectNoEvents()
                }
            }
        }

        test("TC-348f39c9-be15-41d2-a3ca-c0647c788fe8 이어진 화면을 닫은 뒤 뒤로가면 기본 목적지로 돌아간다") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                )

            appState.navigateTo(TopLevelNavigation.Memo)
            appState.backStack.removeLast()

            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }

        test("다시 선택은 그 목적지에만 돌아갈 자리를 알린다") {
            val appState = createAppState(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key)

            appState.reselectEvent.flowOf(TopLevelNavigation.Tag).test {
                appState.navigateTo(TopLevelNavigation.Memo)

                expectNoEvents()
            }
        }

        test("TC-f6d1cbd9-d1de-4408-85d5-6fa8bab19721 주요 목적지 이력을 누적하지 않음") {
            val appState = createAppState(TopLevelNavigation.DEFAULT.key)

            appState.navigateTo(TopLevelNavigation.Tag)
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.navigateTo(TopLevelNavigation.More)

            appState.backStack.toList() shouldBe
                listOf(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.More.key,
                )

            appState.backStack.removeLast()

            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }

        test("TC-e499dc91-528f-4d46-a621-c4f9867ec27a 세부 화면 단독 표시 시 내비게이션 숨김") {
            detailDestinationCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-8cbf380d-d0b9-456a-a873-f94e35d5deea 단독 화면에서 뒤로가면 설정 목록으로 돌아가고 공통 내비게이션은 계속 숨긴다") {
            val appState = createAppState(*(expectedBackStack(TopLevelNavigation.More) + SettingHomeNavKey + SettingMapNavKey).toTypedArray())
            appState.isNavigationVisible.shouldBeFalse()

            appState.backStack.removeLast()

            appState.backStack.last() shouldBe SettingHomeNavKey
            appState.isNavigationVisible.shouldBeFalse()
        }

        test("TC-713b65da-f06b-4d3a-8cd4-4fe3d4ceacb3 주요 목적지 복귀 후 내비게이션 표시") {
            detailDestinationCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.backStack.removeLast()

                appState.isNavigationVisible.shouldBeTrue()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-af129482-c473-4ec8-a9df-79c72a9de480 목록과 세부 화면 함께 표시 시 내비게이션 표시") {
            listDetailTwoPaneCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeTrue()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-ba5937cc-b46a-4b4a-91c7-be1019aba587 주요 목적지 위에 겹쳐 표시할 때 내비게이션 표시") {
            overlayDestinationCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.isNavigationVisible.shouldBeTrue()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-ba5937cc-b46a-4b4a-91c7-be1019aba587 세부 화면과 함께 표시할 때 겹쳐 표시해도 내비게이션 표시") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                    MemoHomeFilterNavKey,
                    isListDetailTwoPane = true,
                )

            appState.isNavigationVisible.shouldBeTrue()
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Memo
        }

        test("TC-e499dc91-528f-4d46-a621-c4f9867ec27a 넓은 화면이어도 목록에서 진입하지 않은 세부 화면은 내비게이션 숨김") {
            listDetailStandaloneCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-af129482-c473-4ec8-a9df-79c72a9de480 목록에서 세부 화면을 거듭 열어도 내비게이션 표시") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoAddNavKey(),
                    MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                    isListDetailTwoPane = true,
                )

            appState.isNavigationVisible.shouldBeTrue()
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Memo
        }

        test("세부 화면 위에 겹쳐 표시하면 내비게이션 숨김") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoAddNavKey(),
                    MemoHomeFilterNavKey,
                )

            appState.isNavigationVisible.shouldBeFalse()
        }

        test("넓은 화면이어도 별도 흐름에서는 내비게이션 숨김") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.More.key,
                    LoginHomeNavKey,
                    isListDetailTwoPane = true,
                )

            appState.isNavigationVisible.shouldBeFalse()
        }

        test("TC-2726daad-ef12-4caf-abda-76fc503caecb 완료된 태그 목록에서는 화면 너비와 관계없이 내비게이션 숨김") {
            listOf(false, true).forEach { isListDetailTwoPane ->
                val appState =
                    createAppState(
                        *(expectedBackStack(TopLevelNavigation.Tag) + TagFinishedListNavKey).toTypedArray(),
                        isListDetailTwoPane = isListDetailTwoPane,
                    )

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            }
        }

        test("TC-0c0a2f95-91df-4e18-95b5-6c757937c929 넓은 화면에서 완료된 메모 목록과 상세를 함께 표시해도 내비게이션 숨김") {
            tagMemoFinishedListDetailCases().forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-ed15c61f-9bee-4eb8-a6d7-e8fc1344b35f 넓은 화면에서 항목 완료 메모 목록과 상세를 함께 표시해도 내비게이션 숨김") {
            entityMemoFinishedListDetailCases().forEach { case ->
                listOf(false, true).forEach { isListDetailTwoPane ->
                    val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = isListDetailTwoPane)

                    appState.isNavigationVisible.shouldBeFalse()
                    appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
                }
            }
        }

        test("TC-c2b989d3-b265-474a-80ce-a38516116249 넓은 화면에서 완료된 메모 목록과 상세를 함께 표시해도 내비게이션 숨김") {
            listOf(null, MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())).forEach { detailKey ->
                val backStack = expectedBackStack(TopLevelNavigation.Memo) + listOfNotNull(MemoFinishedListNavKey, detailKey)
                val appState = createAppState(*backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Memo
            }
        }

        test("TC-865708c2-d7af-4adb-b589-574e4e5338e7 넓은 화면에서 완료된 태그 목록과 상세를 함께 표시해도 내비게이션 숨김") {
            listOf(null, TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())).forEach { detailKey ->
                val backStack = expectedBackStack(TopLevelNavigation.Tag) + listOfNotNull(TagFinishedListNavKey, detailKey)
                val appState = createAppState(*backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Tag
            }
        }

        test("TC-94e59c44-53d0-477b-9aa8-e25a33419603 다른 주요 목적지에 다녀오면 떠난 목적지 위에 열어 둔 화면이 닫힌다") {
            leftDestinationCases().forEach { case ->
                TopLevelNavigation.entries
                    .filterNot { other -> other == case.topLevelNavigation }
                    .forEach { other ->
                        val appState = createAppState(*case.backStack.toTypedArray())

                        appState.navigateTo(other)
                        appState.navigateTo(case.topLevelNavigation)

                        appState.backStack.toList() shouldBe expectedBackStack(case.topLevelNavigation)
                        appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
                        appState.assertBackReturnsToDefaultOnly()
                    }
            }
        }

        test("TC-9328fafa-e957-464c-8e76-4622417cda5d 다른 주요 목적지에 다녀오면 상세 영역이 선택 전 상태로 돌아간다") {
            listDetailLeaveCases(
                topLevelNavigation = TopLevelNavigation.Memo,
                detailList =
                    listOf(
                        listOf(MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                        listOf(MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()), MemoAddNavKey()),
                    ),
            ).forEach { case ->
                assertListDetailResetAfterLeaving(case)
            }
        }

        test("TC-8a46b60c-3b91-4955-a4bc-a1c448f56197 다른 주요 목적지에 다녀오면 상세 영역이 선택 전 상태로 돌아간다") {
            listDetailLeaveCases(
                topLevelNavigation = TopLevelNavigation.Tag,
                detailList =
                    listOf(
                        listOf(TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                        listOf(TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()), TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                        listOf(TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()), TagAddNavKey()),
                    ),
            ).forEach { case ->
                assertListDetailResetAfterLeaving(case)
            }
        }

        test("TC-95e41aa3-afd6-416b-aaa0-1f545b00bf82 파일 올리기 알림으로 FileHome이 열린 뒤 메모로 돌아오면 완료된 메모 목록과 상세가 남지 않는다") {
            val backStack =
                expectedBackStack(TopLevelNavigation.Memo) +
                    listOf(MemoFinishedListNavKey, MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()))

            assertFinishedListResetAfterFileHomeDeepLink(topLevelNavigation = TopLevelNavigation.Memo, backStack = backStack)
        }

        test("TC-744d883c-9baf-45e6-9ab5-c1b5b33a4dfa 파일 올리기 알림으로 FileHome이 열린 뒤 태그로 돌아오면 완료된 태그 목록과 상세가 남지 않는다") {
            listOf(
                listOf(TagFinishedListNavKey, TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                listOf(TagFinishedListNavKey, TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()), TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
            ).forEach { finishedList ->
                assertFinishedListResetAfterFileHomeDeepLink(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack = expectedBackStack(TopLevelNavigation.Tag) + finishedList,
                )
            }
        }

        test("TC-f7946f1c-35ac-4069-a912-3fdd1b65f240 기본 목적지 선택 시 이전 목적지 이력 제거") {
            val appState = createAppState(TopLevelNavigation.DEFAULT.key)
            appState.navigateTo(TopLevelNavigation.Memo)
            appState.navigateTo(TopLevelNavigation.Tag)
            appState.backStack.last() shouldBe TopLevelNavigation.Tag.key

            appState.navigateTo(TopLevelNavigation.DEFAULT)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.DEFAULT.key)
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }
    }) {
    public companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun openedScreenCases(): List<DetailDestinationCase> =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Memo.key,
                            MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                        ),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Memo.key,
                            MemoHomeFilterNavKey,
                        ),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Tag.key,
                            TagAddNavKey(),
                        ),
                ),
            )

        private fun detailDestinationCases(): List<DetailDestinationCase> =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key, MemoAddNavKey()),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.More,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key, LoginHomeNavKey),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagAddNavKey()),
                ),
            )

        private fun overlayDestinationCases(): List<DetailDestinationCase> =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Memo.key,
                            MemoHomeFilterNavKey,
                        ),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Calendar,
                    backStack = listOf(TopLevelNavigation.Calendar.key, CalendarHomeFilterNavKey),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Tag.key,
                            TagHomeFilterNavKey,
                        ),
                ),
            )

        private fun listDetailStandaloneCases(): List<DetailDestinationCase> =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Calendar,
                    backStack = listOf(TopLevelNavigation.Calendar.key, MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Calendar,
                    backStack = listOf(TopLevelNavigation.Calendar.key, CalendarHomeFilterNavKey, TagAddNavKey()),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Memo.key,
                            MemoHomeFilterNavKey,
                            TagAddNavKey(),
                        ),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Memo.key,
                            MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                            TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                        ),
                ),
            )

        private fun tagMemoFinishedListDetailCases(): List<DetailDestinationCase> =
            fixtureMonkey.giveMeOne<Uuid>().let { tagId ->
                listOf(
                    null,
                    MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                ).map { detailKey ->
                    DetailDestinationCase(
                        topLevelNavigation = TopLevelNavigation.Tag,
                        backStack =
                            listOfNotNull(
                                TopLevelNavigation.DEFAULT.key,
                                TopLevelNavigation.Tag.key,
                                TagDetailNavKey(tagId),
                                TagMemoFinishedListNavKey(tagId = tagId),
                                detailKey,
                            ),
                    )
                }
            }

        private fun entityMemoFinishedListDetailCases(): List<DetailDestinationCase> {
            val contactId = fixtureMonkey.giveMeOne<Uuid>()
            val placeId = fixtureMonkey.giveMeOne<Uuid>()
            val webId = fixtureMonkey.giveMeOne<Uuid>()
            val entityBackStackList =
                listOf(
                    listOf(ContactHomeNavKey, ContactDetailNavKey(contactId), ContactMemoFinishedListNavKey(contactId = contactId)),
                    listOf(PlaceHomeNavKey, PlaceDetailNavKey(placeId), PlaceMemoFinishedListNavKey(placeId = placeId)),
                    listOf(WebHomeNavKey, WebDetailNavKey(webId), WebMemoFinishedListNavKey(webId = webId)),
                )

            return entityBackStackList.flatMap { entityBackStack ->
                listOf(null, MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())).map { detailKey ->
                    DetailDestinationCase(
                        topLevelNavigation = TopLevelNavigation.More,
                        backStack =
                            listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.More.key) + entityBackStack + listOfNotNull(detailKey),
                    )
                }
            }
        }

        private fun listDetailTwoPaneCases(): List<DetailDestinationCase> =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key, MemoAddNavKey()),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagAddNavKey()),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>())),
                ),
            )

        private fun leftDestinationCases(): List<DetailDestinationCase> =
            listOf(
                TopLevelNavigation.Memo to MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                TopLevelNavigation.Memo to MemoAddNavKey(),
                TopLevelNavigation.Memo to MemoHomeFilterNavKey,
                TopLevelNavigation.Tag to TagDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                TopLevelNavigation.Tag to TagAddNavKey(),
                TopLevelNavigation.Tag to TagHomeFilterNavKey,
                TopLevelNavigation.Calendar to MemoDetailNavKey(fixtureMonkey.giveMeOne<Uuid>()),
                TopLevelNavigation.Calendar to CalendarHomeFilterNavKey,
                TopLevelNavigation.Routine to RoutineAddNavKey,
                TopLevelNavigation.More to LoginHomeNavKey,
                TopLevelNavigation.More to SettingHomeNavKey,
            ).map { (topLevelNavigation, openedKey) ->
                DetailDestinationCase(
                    topLevelNavigation = topLevelNavigation,
                    backStack = expectedBackStack(topLevelNavigation) + openedKey,
                )
            }

        private fun listDetailLeaveCases(
            topLevelNavigation: TopLevelNavigation,
            detailList: List<List<ScreenNavKey>>,
        ): List<ListDetailLeaveCase> =
            detailList.flatMap { detail ->
                TopLevelNavigation.entries
                    .filterNot { other -> other == topLevelNavigation }
                    .map { other ->
                        ListDetailLeaveCase(
                            topLevelNavigation = topLevelNavigation,
                            other = other,
                            backStack = expectedBackStack(topLevelNavigation) + detail,
                        )
                    }
            }

        private fun assertFinishedListResetAfterFileHomeDeepLink(
            topLevelNavigation: TopLevelNavigation,
            backStack: List<ScreenNavKey>,
        ) {
            val appState = createAppState(*backStack.toTypedArray(), isListDetailTwoPane = true)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.More
            appState.navigateTo(topLevelNavigation)

            appState.backStack.toList() shouldBe expectedBackStack(topLevelNavigation)
            appState.currentTopLevelNavigation shouldBe topLevelNavigation
        }

        private fun assertListDetailResetAfterLeaving(case: ListDetailLeaveCase) {
            val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

            appState.navigateTo(case.other)
            appState.navigateTo(case.topLevelNavigation)

            appState.backStack.toList() shouldBe expectedBackStack(case.topLevelNavigation)
            appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            appState.isNavigationVisible.shouldBeTrue()
            appState.assertBackReturnsToDefaultOnly()
        }

        private fun AppState.assertBackReturnsToDefaultOnly() {
            if (backStack.size > 1) {
                backStack.removeLast()
            }

            backStack.toList() shouldBe listOf(TopLevelNavigation.DEFAULT.key)
            currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }

        private fun createAppState(
            vararg keys: ScreenNavKey,
            isListDetailTwoPane: Boolean = false,
        ): AppState =
            AppState(
                backStack = NavBackStack(*keys),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = {
                    if (isListDetailTwoPane) {
                        PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
                    } else {
                        PaneScaffoldDirective.Default
                    }
                },
            )

        private fun expectedBackStack(destination: TopLevelNavigation): List<ScreenNavKey> =
            listOf(TopLevelNavigation.DEFAULT, destination)
                .distinct()
                .map(TopLevelNavigation::key)
    }
}

private data class ListDetailLeaveCase(
    val topLevelNavigation: TopLevelNavigation,
    val other: TopLevelNavigation,
    val backStack: List<ScreenNavKey>,
)

private data class DetailDestinationCase(
    val topLevelNavigation: TopLevelNavigation,
    val backStack: List<ScreenNavKey>,
)
