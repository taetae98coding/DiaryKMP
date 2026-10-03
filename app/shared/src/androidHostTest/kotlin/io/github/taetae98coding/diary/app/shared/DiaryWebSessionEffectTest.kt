package io.github.taetae98coding.diary.app.shared

import androidx.compose.ui.test.junit4.v2.createComposeRule
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.web.DiaryWebSession
import io.github.taetae98coding.diary.compose.web.SingletonDiaryWebSession
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryWebSessionEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @After
    fun resetWebSession() {
        SingletonDiaryWebSession.set(DiaryWebSession())
    }

    @Test
    fun `앱이 받은 세션을 웹 표시 수단이 읽는 세션에 반영한다`() {
        val session = fixtureMonkey.giveMeOne<DiaryWebSession>()

        composeRule.setContent {
            DiaryWebSessionEffect(session = MutableStateFlow(session))
        }

        composeRule.runOnIdle { SingletonDiaryWebSession.get() shouldBe session }
    }

    @Test
    fun `세션이 바뀌면 바뀐 세션을 반영한다`() {
        val initial = fixtureMonkey.giveMeOne<DiaryWebSession>()
        val changed = initial.copy(importCount = initial.importCount + 1)
        val session = MutableStateFlow(initial)

        composeRule.setContent {
            DiaryWebSessionEffect(session = session)
        }
        composeRule.runOnIdle { session.value = changed }

        composeRule.runOnIdle { SingletonDiaryWebSession.get() shouldBe changed }
    }
}
