package io.github.taetae98coding.diary.app.shared.share

import androidx.compose.material3.adaptive.layout.PaneScaffoldDirective
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldState
import androidx.navigation3.runtime.NavBackStack
import io.github.taetae98coding.diary.app.shared.AppState
import io.github.taetae98coding.diary.app.shared.navigation.TopLevelReselectEvent
import io.github.taetae98coding.diary.app.shared.navigation.appDeepLinkNavigationList
import io.github.taetae98coding.diary.core.navigation.ScreenDeepLinkRequest
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.core.navigation.open
import io.github.taetae98coding.diary.domain.share.ShareContent
import io.github.taetae98coding.diary.feature.calendar.api.CalendarHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactAddNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.api.isContactListDetailPane
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoHomeNavKey
import io.github.taetae98coding.diary.feature.memo.api.isMemoListDetailPane
import io.github.taetae98coding.diary.feature.more.api.MoreHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.WebAddNavKey
import io.github.taetae98coding.diary.feature.web.api.WebHomeNavKey
import io.github.taetae98coding.diary.feature.web.api.isWebListDetailPane
import io.github.taetae98coding.diary.library.fixture.alphanumericString
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.mockk
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

class ShareAddDeepLinkTest :
    FunSpec({
        val seoul = TimeZone.of("Asia/Seoul")

        fun AppState.share(
            content: ShareContent,
            isLaunch: Boolean,
        ): Boolean {
            val deepLink = shareAddDeepLinkOf(content = content, timeZone = seoul)

            return appDeepLinkNavigationList.open(request = ScreenDeepLinkRequest(deepLink = deepLink, isLaunch = isLaunch), navigator = this)
        }

        test("TC-e03f84ae-cbc3-48b0-8640-084afb8f87dc 앱을 새로 시작하면 그 종류의 목록 화면 위에 공유 Add 화면을 연다") {
            listOf(
                ShareContent.Text(text = "https://example.com") to listOf(MoreHomeNavKey, WebHomeNavKey),
                ShareContent.Text(text = Arb.alphanumericString(prefix = "Memo").next()) to listOf(MemoHomeNavKey),
                ShareContent.Contact(vCard = crlf("BEGIN:VCARD", "FN:${Arb.alphanumericString(prefix = "Name").next()}", "END:VCARD")) to listOf(MoreHomeNavKey, ContactHomeNavKey),
                ShareContent.Event(iCalendar = crlf("BEGIN:VEVENT", "SUMMARY:${Arb.alphanumericString(prefix = "Summary").next()}", "END:VEVENT")) to listOf(MemoHomeNavKey),
            ).forEach { (content, homeList) ->
                withClue(content) {
                    val navigator = createNavigator(CalendarHomeNavKey)
                    val listBackStack = navigator.topLevelBackStackOf(homeList.first()) + homeList.drop(1)

                    navigator.share(content = content, isLaunch = true) shouldBe true

                    navigator.backStack.dropLast(1) shouldBe listBackStack
                    navigator.backStack.last().isStandaloneAddOf(homeList.last()) shouldBe true
                    navigator.backStack.isOnListDetailPane(navigator.backStack.last()) shouldBe false
                }
            }
        }

        test("TC-51d6d123-653c-4b8f-aaf1-4da07dd196fb 보던 화면이 남아 있으면 그 위에 공유 Add 화면을 연다") {
            listOf(
                listOf<ScreenNavKey>(CalendarHomeNavKey),
                listOf(MoreHomeNavKey, WebHomeNavKey),
                listOf(MoreHomeNavKey, WebHomeNavKey, WebAddNavKey()),
            ).forEach { before ->
                withClue(before) {
                    val navigator = createNavigator(*before.toTypedArray())

                    navigator.share(content = ShareContent.Text(text = "https://example.com"), isLaunch = false)

                    navigator.backStack.toList() shouldBe before + WebAddNavKey(initialUrl = "https://example.com", isStandalone = true)
                    navigator.backStack.isOnListDetailPane(navigator.backStack.last()) shouldBe false
                }
            }
        }

        test("TC-0751d47a-5e7c-41fb-8532-14f9dfd6fc32 작성 중인 추가 화면 위에 새 추가 화면을 연다") {
            val navigator = createNavigator(MemoHomeNavKey, MemoAddNavKey())

            val title = Arb.alphanumericString(prefix = "Title").next()

            navigator.share(content = ShareContent.Text(text = title), isLaunch = false)

            navigator.backStack.toList() shouldBe listOf(MemoHomeNavKey, MemoAddNavKey(), MemoAddNavKey(initialTitle = title, isStandalone = true))
        }

        test("채운 값은 주소를 거쳐 화면 키에 그대로 담긴다") {
            listOf(
                ShareContent.Text(text = "https://a.com/?q=1+2&r=%", subject = "제목 +") to
                    WebAddNavKey(initialTitle = "제목 +", initialUrl = "https://a.com/?q=1+2&r=%", isStandalone = true),
                ShareContent.Text(text = "장보기\n우유") to MemoAddNavKey(initialTitle = "장보기", initialDescription = "우유", isStandalone = true),
                ShareContent.Event(iCalendar = crlf("BEGIN:VEVENT", "SUMMARY:여행", "DTSTART;VALUE=DATE:20261120", "DTEND;VALUE=DATE:20261122", "END:VEVENT")) to
                    MemoAddNavKey(
                        initialTitle = "여행",
                        initialDateRange = MemoAddNavKey.InitialDateRange(start = LocalDate(2026, 11, 20), endInclusive = LocalDate(2026, 11, 21)),
                        isStandalone = true,
                    ),
                ShareContent.Event(iCalendar = crlf("BEGIN:VEVENT", "SUMMARY:회의", "DTSTART:20261120T010000Z", "DTEND:20261120T023000Z", "END:VEVENT")) to
                    MemoAddNavKey(
                        initialTitle = "회의",
                        initialDateTimeRange = MemoAddNavKey.InitialDateTimeRange(start = LocalDateTime(2026, 11, 20, 10, 0), endInclusive = LocalDateTime(2026, 11, 20, 11, 30)),
                        isStandalone = true,
                    ),
                ShareContent.Contact(vCard = crlf("BEGIN:VCARD", "FN:김철수", "TEL:010-1", "TEL:+82 2", "BDAY:1990-03-15", "EMAIL:a@b.com", "END:VCARD")) to
                    ContactAddNavKey(
                        initialName = "김철수",
                        initialDescription = "이메일: a@b.com",
                        initialPhoneNumberList = listOf("010-1", "+82 2"),
                        initialBirthday = LocalDate(1990, 3, 15),
                        isStandalone = true,
                    ),
            ).forEach { (content, expected) ->
                withClue(content) {
                    val navigator = createNavigator(CalendarHomeNavKey)

                    navigator.share(content = content, isLaunch = false) shouldBe true

                    navigator.backStack.last() shouldBe expected
                }
            }
        }

        test("TC-f49a958d-1e39-4a0c-afba-4bb224e063da 공유 Add 화면은 태그를 고르지 않은 상태로 연다") {
            listOf(
                ShareContent.Text(text = "https://example.com"),
                ShareContent.Contact(vCard = crlf("BEGIN:VCARD", "FN:${Arb.alphanumericString(prefix = "Name").next()}", "END:VCARD")),
            ).forEach { content ->
                withClue(content) {
                    val navigator = createNavigator(CalendarHomeNavKey)

                    navigator.share(content = content, isLaunch = false)

                    when (val key = navigator.backStack.last()) {
                        is WebAddNavKey -> key.initialTagId shouldBe null
                        is ContactAddNavKey -> key.initialTagId shouldBe null
                        else -> error("공유 Add 화면이 아니다: $key")
                    }
                }
            }
        }

        test("공유 확장이 넘긴 원문 주소를 공유 내용으로 읽는다") {
            shareContentOf("diary://share?kind=text&text=https%3A%2F%2Fa.com%2F%3Fq%3D1%2B2&subject=%EC%A0%9C%EB%AA%A9") shouldBe
                ShareContent.Text(text = "https://a.com/?q=1+2", subject = "제목")
            shareContentOf("diary://share?kind=contact&text=BEGIN%3AVCARD") shouldBe ShareContent.Contact(vCard = "BEGIN:VCARD")
            shareContentOf("diary://share?kind=event&text=BEGIN%3AVEVENT") shouldBe ShareContent.Event(iCalendar = "BEGIN:VEVENT")
            shareContentOf("diary://share?kind=qr&text=a") shouldBe null
            shareContentOf("diary://share?kind=unknown&text=a") shouldBe null
            shareContentOf("diary://memo-detail?id=a") shouldBe null
        }
    })

private fun ScreenNavKey.isStandaloneAddOf(homeKey: ScreenNavKey): Boolean =
    when (homeKey) {
        WebHomeNavKey -> this is WebAddNavKey && isStandalone
        MemoHomeNavKey -> this is MemoAddNavKey && isStandalone
        ContactHomeNavKey -> this is ContactAddNavKey && isStandalone
        else -> false
    }

private fun List<ScreenNavKey>.isOnListDetailPane(key: ScreenNavKey): Boolean = isWebListDetailPane(key) || isMemoListDetailPane(key) || isContactListDetailPane(key)

private fun crlf(vararg lineList: String): String = lineList.joinToString(separator = "\r\n")

private fun createNavigator(vararg keyList: ScreenNavKey): AppState =
    AppState(
        backStack = NavBackStack(*keyList),
        scaffoldState = mockk<NavigationSuiteScaffoldState>(relaxed = true),
        reselectEvent = TopLevelReselectEvent(),
        paneScaffoldDirectiveProvider = { PaneScaffoldDirective.Default },
    )
