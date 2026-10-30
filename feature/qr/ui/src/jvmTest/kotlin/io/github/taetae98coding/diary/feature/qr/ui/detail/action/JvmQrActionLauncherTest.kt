package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.domain.qr.content.qrActionOf
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldEndWith
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.datetime.TimeZone
import java.awt.Desktop
import java.io.File
import java.io.IOException
import java.net.URI

class JvmQrActionLauncherTest :
    FunSpec({
        lateinit var desktop: Desktop

        beforeTest {
            desktop = mockk(relaxed = true)
            mockkStatic(Desktop::class)
            every { Desktop.isDesktopSupported() } returns true
            every { Desktop.getDesktop() } returns desktop
        }

        afterTest {
            unmockkStatic(Desktop::class)
        }

        test("TC-49054cf5-02d9-450a-a016-b891a6c7120c 데스크톱 앱은 Wi-Fi 연결을 실행 동작으로 제공하지 않는다") {
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            launcher.isSupported(action = fixtureMonkey.giveMeOne<QrAction.JoinWifi>()) shouldBe false
        }

        test("데스크톱 앱은 Wi-Fi 연결 외의 실행 동작을 모두 제공한다") {
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            listOf(
                fixtureMonkey.giveMeOne<QrAction.OpenUrl>(),
                fixtureMonkey.giveMeOne<QrAction.Call>(),
                fixtureMonkey.giveMeOne<QrAction.SendEmail>(),
                fixtureMonkey.giveMeOne<QrAction.SendSms>(),
                fixtureMonkey.giveMeOne<QrAction.ShowLocation>(),
                fixtureMonkey.giveMeOne<QrAction.AddContact>(),
                fixtureMonkey.giveMeOne<QrAction.AddEvent>(),
            ).forEach { action ->
                launcher.isSupported(action = action) shouldBe true
            }
        }

        test("TC-d213833f-476a-42c5-ac57-5615dd97ea57 데스크톱 앱은 주소로 넘기는 실행 동작을 기본 앱의 주소로 넘긴다") {
            listOf(
                "https://example.com" to "https://example.com",
                "tel:010" to "tel:010",
                "mailto:a@example.com" to "mailto:a@example.com",
                "SMSTO:010:hi" to "sms:010?body=hi",
                "geo:37.5665,126.978" to "https://www.google.com/maps/search/?api=1&query=37.566500%2C126.978000",
            ).forEach { (qrValue, expectedUri) ->
                val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

                launcher.launch(action = actionOf(qrValue = qrValue)) shouldBe true

                verify(exactly = 1) { desktop.browse(URI(expectedUri)) }
            }
        }

        test("TC-d213833f-476a-42c5-ac57-5615dd97ea57 데스크톱 앱은 연락처를 QR 값 그대로 담은 연락처 파일로 기본 앱에서 연다") {
            val qrValue = listOf("BEGIN:VCARD", "EMAIL:a@example.com", "END:VCARD").joinToString(separator = "\r\n")
            val openedFile = slot<File>()
            var openedContent = ""
            every { desktop.open(capture(openedFile)) } answers { openedContent = openedFile.captured.readText() }
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            launcher.launch(action = actionOf(qrValue = qrValue)) shouldBe true

            openedFile.captured.name shouldEndWith ".vcf"
            openedContent shouldBe qrValue
        }

        test("TC-d213833f-476a-42c5-ac57-5615dd97ea57 데스크톱 앱은 일정을 일정 파일로 기본 앱에서 연다") {
            val action = actionOf(qrValue = listOf("BEGIN:VEVENT", "SUMMARY:회의", "END:VEVENT").joinToString(separator = "\r\n")) as QrAction.AddEvent
            val openedFile = slot<File>()
            var openedContent = ""
            every { desktop.open(capture(openedFile)) } answers { openedContent = openedFile.captured.readText() }
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            launcher.launch(action = action) shouldBe true

            openedFile.captured.name shouldEndWith ".ics"
            openedContent shouldBe action.calendarFile
        }

        test("TC-a44e8630-1cd2-42f3-990e-01ad0a832c1b 데스크톱 앱에서 웹 주소를 받을 앱이 없으면 넘기지 못한 것으로 본다") {
            every { desktop.browse(any()) } throws IOException()
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            launcher.launch(action = actionOf(qrValue = "https://example.com")) shouldBe false
        }

        test("TC-a44e8630-1cd2-42f3-990e-01ad0a832c1b 데스크톱 앱이 앱 밖 열기를 지원하지 않으면 연락처를 넘기지 못한 것으로 본다") {
            every { desktop.open(any()) } throws UnsupportedOperationException()
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            launcher.launch(action = actionOf(qrValue = listOf("BEGIN:VCARD", "EMAIL:a@example.com", "END:VCARD").joinToString(separator = "\r\n"))) shouldBe false
        }

        test("데스크톱 연동을 쓸 수 없는 환경이면 어떤 실행 동작도 넘기지 못한 것으로 본다") {
            every { Desktop.isDesktopSupported() } returns false
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            listOf(
                "https://example.com",
                listOf("BEGIN:VCARD", "EMAIL:a@example.com", "END:VCARD").joinToString(separator = "\r\n"),
            ).forEach { qrValue ->
                launcher.launch(action = actionOf(qrValue = qrValue)) shouldBe false
            }
            verify(exactly = 0) { Desktop.getDesktop() }
        }

        test("Wi-Fi 연결은 넘기지 않고 넘기지 못한 것으로 본다") {
            val launcher = JvmQrActionLauncher(dispatcher = UnconfinedTestDispatcher())

            launcher.launch(action = fixtureMonkey.giveMeOne<QrAction.JoinWifi>()) shouldBe false

            verify(exactly = 0) { Desktop.getDesktop() }
        }
    }) {
    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private fun actionOf(qrValue: String): QrAction = checkNotNull(qrActionOf(raw = qrValue, timeZone = TimeZone.UTC))
    }
}
