package io.github.taetae98coding.diary.app.shared

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelNavigation
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.openDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLink
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.file.api.FileAddNavKey
import io.github.taetae98coding.diary.feature.file.api.FileDetailNavKey
import io.github.taetae98coding.diary.feature.file.api.FileHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.feature.qr.api.QrHomeNavKey
import io.github.taetae98coding.diary.feature.timetable.api.CalendarTimetableNavKey
import io.github.taetae98coding.diary.library.fixture.kotlinUuid
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

class AppStateDeepLinkTest :
    FunSpec({
        test("TC-5b734721-c6fe-4d3d-a2bc-e744c156e188 알림을 선택하면 더보기 위에 FileHome 화면을 열고 뒤로가면 더보기가 보인다") {
            listOf(
                listOf(TopLevelNavigation.Calendar.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(Arb.kotlinUuid().next())),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, QrHomeNavKey),
            ).forEach { backStack ->
                val appState = createAppState(*backStack.toTypedArray())

                appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

                appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
                appState.backStack.removeLastOrNull()
                appState.backStack.last() shouldBe TopLevelNavigation.More.key
            }
        }

        test("TC-66288305-8c99-4bde-9280-d147cf3171a6 이미 FileHome 화면을 보고 있으면 새로 열지 않는다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-41f772e5-d9ec-4108-8edc-7af5961a0a26 FileHome에서 연 FileAdd를 보고 있으면 알림을 선택해도 FileAdd를 유지하고 뒤로가면 FileHome이 보인다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileAddNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray())

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe FileHomeNavKey
        }

        test("TC-5b734721-c6fe-4d3d-a2bc-e744c156e188 FileHome 위가 아닌 곳에 열린 FileAdd는 다른 화면처럼 닫고 FileHome을 연다") {
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileAddNavKey)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
        }

        test("TC-323336ff-c39e-4d3b-b447-ba5497a00ee9 FileHome에서 연 FileDetail을 보고 있으면 알림을 선택했을 때 FileDetail을 닫고 FileHome을 연다") {
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, FileDetailNavKey(id = Arb.kotlinUuid().next()))

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-0d02a609-d833-4ec1-9cd1-e33ddbee22e7 목록과 함께 표시한 FileDetail 위에서 연 FileAdd를 보고 있으면 알림을 선택해도 배치를 유지한다") {
            val detail = FileDetailNavKey(id = Arb.kotlinUuid().next())
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, detail, FileAddNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray(), directive = TWO_PANE_DIRECTIVE)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
        }

        test("TC-e9877edd-5af6-4908-8137-589a6bcf6cda 목록과 함께 선택 전 FileAdd가 놓여 있으면 알림을 선택해도 배치를 유지하고 뒤로가면 더보기로 돌아간다") {
            val initialBackStack = listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            val appState = createAppState(*initialBackStack.toTypedArray(), directive = TWO_PANE_DIRECTIVE)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe initialBackStack
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-fc7083f5-9f79-49b4-940a-0a580d483704 목록과 함께 상세 영역에 FileDetail이 놓여 있으면 알림을 선택했을 때 FileDetail을 닫고 선택 전 상태로 FileHome을 연다") {
            val detail = FileDetailNavKey(id = Arb.kotlinUuid().next())
            val appState = createAppState(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey, detail, directive = TWO_PANE_DIRECTIVE)

            appState.openDeepLink(deepLink = ScreenDeepLink.FILE_HOME)

            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey)
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe TopLevelNavigation.More.key
        }

        test("TC-0df323d1-1092-4bfa-867a-4fb90f107490 앱을 새로 시작하며 알림을 선택하면 캘린더 홈 위에 알림 날짜의 하루 시간표가 열린다") {
            val appState = createAppState(TopLevelNavigation.DEFAULT.key)

            appState.openDeepLink(deepLink = NOTIFICATION_TIMETABLE)

            appState.backStack.size shouldBe 2
            appState.backStack.last().shouldBeNotificationTimetable()
            appState.backStack.removeLastOrNull()
            appState.backStack.toList() shouldBe listOf(TopLevelNavigation.Calendar.key)
        }

        test("TC-554765da-1c98-40f1-a6d6-0e11136c1307 보던 화면 위에 알림 날짜의 하루 시간표를 열고 뒤로 가면 보던 화면이 보인다") {
            listOf(
                listOf(TopLevelNavigation.Calendar.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key, MemoDetailNavKey(Arb.kotlinUuid().next())),
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.More.key, FileHomeNavKey),
            ).forEach { initialBackStack ->
                val appState = createAppState(*initialBackStack.toTypedArray())

                appState.openDeepLink(deepLink = NOTIFICATION_TIMETABLE)

                appState.backStack.size shouldBe initialBackStack.size + 1
                appState.backStack.last().shouldBeNotificationTimetable()
                appState.backStack.removeLastOrNull()
                appState.backStack.toList() shouldBe initialBackStack
            }
        }

        test("TC-d31c9578-0aa9-4675-be37-db8984806efc 보던 화면이 시간표면 그 자리에 알림 날짜의 하루 시간표를 연다") {
            listOf(
                listOf(TopLevelNavigation.Calendar.key, CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = NOTIFICATION_DATE)),
                listOf(TopLevelNavigation.Calendar.key, CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = LocalDate(2026, 11, 3))),
                listOf(TopLevelNavigation.Calendar.key, CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.WEEK, date = LocalDate(2026, 11, 1))),
                listOf(
                    TopLevelNavigation.Calendar.key,
                    TopLevelNavigation.Memo.key,
                    CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = LocalDate(2026, 11, 3), id = Arb.kotlinUuid().next()),
                ),
            ).forEach { initialBackStack ->
                val appState = createAppState(*initialBackStack.toTypedArray())

                appState.openDeepLink(deepLink = NOTIFICATION_TIMETABLE)

                val opened = appState.backStack.last()

                appState.backStack.size shouldBe initialBackStack.size
                opened.shouldBeNotificationTimetable()
                opened shouldNotBe initialBackStack.last()
                appState.backStack.removeLastOrNull()
                appState.backStack.toList() shouldBe initialBackStack.dropLast(1)
            }
        }

        test("TC-5e6de620-f0f7-45b4-b09f-5e691e72e39d 시간표 위에 다른 화면이 있으면 그 위에 시간표를 새로 연다") {
            val timetable = CalendarTimetableNavKey(type = CalendarTimetableNavKey.Type.DAY, date = NOTIFICATION_DATE)
            val memoDetail = MemoDetailNavKey(Arb.kotlinUuid().next())
            val appState = createAppState(TopLevelNavigation.Calendar.key, timetable, memoDetail)

            appState.openDeepLink(deepLink = NOTIFICATION_TIMETABLE)

            val opened = appState.backStack.last()

            opened.shouldBeNotificationTimetable()
            opened shouldNotBe timetable
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe memoDetail
            appState.backStack.removeLastOrNull()
            appState.backStack.last() shouldBe timetable
        }

        test("TC-d364dca6-3929-449e-8c9a-5907b5c36030 여는 날짜는 기기의 시간대와 관계없이 알림이 발생한 날이다") {
            val defaultTimeZone = java.util.TimeZone.getDefault()

            try {
                listOf("Asia/Seoul", "America/Los_Angeles").forEach { timeZoneId ->
                    java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(timeZoneId))
                    val appState = createAppState(TopLevelNavigation.Calendar.key)

                    appState.openDeepLink(deepLink = NOTIFICATION_TIMETABLE)

                    appState.backStack.last().shouldBeNotificationTimetable()
                }
            } finally {
                java.util.TimeZone.setDefault(defaultTimeZone)
            }
        }

        test("TC-0c488028-2358-4e3f-8cf9-3f3447bc6ace 열 화면을 알아볼 수 없으면 보던 화면이나 시작 화면을 그대로 둔다") {
            listOf(
                listOf(TopLevelNavigation.Calendar.key, TopLevelNavigation.Memo.key),
                listOf(TopLevelNavigation.DEFAULT.key),
            ).forEach { initialBackStack ->
                listOf(
                    "diary://${Arb.kotlinUuid().next()}",
                    "diary://timetable?type=day&date=${Arb.kotlinUuid().next()}",
                ).forEach { deepLink ->
                    val appState = createAppState(*initialBackStack.toTypedArray())

                    appState.openDeepLink(deepLink = deepLink)

                    appState.backStack.toList() shouldBe initialBackStack
                }
            }
        }
    }) {
    private companion object {
        private val TWO_PANE_DIRECTIVE: PaneScaffoldDirective = PaneScaffoldDirective.Default.copy(maxHorizontalPartitions = 2)
        private val NOTIFICATION_DATE: LocalDate = LocalDate(2026, 11, 4)

        // 서버가 2026년 11월 4일에 발생한 일일 메모 알림에 담아 보내는 주소다.
        private const val NOTIFICATION_TIMETABLE: String = "diary://timetable?type=day&date=2026-11-04"

        private fun createAppState(
            vararg keys: ScreenNavKey,
            directive: PaneScaffoldDirective = PaneScaffoldDirective.Default,
        ): AppState =
            AppState(
                backStack = NavBackStack(*keys),
                scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
                reselectEvent = TopLevelReselectEvent(),
                paneScaffoldDirectiveProvider = { directive },
            )

        private fun ScreenNavKey.shouldBeNotificationTimetable() {
            shouldBeInstanceOf<CalendarTimetableNavKey>()
            type shouldBe CalendarTimetableNavKey.Type.DAY
            date shouldBe NOTIFICATION_DATE
        }
    }
}
