package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import app.cash.turbine.test
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.topLevelNavigationList
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeFilterNavKey
import io.github.taetae98coding.diary.feature.login.api.LoginHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagAddNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagDetailNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagHomeFilterNavKey
import io.github.taetae98coding.diary.feature.tag.api.TagMemoFinishedListNavKey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlin.uuid.Uuid

class AppStateTest :
    FunSpec({
        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-001 주요 목적지 순서") {
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

        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-003 선택한 주요 목적지로 전환") {
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

        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-004 현재 목적지 재선택 시 화면 유지") {
            TopLevelNavigation.entries.forEach { destination ->
                val appState = createAppState(*expectedBackStack(destination).toTypedArray())
                val initialBackStack = appState.backStack.toList()

                appState.navigateTo(destination)

                appState.backStack.toList() shouldBe initialBackStack
                appState.currentTopLevelNavigation shouldBe destination
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-011 상세 자리에 기본으로 놓인 화면은 이어진 화면으로 보지 않는다") {
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

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-012 이어진 화면이 열려 있으면 닫고 돌아오기만 한다") {
            openedScreenCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.reselectEvent.flowOf(case.topLevelNavigation).test {
                    appState.navigateTo(case.topLevelNavigation)

                    expectNoEvents()
                }

                appState.backStack.toList() shouldBe expectedBackStack(case.topLevelNavigation)
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-013 이어진 화면을 닫은 뒤 다시 선택하면 돌아갈 자리를 알린다") {
            openedScreenCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.navigateTo(case.topLevelNavigation)

                appState.reselectEvent.flowOf(case.topLevelNavigation).test {
                    appState.navigateTo(case.topLevelNavigation)

                    awaitItem()
                    expectNoEvents()
                }
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-012 이어진 화면을 닫은 뒤 뒤로가면 기본 목적지로 돌아간다") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoDetailNavKey(Uuid.random()),
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

        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-005 주요 목적지 이력을 누적하지 않음") {
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

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-004 세부 화면 단독 표시 시 내비게이션 숨김") {
            detailDestinationCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-005 주요 목적지 복귀 후 내비게이션 표시") {
            detailDestinationCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.backStack.removeLast()

                appState.isNavigationVisible.shouldBeTrue()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-007 목록과 세부 화면 함께 표시 시 내비게이션 표시") {
            listDetailTwoPaneCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeTrue()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-008 주요 목적지 위에 겹쳐 표시할 때 내비게이션 표시") {
            overlayDestinationCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray())

                appState.isNavigationVisible.shouldBeTrue()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-008 세부 화면과 함께 표시할 때 겹쳐 표시해도 내비게이션 표시") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoDetailNavKey(Uuid.random()),
                    MemoHomeFilterNavKey,
                    isListDetailTwoPane = true,
                )

            appState.isNavigationVisible.shouldBeTrue()
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.Memo
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-004 넓은 화면이어도 목록에서 진입하지 않은 세부 화면은 내비게이션 숨김") {
            listDetailStandaloneCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-FEATURE-007 목록에서 세부 화면을 거듭 열어도 내비게이션 표시") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.Memo.key,
                    MemoAddNavKey(),
                    MemoDetailNavKey(Uuid.random()),
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

        test("TC-TAG-MEMO-FINISHED-LIST-DETAIL-FEATURE-008 넓은 화면에서 완료된 메모 목록과 상세를 함께 표시해도 내비게이션 숨김") {
            tagMemoFinishedListDetailCases.forEach { case ->
                val appState = createAppState(*case.backStack.toTypedArray(), isListDetailTwoPane = true)

                appState.isNavigationVisible.shouldBeFalse()
                appState.currentTopLevelNavigation shouldBe case.topLevelNavigation
            }
        }

        test("TC-TOP-LEVEL-NAVIGATION-DOMAIN-008 기본 목적지 선택 시 이전 목적지 이력 제거") {
            val appState =
                createAppState(
                    TopLevelNavigation.DEFAULT.key,
                    TopLevelNavigation.More.key,
                    LoginHomeNavKey,
                )

            appState.navigateTo(TopLevelNavigation.DEFAULT)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.DEFAULT.key)
            appState.currentTopLevelNavigation shouldBe TopLevelNavigation.DEFAULT
        }
    }) {
    public companion object {
        private val openedScreenCases =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack =
                        listOf(
                            TopLevelNavigation.DEFAULT.key,
                            TopLevelNavigation.Memo.key,
                            MemoDetailNavKey(Uuid.random()),
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

        private val detailDestinationCases =
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

        private val overlayDestinationCases =
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

        private val listDetailStandaloneCases =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Calendar,
                    backStack = listOf(TopLevelNavigation.Calendar.key, MemoDetailNavKey(Uuid.random())),
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
                            MemoDetailNavKey(Uuid.random()),
                            TagDetailNavKey(Uuid.random()),
                        ),
                ),
            )

        private val tagMemoFinishedListDetailCases =
            Uuid.random().let { tagId ->
                listOf(
                    null,
                    MemoDetailNavKey(Uuid.random()),
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

        private val listDetailTwoPaneCases =
            listOf(
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key, MemoAddNavKey()),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Memo,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(Uuid.random())),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagAddNavKey()),
                ),
                DetailDestinationCase(
                    topLevelNavigation = TopLevelNavigation.Tag,
                    backStack = listOf(TopLevelNavigation.DEFAULT.key, TopLevelNavigation.Tag.key, TagDetailNavKey(Uuid.random())),
                ),
            )

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

private data class DetailDestinationCase(
    val topLevelNavigation: TopLevelNavigation,
    val backStack: List<ScreenNavKey>,
)
