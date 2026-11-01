@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListItem
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailUiState
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.DEFAULT_MEMO_TAB_DESCRIPTION
import io.github.taetae98coding.diary.feature.contact.ui.detail.contactMemo
import io.github.taetae98coding.diary.feature.contact.ui.detail.contactMemoPagingData
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.selectContactDetailTab
import io.github.taetae98coding.diary.feature.contact.ui.detail.testContactDetail
import io.github.taetae98coding.diary.feature.core.list.ListSortUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshUiState
import io.github.taetae98coding.diary.feature.core.sync.SyncRefreshViewModel
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.feature.memo.api.MemoDetailNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 다른 기능의 화면은 연락처 상세로 갈 때 그 위에 연락처 상세를 쌓기만 하므로, 그 화면을 대역으로 두고 제품 연락처 entry로 NavDisplay를 구성한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h800dp")
class ContactDetailEntryRouteTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val memoContactIdList = mutableListOf<Uuid>()

    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-d1672a7e-7824-469d-bfb3-4efaa25c310e 캘린더 홈 화면의 생일에서 진입한 ContactDetail 화면에서도 메모 탭을 제공한다`() {
        assertMemoTabProvided(previousRouteList = listOf(CalendarHomeStandInNavKey))
    }

    @Test
    fun `TC-d1672a7e-7824-469d-bfb3-4efaa25c310e 메모 추가의 연락처 입력에서 진입한 ContactDetail 화면에서도 메모 탭을 제공한다`() {
        assertMemoTabProvided(previousRouteList = listOf(MemoHomeStandInNavKey, MemoAddNavKey()))
    }

    @Test
    fun `TC-d1672a7e-7824-469d-bfb3-4efaa25c310e 메모 상세의 연락처 입력에서 진입한 ContactDetail 화면에서도 메모 탭을 제공한다`() {
        assertMemoTabProvided(previousRouteList = listOf(MemoHomeStandInNavKey, MemoDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())))
    }

    private fun assertMemoTabProvided(previousRouteList: List<ScreenNavKey>) {
        val contactId = fixtureMonkey.giveMeOne<Uuid>()
        val memoTitle = "ContactMemo-${fixtureMonkey.giveMeOne<Int>()}"
        val backStack = NavBackStack<ScreenNavKey>(*previousRouteList.toTypedArray())
        setNavDisplay(backStack = backStack, contactId = contactId, memoTitle = memoTitle)

        composeRule.runOnIdle { backStack.add(ContactDetailNavKey(id = contactId)) }
        composeRule.waitForIdle()
        composeRule.selectContactDetailTab(DEFAULT_MEMO_TAB_DESCRIPTION)

        composeRule.onNodeWithContentDescription(DEFAULT_MEMO_TAB_DESCRIPTION).assertIsSelected()
        composeRule.waitUntil(timeoutMillis = PAGING_ITEMS_TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(memoTitle).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(memoTitle).assertIsDisplayed()
        composeRule.onNodeWithText(PREVIOUS_ROUTE_CONTENT).assertDoesNotExist()
        memoContactIdList.distinct() shouldBe listOf(contactId)
    }

    private fun viewModelModule(
        contactId: Uuid,
        memoTitle: String,
    ): Module =
        module {
            factory {
                mockk<ContactDetailViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(ContactDetailUiState.Content(id = contactId, detail = testContactDetail()))
                    every { effect } returns emptyFlow()
                }
            }
            factory { parameters ->
                memoContactIdList += parameters.get<Uuid>()
                mockk<ContactDetailMemoViewModel>(relaxed = true).apply {
                    every { memoPagingData } returns
                        flowOf(contactMemoPagingData(itemList = listOf(MemoListItem.Content(memo = contactMemo(title = memoTitle)))))
                    every { sortUiState } returns MutableStateFlow(ListSortUiState(sort = ListSort.DEFAULT))
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<SyncRefreshViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(SyncRefreshUiState())
                }
            }
        }

    private fun setNavDisplay(
        backStack: NavBackStack<ScreenNavKey>,
        contactId: Uuid,
        memoTitle: String,
    ) {
        val viewModelModule = viewModelModule(contactId = contactId, memoTitle = memoTitle)

        composeRule.setContent {
            // 테스트 호스트 Activity의 ViewModelStore는 테스트 사이에 유지되므로 테스트마다 새 소유자를 둔다.
            val viewModelStoreOwner =
                remember {
                    object : ViewModelStoreOwner {
                        override val viewModelStore: ViewModelStore = ViewModelStore()
                    }
                }
            val resultEventBus = remember { ResultEventBus() }

            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner,
                LocalResultEventBus provides resultEventBus,
            ) {
                KoinApplication(configuration = koinConfiguration { modules(viewModelModule) }) {
                    DiaryTheme {
                        val holder = rememberListDetailPlaceholderStateHolder()

                        CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                            NavDisplay(
                                backStack = backStack,
                                sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                                entryDecorators =
                                    listOf(
                                        rememberSaveableStateHolderNavEntryDecorator(),
                                        rememberViewModelStoreNavEntryDecorator(),
                                        rememberListDetailPlaceholderNavEntryDecorator(holder),
                                    ),
                                entryProvider =
                                    entryProvider {
                                        entry<CalendarHomeStandInNavKey> { Text(text = PREVIOUS_ROUTE_CONTENT) }
                                        entry<MemoHomeStandInNavKey> { Text(text = PREVIOUS_ROUTE_CONTENT) }
                                        entry<MemoAddNavKey> { Text(text = PREVIOUS_ROUTE_CONTENT) }
                                        entry<MemoDetailNavKey> { Text(text = PREVIOUS_ROUTE_CONTENT) }
                                        contactDetailEntry(backStack = backStack)
                                    },
                            )
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private companion object {
        const val PREVIOUS_ROUTE_CONTENT = "PreviousRouteContent"
        const val PAGING_ITEMS_TIMEOUT_MILLIS = 5_000L
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()
    }
}

// 연락처 상세로 들어가는 캘린더 홈과 메모 목록 화면을 대신한다. 두 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object CalendarHomeStandInNavKey : ScreenNavKey {
    override val screenName: String
        get() = "CalendarHomeStandIn"
}

private data object MemoHomeStandInNavKey : ScreenNavKey {
    override val screenName: String
        get() = "MemoHomeStandIn"
}
