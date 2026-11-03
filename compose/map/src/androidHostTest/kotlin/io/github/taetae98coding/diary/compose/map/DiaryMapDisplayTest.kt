package io.github.taetae98coding.diary.compose.map

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProvider
import io.github.taetae98coding.diary.compose.map.provider.DiaryMapProviderTestFixture.label
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DiaryMapDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val mapContent =
        object : DiaryMapContentOverride {
            @Composable
            override fun DiaryMapContentOverrideScope.MapContent() {
                Box(modifier = modifier.testTag(mapTag(state.provider)))
            }
        }

    @Test
    fun `TC-5892fbc1-c2c5-4cd6-8c6a-5b7dcf7f6dea 초기 제공자를 지정하지 않으면 네이버 지도가 표시된다`() {
        composeRule.setContent {
            DiaryMapContentProvider { DiaryMap(modifier = Modifier.fillMaxSize()) }
        }

        composeRule.onNodeWithText(label(DiaryMapProvider.NAVER)).assertIsSelected()
        composeRule.onNodeWithTag(mapTag(DiaryMapProvider.NAVER)).assertIsDisplayed()
        composeRule.onNodeWithTag(mapTag(DiaryMapProvider.GOOGLE)).assertDoesNotExist()
    }

    @Test
    fun `TC-a740c1d4-9f5b-4622-b763-66eb2d3ac904 초기 제공자로 네이버 지도를 지정하면 네이버 지도만 표시된다`() {
        assertInitialProviderMapDisplayed(provider = DiaryMapProvider.NAVER)
    }

    @Test
    fun `TC-a740c1d4-9f5b-4622-b763-66eb2d3ac904 초기 제공자로 Google 지도를 지정하면 Google 지도만 표시된다`() {
        assertInitialProviderMapDisplayed(provider = DiaryMapProvider.GOOGLE)
    }

    @Test
    fun `TC-f358145f-ac48-4f53-8616-0f7a6172d0a9 네이버 지도에서 Google 지도를 선택하면 Google 지도로 바뀐다`() {
        assertProviderSwitched(before = DiaryMapProvider.NAVER, after = DiaryMapProvider.GOOGLE)
    }

    @Test
    fun `TC-f358145f-ac48-4f53-8616-0f7a6172d0a9 Google 지도에서 네이버 지도를 선택하면 네이버 지도로 바뀐다`() {
        assertProviderSwitched(before = DiaryMapProvider.GOOGLE, after = DiaryMapProvider.NAVER)
    }

    @Test
    fun `TC-96703ba6-edd3-4bb0-98c1-3cf05a08b898 네이버 지도를 다시 선택해도 보던 지도가 유지된다`() {
        assertReselectKeepsMap(provider = DiaryMapProvider.NAVER)
    }

    @Test
    fun `TC-96703ba6-edd3-4bb0-98c1-3cf05a08b898 Google 지도를 다시 선택해도 보던 지도가 유지된다`() {
        assertReselectKeepsMap(provider = DiaryMapProvider.GOOGLE)
    }

    @Test
    fun `TC-926f3008-11d9-45a9-ac60-2657fa9ecd9c 위치 권한이 없어도 지도를 표시할 때 권한 요청을 시작하지 않는다`() {
        val registry = spyk<ActivityResultRegistry>()
        every { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) } answers {
            registry.dispatchResult(firstArg(), emptyMap<String, Boolean>())
        }
        val registryOwner = mockk<ActivityResultRegistryOwner>()
        every { registryOwner.activityResultRegistry } returns registry

        composeRule.setContent {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides registryOwner) {
                DiaryMapContentProvider { DiaryMap(modifier = Modifier.fillMaxSize()) }
            }
        }

        composeRule.onNodeWithTag(mapTag(DiaryMapProvider.NAVER)).assertIsDisplayed()
        verify(exactly = 0) { registry.onLaunch(any(), any<ActivityResultContract<Any?, Any?>>(), any(), any()) }
    }

    private fun assertInitialProviderMapDisplayed(provider: DiaryMapProvider) {
        setDiaryMap(state = DiaryMapState(initialProvider = provider))

        composeRule.onNodeWithText(label(provider)).assertIsSelected()
        composeRule.onNodeWithTag(mapTag(provider)).assertIsDisplayed()
        composeRule.onNodeWithTag(mapTag(provider.other)).assertDoesNotExist()
    }

    private fun assertProviderSwitched(
        before: DiaryMapProvider,
        after: DiaryMapProvider,
    ) {
        val state = DiaryMapState(initialProvider = before)
        setDiaryMap(state = state)
        composeRule.onNodeWithTag(mapTag(before)).assertIsDisplayed()

        composeRule.onNodeWithText(label(after)).performClick()

        composeRule.onNodeWithTag(mapTag(before)).assertDoesNotExist()
        composeRule.onNodeWithTag(mapTag(after)).assertIsDisplayed()
        composeRule.runOnIdle { state.provider shouldBe after }
    }

    private fun assertReselectKeepsMap(provider: DiaryMapProvider) {
        val coordinate = fixtureMonkey.giveMeOne<DiaryMapCoordinate>()
        val camera =
            DiaryMapCamera(
                latitude = coordinate.latitude,
                longitude = coordinate.longitude,
                zoom = DiaryMapCamera.NEIGHBORHOOD_ZOOM + 2,
            )
        val state = DiaryMapState(initialProvider = provider)
        setDiaryMap(state = state)
        composeRule.runOnIdle { state.moveCamera(camera) }

        composeRule.onNodeWithText(label(provider)).performClick()

        composeRule.onNodeWithText(label(provider)).assertIsSelected()
        composeRule.onNodeWithTag(mapTag(provider)).assertIsDisplayed()
        composeRule.onNodeWithTag(mapTag(provider.other)).assertDoesNotExist()
        composeRule.runOnIdle {
            state.provider shouldBe provider
            state.camera shouldBe camera
        }
    }

    private fun setDiaryMap(state: DiaryMapState) {
        composeRule.setContent {
            DiaryMapContentProvider { DiaryMap(modifier = Modifier.fillMaxSize(), state = state) }
        }
    }

    @Composable
    private fun DiaryMapContentProvider(content: @Composable () -> Unit) {
        DiaryTheme {
            CompositionLocalProvider(LocalDiaryMapContentOverride provides mapContent, content = content)
        }
    }

    private companion object {
        private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        private val DiaryMapProvider.other: DiaryMapProvider
            get() = DiaryMapProvider.entries.single { provider -> provider != this }

        private fun mapTag(provider: DiaryMapProvider): String = "diaryMap-${provider.name}"
    }
}
