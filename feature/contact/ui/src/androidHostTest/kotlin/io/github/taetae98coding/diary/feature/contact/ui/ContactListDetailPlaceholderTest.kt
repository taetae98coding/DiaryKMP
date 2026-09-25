@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.ListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberDiaryListDetailSceneStrategy
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.memo.list.MemoListUiState
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.core.navigation.ScreenNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactDetailNavKey
import io.github.taetae98coding.diary.feature.contact.api.ContactHomeNavKey
import io.github.taetae98coding.diary.feature.contact.ui.add.ContactAddViewModel
import io.github.taetae98coding.diary.feature.contact.ui.add.DEFAULT_BIRTHDAY_NOT_SET
import io.github.taetae98coding.diary.feature.contact.ui.add.INPUT_COUNT_WITHOUT_PHONE_NUMBER
import io.github.taetae98coding.diary.feature.contact.ui.add.TYPED_FIRST_PHONE_NUMBER
import io.github.taetae98coding.diary.feature.contact.ui.add.TYPED_FOOT_SIZE
import io.github.taetae98coding.diary.feature.contact.ui.add.TYPED_HEIGHT
import io.github.taetae98coding.diary.feature.contact.ui.add.addPhoneNumberRow
import io.github.taetae98coding.diary.feature.contact.ui.add.descriptionInput
import io.github.taetae98coding.diary.feature.contact.ui.add.footSizeInput
import io.github.taetae98coding.diary.feature.contact.ui.add.heightInput
import io.github.taetae98coding.diary.feature.contact.ui.add.hometownInput
import io.github.taetae98coding.diary.feature.contact.ui.add.inputCount
import io.github.taetae98coding.diary.feature.contact.ui.add.nameInput
import io.github.taetae98coding.diary.feature.contact.ui.add.phoneNumberInput
import io.github.taetae98coding.diary.feature.contact.ui.add.screenTestViewModel
import io.github.taetae98coding.diary.feature.contact.ui.add.selectBirthday
import io.github.taetae98coding.diary.feature.contact.ui.add.todayDisplayText
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailEffect
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailUiState
import io.github.taetae98coding.diary.feature.contact.ui.detail.ContactDetailViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoSyncViewModel
import io.github.taetae98coding.diary.feature.contact.ui.detail.memo.ContactDetailMemoViewModel
import io.github.taetae98coding.diary.feature.contact.ui.home.CONTACT_CARD_TEST_TAG
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeSyncViewModel
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeUiState
import io.github.taetae98coding.diary.feature.contact.ui.home.ContactHomeViewModel
import io.github.taetae98coding.diary.feature.contact.ui.home.contactPagingDataOf
import io.github.taetae98coding.diary.feature.contact.ui.home.testContact
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.compose.KoinApplication
import org.koin.core.context.stopKoin
import org.koin.dsl.koinConfiguration
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.uuid.Uuid

