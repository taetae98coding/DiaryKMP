package io.github.taetae98coding.diary.compose.calendar.search

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.platform.UriHandler
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

class CalendarSearchOpenerTest :
    FunSpec({
        test("검색 주소를 열면 안내를 표시하지 않는다") {
            runTest {
                val uriHandler = mockk<UriHandler>(relaxUnitFun = true)
                val snackbarHostState = SnackbarHostState()
                val holidayName = Arb.nonBlankString().next()
                val locationName = Arb.nonBlankString().next()
                val opener = CalendarSearchOpener(uriHandler = uriHandler, snackbarHostState = snackbarHostState, failedMessage = Arb.nonBlankString().next())

                opener.openHolidaySearch(name = holidayName)
                opener.openWeatherSearch(locationName = locationName)

                verify(exactly = 1) { uriHandler.openUri(holidaySearchUri(name = holidayName)) }
                verify(exactly = 1) { uriHandler.openUri(weatherSearchUri(locationName = locationName)) }
                snackbarHostState.currentSnackbarData.shouldBeNull()
            }
        }

        test("검색 주소를 열지 못하면 실패 안내를 표시한다") {
            runTest {
                val uriHandler = mockk<UriHandler>()
                every { uriHandler.openUri(any()) } throws IllegalArgumentException()
                val snackbarHostState = SnackbarHostState()
                val failedMessage = Arb.nonBlankString().next()
                val opener = CalendarSearchOpener(uriHandler = uriHandler, snackbarHostState = snackbarHostState, failedMessage = failedMessage)

                val job = launch { opener.openHolidaySearch(name = Arb.nonBlankString().next()) }
                runCurrent()

                snackbarHostState.currentSnackbarData?.visuals?.message shouldBe failedMessage
                job.cancel()
            }
        }
    })
