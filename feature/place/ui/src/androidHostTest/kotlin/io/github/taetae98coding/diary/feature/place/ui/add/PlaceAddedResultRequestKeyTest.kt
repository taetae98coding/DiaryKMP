package io.github.taetae98coding.diary.feature.place.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.navigation3.runtime.result.ResultEffect
import androidx.navigation3.runtime.result.ResultEventBus
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.place.api.PlaceAddedResult
import io.github.taetae98coding.diary.feature.place.api.placeAddedResultKey
import io.github.taetae98coding.diary.feature.place.ui.form.rememberPlaceAddFormState
import io.github.taetae98coding.diary.feature.place.ui.resetAndroidUiDispatcher
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlaceAddedResultRequestKeyTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val effect = Channel<PlaceAddEffect>(capacity = Channel.BUFFERED)

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @Test
    fun `추가에 성공하면 이 화면을 연 요청 키로만 추가한 항목을 돌려준다`() {
        val addedId = Uuid.random()
        val requestKey = Uuid.random()
        val receivedList = setAddedResultEffect(addedResultRequestKey = requestKey, listenedRequestKeyList = listOf(requestKey, Uuid.random()))

        effect.trySend(PlaceAddEffect.AddSucceeded(id = addedId)).getOrThrow()
        composeRule.waitForIdle()

        receivedList shouldBe listOf(requestKey to addedId)
    }

    @Test
    fun `요청 키 없이 연 화면은 추가한 항목을 돌려주지 않는다`() {
        val receivedList = setAddedResultEffect(addedResultRequestKey = null, listenedRequestKeyList = listOf(Uuid.random()))

        effect.trySend(PlaceAddEffect.AddSucceeded(id = Uuid.random())).getOrThrow()
        composeRule.waitForIdle()

        receivedList.shouldBeEmpty()
    }

    private fun setAddedResultEffect(
        addedResultRequestKey: Uuid?,
        listenedRequestKeyList: List<Uuid>,
    ): List<Pair<Uuid, Uuid>> {
        val resultEventBus = ResultEventBus()
        val receivedList = mutableListOf<Pair<Uuid, Uuid>>()
        composeRule.setContent {
            DiaryTheme {
                PlaceAddScreenEffect(
                    addedResultRequestKey = addedResultRequestKey,
                    effect = effect.receiveAsFlow(),
                    scaffoldState = rememberPlaceAddFormState(),
                    resultEventBus = resultEventBus,
                )
                listenedRequestKeyList.forEach { requestKey ->
                    AddedResultListener(requestKey = requestKey, resultEventBus = resultEventBus) { id -> receivedList += requestKey to id }
                }
            }
        }
        composeRule.waitForIdle()

        return receivedList
    }
}

@Composable
private fun AddedResultListener(
    requestKey: Uuid,
    resultEventBus: ResultEventBus,
    onAdded: (Uuid) -> Unit,
) {
    ResultEffect<PlaceAddedResult>(resultKey = placeAddedResultKey(requestKey = requestKey), resultEventBus = resultEventBus) { result -> onAdded(result.id) }
}
