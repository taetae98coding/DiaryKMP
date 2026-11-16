package io.github.taetae98coding.diary.app.shared.web

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.github.taetae98coding.diary.app.shared.diaryWebSession
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.LocalDiaryWebSession
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProvideDiaryWebSessionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `앱이 받은 세션을 웹 표시 수단이 읽는 세션으로 제공한다`() {
        val session = Arb.diaryWebSession().next()
        var provided: DiaryWebSession? = null

        composeRule.setContent {
            ProvideDiaryWebSession(session = MutableStateFlow(session)) {
                provided = LocalDiaryWebSession.current
            }
        }

        composeRule.runOnIdle { provided shouldBe session }
    }

    @Test
    fun `세션이 바뀌면 바뀐 세션을 제공한다`() {
        val initial = Arb.diaryWebSession().next()
        val changed = initial.copy(isRestoring = !initial.isRestoring)
        val session = MutableStateFlow(initial)
        var provided: DiaryWebSession? = null

        composeRule.setContent {
            ProvideDiaryWebSession(session = session) {
                provided = LocalDiaryWebSession.current
            }
        }
        composeRule.runOnIdle { session.value = changed }

        composeRule.runOnIdle { provided shouldBe changed }
    }
}
