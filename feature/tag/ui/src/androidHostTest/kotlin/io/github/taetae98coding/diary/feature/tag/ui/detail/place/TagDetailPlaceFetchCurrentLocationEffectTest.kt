package io.github.taetae98coding.diary.feature.tag.ui.detail.place

import androidx.compose.ui.test.junit4.v2.createComposeRule
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TagDetailPlaceFetchCurrentLocationEffectTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-004 목록 모드에 머무르는 동안에는 현재 위치를 확인하지 않는다`() {
        val mapViewModel = mockk<TagDetailPlaceMapViewModel>(relaxed = true)
        setEffect(state = TagDetailPlaceState(), mapViewModel = mapViewModel)

        verify(exactly = 0) { mapViewModel.fetchCurrentLocation() }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-003 처음 지도 모드로 바꿀 때 현재 위치를 확인한다`() {
        val state = TagDetailPlaceState()
        val mapViewModel = mockk<TagDetailPlaceMapViewModel>(relaxed = true)
        setEffect(state = state, mapViewModel = mapViewModel)

        composeRule.runOnIdle { state.toggleViewMode() }
        composeRule.waitForIdle()

        verify(exactly = 1) { mapViewModel.fetchCurrentLocation() }
    }

    @Test
    fun `TC-TAG-DETAIL-PLACE-DOMAIN-005 목록 모드로 되돌린 뒤 다시 지도 모드로 바꿀 때만 확인을 요청한다`() {
        val state = TagDetailPlaceState()
        val mapViewModel = mockk<TagDetailPlaceMapViewModel>(relaxed = true)
        setEffect(state = state, mapViewModel = mapViewModel)

        repeat(TOGGLE_COUNT) {
            composeRule.runOnIdle { state.toggleViewMode() }
            composeRule.waitForIdle()
        }

        // 확인을 한 번만 수행하는 것은 요청을 받는 쪽이 보장하며, 여기서는 지도 모드가 될 때만 요청하는 것을 확인한다.
        verify(exactly = TOGGLE_COUNT / 2) { mapViewModel.fetchCurrentLocation() }
    }

    private fun setEffect(
        state: TagDetailPlaceState,
        mapViewModel: TagDetailPlaceMapViewModel,
    ) {
        composeRule.setContent {
            TagDetailPlaceFetchCurrentLocationEffect(
                mapViewModel = mapViewModel,
                state = state,
            )
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val TOGGLE_COUNT = 4
    }
}