// 선택한 연락처가 없을 때의 연락처 추가는 목록과 상세를 함께 표시할 때만 상세 영역에 놓이므로, 두 영역을 함께 표시하는 너비의 NavDisplay 위에서 검증한다.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w1280dp-h1600dp")
class ContactListDetailPlaceholderTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val viewModelList = mutableListOf<ContactAddViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(MoreNavKey, ContactHomeNavKey)
    private var homeContactList: List<Contact> = emptyList()
    private val homeViewModelList = mutableListOf<ContactHomeViewModel>()

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @Before
    fun resetUiDispatcher() {
        resetAndroidUiDispatcher()
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-005 더보기로 돌아갔다가 다시 진입하면 상세 영역의 연락처 추가는 입력이 모두 비어 있는 상태로 시작한다`() {
        val input = placeholderInput()
        setPlaceholderNavDisplay()
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(MORE_CONTENT).assertExists()
        composeRule.runOnIdle { backStack.add(ContactHomeNavKey) }
        composeRule.waitForIdle()

        composeRule.nameInput().assert(hasText(""))
        composeRule.descriptionInput().assert(hasText(""))
        composeRule.heightInput().assert(hasText(""))
        composeRule.footSizeInput().assert(hasText(""))
        composeRule.hometownInput().assert(hasText(""))
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_PHONE_NUMBER
        composeRule.onAllNodesWithText(DEFAULT_BIRTHDAY_NOT_SET).onFirst().assertExists()
        viewModelList shouldHaveSize 2
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-006 배치를 떠나기 전에는 상세 영역에서 연락처 상세를 보고 돌아와도 연락처 추가의 입력이 남는다`() {
        assertPlaceholderRetained(routeList = listOf(ContactDetailNavKey(id = fixtureMonkey.giveMeOne<Uuid>())))
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-006 배치를 떠나기 전에는 연락처 상세의 메모 탭에서 연 메모 추가에 다녀와도 연락처 추가의 입력이 남는다`() {
        val contactId = fixtureMonkey.giveMeOne<Uuid>()

        assertPlaceholderRetained(routeList = listOf(ContactDetailNavKey(id = contactId), MemoAddNavKey(initialContactId = contactId)))
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-011 연락처 상세가 놓인 동안 목록에서 연 연락처 추가는 빈 입력으로 시작한다`() {
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact))
        composeRule.fillPlaceholder(input = placeholderInput())
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertExists()

        composeRule.clickListAddButton()
        composeRule.assertContactAddIsEmpty()

        composeRule.nameInput().performTextInput(placeholderInput().name)
        composeRule.runOnIdle { backStack.removeLastOrNull() }
        composeRule.waitForIdle()
        composeRule.clickListAddButton()

        composeRule.assertContactAddIsEmpty()
        // 연락처 추가를 열 때마다 entry별 저장소에 새 ViewModel이 만들어진다. 선택 전 연락처 추가의 것까지 셋이다.
        viewModelList shouldHaveSize 3
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-010 상세 영역의 연락처에서 뒤로가면 연락처 추가로 되돌아간다`() {
        val input = placeholderInput()
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact))
        composeRule.fillPlaceholder(input = input)
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertExists()

        pressBack()

        backStack.toList() shouldBe listOf(MoreNavKey, ContactHomeNavKey)
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertDoesNotExist()
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertExists()
        composeRule.nameInput().assert(hasText(input.name))
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-012 연락처 상세가 놓인 동안 연 연락처 추가에서 뒤로가면 그 연락처의 상세로 돌아간다`() {
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact))
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).performClick()
        composeRule.waitForIdle()
        composeRule.clickListAddButton()
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertDoesNotExist()

        pressBack()

        backStack.toList() shouldBe listOf(MoreNavKey, ContactHomeNavKey, ContactDetailNavKey(id = contact.id))
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertExists()
        composeRule.onNodeWithTag(CONTACT_CARD_TEST_TAG).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-001 TC-CONTACT-LIST-DETAIL-FEATURE-008 넓은 창에서는 목록과 선택 전 연락처 추가를 함께 표시한다`() {
        val contact = listedContact()

        setContactNavDisplay(contactList = listOf(contact))

        composeRule.onNode(contactCard(contact)).assertIsDisplayed()
        composeRule.nameInput().assertIsDisplayed()
        composeRule.inputCount() shouldBe INPUT_COUNT_WITHOUT_PHONE_NUMBER
    }

    @Test
    @Config(qualifiers = "w400dp-h800dp")
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-002 좁은 창에서는 지금 사용하는 화면만 단독으로 표시한다`() {
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact))

        composeRule.onNode(contactCard(contact)).assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)

        composeRule.clickListAddButton()

        composeRule.nameInput().assertIsDisplayed()
        composeRule.onNode(contactCard(contact)).assertDoesNotExist()
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-009 연락처를 선택하면 상세 영역이 그 연락처의 상세로 바뀌고 목록은 유지된다`() {
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact))

        composeRule.onNode(contactCard(contact)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText(detailContent(id = contact.id)).assertIsDisplayed()
        composeRule.onNode(contactCard(contact)).assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-010 상세 영역의 연락처를 삭제하면 연락처 추가로 되돌아간다`() {
        val input = placeholderInput()
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact), isProductDetail = true)
        composeRule.fillPlaceholder(input = input)
        composeRule.onNode(contactCard(contact)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription(DEFAULT_DELETE_CONTACT_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(MoreNavKey, ContactHomeNavKey)
        composeRule.onNode(contactCard(contact)).assertIsDisplayed()
        composeRule.nameInput().assert(hasText(input.name))
    }

    @Test
    fun `TC-CONTACT-HOME-FEATURE-035 상세에 열린 연락처를 목록에서 밀어 삭제해도 상세 영역은 그 연락처의 상세로 남는다`() {
        val contact = listedContact()
        setContactNavDisplay(contactList = listOf(contact))
        composeRule.onNode(contactCard(contact)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertIsDisplayed()

        composeRule.onNode(contactCard(contact)).performTouchInput { swipeLeft() }
        composeRule.waitForIdle()

        verify(exactly = 1) { homeViewModelList.last().delete(id = contact.id) }
        backStack.toList() shouldBe listOf(MoreNavKey, ContactHomeNavKey, ContactDetailNavKey(id = contact.id))
        composeRule.onNodeWithText(detailContent(id = contact.id)).assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-013 상세가 놓인 동안 다른 연락처를 고르면 상세를 쌓지 않고 바꾼다`() {
        val first = listedContact()
        val second = listedContact()
        setContactNavDisplay(contactList = listOf(first, second))
        composeRule.onNode(contactCard(first)).performClick()
        composeRule.waitForIdle()

        composeRule.onNode(contactCard(second)).performClick()
        composeRule.waitForIdle()

        backStack.toList() shouldBe listOf(MoreNavKey, ContactHomeNavKey, ContactDetailNavKey(id = second.id))
        composeRule.onNodeWithText(detailContent(id = second.id)).assertIsDisplayed()
        composeRule.onNodeWithText(detailContent(id = first.id)).assertDoesNotExist()

        pressBack()

        backStack.toList() shouldBe listOf(MoreNavKey, ContactHomeNavKey)
        composeRule.onNodeWithText(detailContent(id = first.id)).assertDoesNotExist()
        composeRule.nameInput().assertIsDisplayed()
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-FEATURE-014 선택 전 연락처 추가가 놓인 상태에서 뒤로가면 배치를 떠나 더보기로 간다`() {
        setContactNavDisplay(contactList = listOf(listedContact()))
        composeRule.nameInput().assertIsDisplayed()

        pressBack()

        backStack.toList() shouldBe listOf(MoreNavKey)
        composeRule.onNodeWithText(MORE_CONTENT).assertIsDisplayed()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
    }

    private fun pressBack() {
        composeRule.runOnIdle { composeRule.activity.onBackPressedDispatcher.onBackPressed() }
        composeRule.waitForIdle()
    }

    // 목록 영역의 추가 버튼은 상세 영역에 연락처 상세가 놓여 있을 때만 보이므로, 누른 뒤 상세 영역에 연락처 추가가 놓이는 것까지 확인한다.
    private fun ComposeContentTestRule.clickListAddButton() {
        onNodeWithContentDescription(DEFAULT_ADD_CONTACT_DESCRIPTION).performClick()
        waitForIdle()
    }

    private fun ComposeContentTestRule.assertContactAddIsEmpty() {
        onNodeWithText(ROUTE_CONTENT).assertDoesNotExist()
        inputCount() shouldBe INPUT_COUNT_WITHOUT_PHONE_NUMBER
        nameInput().assert(hasText(""))
        descriptionInput().assert(hasText(""))
        heightInput().assert(hasText(""))
        footSizeInput().assert(hasText(""))
        hometownInput().assert(hasText(""))
        onAllNodesWithText(DEFAULT_BIRTHDAY_NOT_SET).onFirst().assertExists()
    }

    private fun assertPlaceholderRetained(routeList: List<ScreenNavKey>) {
        val input = placeholderInput()
        setPlaceholderNavDisplay()
        composeRule.fillPlaceholder(input = input)

        composeRule.runOnIdle { backStack.addAll(routeList) }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(ROUTE_CONTENT).assertExists()
        composeRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        routeList.forEach { _ ->
            composeRule.runOnIdle { backStack.removeLastOrNull() }
            composeRule.waitForIdle()
        }

        composeRule.nameInput().assert(hasText(input.name))
        composeRule.descriptionInput().assert(hasText(input.description))
        composeRule.heightInput().assert(hasText(TYPED_HEIGHT))
        composeRule.footSizeInput().assert(hasText(TYPED_FOOT_SIZE))
        composeRule.hometownInput().assert(hasText(input.hometown))
        composeRule.phoneNumberInput().assert(hasText(TYPED_FIRST_PHONE_NUMBER))
        composeRule.onAllNodesWithText(todayDisplayText()).onFirst().assertExists()
        viewModelList shouldHaveSize 1
    }

    private fun setPlaceholderNavDisplay() {
        setNavDisplay(
            entryDecorators = { holder ->
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberListDetailPlaceholderNavEntryDecorator(holder),
                )
            },
            homeEntry = {
                entry<ContactHomeNavKey>(
                    clazzContentKey = { CONTACT_HOME_CONTENT_KEY },
                    metadata = contactHomeListPaneMetadata(),
                ) { Text(text = CONTACT_HOME_CONTENT) }
            },
        )
    }

    // isProductDetail이면 상세 영역에도 제품의 연락처 상세 entry를 두어 삭제까지 실행한다.
    private fun setContactNavDisplay(
        contactList: List<Contact>,
        isProductDetail: Boolean = false,
    ) {
        homeContactList = contactList
        setNavDisplay(
            entryDecorators = { holder ->
                listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                    rememberListDetailPlaceholderNavEntryDecorator(holder),
                )
            },
            homeEntry = {
                contactHomeEntry(backStack = backStack)
                contactAddEntry(backStack = backStack)
                if (isProductDetail) contactDetailEntry(backStack = backStack)
            },
            isStubDetail = !isProductDetail,
        )
    }

    private fun setNavDisplay(
        entryDecorators: @Composable (ListDetailPlaceholderStateHolder) -> List<NavEntryDecorator<ScreenNavKey>>,
        homeEntry: EntryProviderScope<ScreenNavKey>.() -> Unit,
        isStubDetail: Boolean = true,
    ) {
        composeRule.setContent {
            ContactPlaceholderTestHost {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberDiaryListDetailSceneStrategy()),
                        entryDecorators = entryDecorators(holder),
                        entryProvider =
                            entryProvider {
                                entry<MoreNavKey> { Text(text = MORE_CONTENT) }
                                homeEntry()
                                if (isStubDetail) {
                                    entry<ContactDetailNavKey>(
                                        metadata = ListDetailSceneStrategy.detailPane(sceneKey = ContactHomeNavKey),
                                    ) { key ->
                                        Column {
                                            Text(text = ROUTE_CONTENT)
                                            Text(text = detailContent(id = key.id))
                                        }
                                    }
                                }
                                entry<MemoAddNavKey> { Text(text = ROUTE_CONTENT) }
                            },
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Composable
    private fun ContactPlaceholderTestHost(content: @Composable () -> Unit) {
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
            KoinApplication(configuration = koinConfiguration { modules(placeholderViewModelModule()) }) {
                DiaryTheme(content = content)
            }
        }
    }

    // 배치를 떠나면 ViewModel이 정리되므로 정리 호출에 답하는 relaxed mock을 쓰고, 만들어진 인스턴스를 세어 저장 범위를 확인한다.
    private fun placeholderViewModelModule() =
        module {
            factory { screenTestViewModel().also { viewModel -> viewModelList += viewModel } }
            factory {
                mockk<ContactHomeViewModel>(relaxed = true)
                    .apply {
                        every { contactPagingData } returns MutableStateFlow(contactPagingDataOf(homeContactList))
                        every { sort } returns MutableStateFlow(ListSort.NAME)
                        every { effect } returns emptyFlow()
                    }.also { viewModel -> homeViewModelList += viewModel }
            }
            factory {
                mockk<ContactHomeSyncViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(ContactHomeUiState())
                }
            }
            factory { parameters ->
                val id = parameters.get<Uuid>()
                val effectChannel = Channel<ContactDetailEffect>(capacity = Channel.BUFFERED)
                mockk<ContactDetailViewModel>(relaxed = true).apply {
                    every { uiState } returns
                        MutableStateFlow(
                            ContactDetailUiState.Content(id = id, detail = homeContactList.first { contact -> contact.id == id }.detail),
                        )
                    every { effect } returns effectChannel.receiveAsFlow()
                    every { delete() } answers { effectChannel.trySend(ContactDetailEffect.DeleteSucceeded).getOrThrow() }
                }
            }
            factory {
                mockk<ContactDetailMemoViewModel>(relaxed = true).apply {
                    every { memoPagingData } returns flowOf(PagingData.empty())
                    every { sort } returns MutableStateFlow(ListSort.DEFAULT)
                    every { effect } returns emptyFlow()
                }
            }
            factory {
                mockk<ContactDetailMemoSyncViewModel>(relaxed = true).apply {
                    every { uiState } returns MutableStateFlow(MemoListUiState())
                }
            }
        }

    private fun ComposeContentTestRule.fillPlaceholder(input: PlaceholderInput) {
        nameInput().performTextInput(input.name)
        descriptionInput().performTextInput(input.description)
        heightInput().performTextInput(TYPED_HEIGHT)
        footSizeInput().performTextInput(TYPED_FOOT_SIZE)
        hometownInput().performTextInput(input.hometown)
        selectBirthday()
        addPhoneNumberRow()
        phoneNumberInput().performTextInput(TYPED_FIRST_PHONE_NUMBER)
        waitForIdle()
        nameInput().assert(hasText(input.name))
    }

    private data class PlaceholderInput(
        val name: String,
        val description: String,
        val hometown: String,
    )

    private companion object {
        const val MORE_CONTENT = "MoreContent"
        const val CONTACT_HOME_CONTENT = "ContactHomeContent"
        const val ROUTE_CONTENT = "RouteContent"
        const val DEFAULT_ADD_CONTACT_DESCRIPTION = "Add contact"
        const val DEFAULT_DELETE_CONTACT_DESCRIPTION = "Delete contact"
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

        fun detailContent(id: Uuid): String = "ContactDetail-$id"

        fun contactCard(contact: Contact): SemanticsMatcher = hasTestTag(CONTACT_CARD_TEST_TAG) and hasText(contact.detail.name)

        fun listedContact(): Contact = testContact(name = "list-${fixtureMonkey.giveMeOne<String>()}")

        fun placeholderInput(): PlaceholderInput =
            PlaceholderInput(
                name = "name-${fixtureMonkey.giveMeOne<String>()}",
                description = "description-${fixtureMonkey.giveMeOne<String>()}",
                hometown = "hometown-${fixtureMonkey.giveMeOne<String>()}",
            )
    }
}

// 연락처 화면으로 들어가는 `더보기` 화면을 대신한다. 더보기 기능 모듈은 이 모듈의 의존이 아니므로 대역을 사용한다.
private data object MoreNavKey : ScreenNavKey {
    override val screenName: String
        get() = "More"
}
