@file:OptIn(ExperimentalMaterial3AdaptiveApi::class)

package io.github.taetae98coding.diary.feature.contact.ui

import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.result.LocalResultEventBus
import androidx.navigation3.runtime.result.ResultEventBus
import androidx.navigation3.ui.NavDisplay
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.compose.core.scene.LocalListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderNavEntryDecorator
import io.github.taetae98coding.diary.compose.core.scene.rememberListDetailPlaceholderStateHolder
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
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
import io.github.taetae98coding.diary.feature.memo.api.MemoAddNavKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.After
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
    val composeRule = createComposeRule()

    private val viewModelList = mutableListOf<ContactAddViewModel>()
    private val backStack = NavBackStack<ScreenNavKey>(MoreNavKey, ContactHomeNavKey)

    // KoinApplication 컴포저블은 전역 Koin이 남아 있으면 새 모듈 선언을 무시하고 재사용하므로 테스트마다 전역 Koin을 정리한다.
    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-005 더보기로 돌아갔다가 다시 진입하면 상세 영역의 연락처 추가는 입력이 모두 비어 있는 상태로 시작한다`() {
        val input = placeholderInput()
        setContactNavDisplay()
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
        assertPlaceholderRetained(routeList = listOf(ContactDetailNavKey(id = Uuid.random())))
    }

    @Test
    fun `TC-CONTACT-LIST-DETAIL-DOMAIN-006 배치를 떠나기 전에는 연락처 상세의 메모 탭에서 연 메모 추가에 다녀와도 연락처 추가의 입력이 남는다`() {
        val contactId = Uuid.random()

        assertPlaceholderRetained(routeList = listOf(ContactDetailNavKey(id = contactId), MemoAddNavKey(initialContactId = contactId)))
    }

    private fun assertPlaceholderRetained(routeList: List<ScreenNavKey>) {
        val input = placeholderInput()
        setContactNavDisplay()
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

    private fun setContactNavDisplay() {
        composeRule.setContent {
            ContactPlaceholderTestHost {
                val holder = rememberListDetailPlaceholderStateHolder()

                CompositionLocalProvider(LocalListDetailPlaceholderStateHolder provides holder) {
                    NavDisplay(
                        backStack = backStack,
                        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
                        entryDecorators =
                            listOf(
                                rememberSaveableStateHolderNavEntryDecorator(),
                                rememberListDetailPlaceholderNavEntryDecorator(holder),
                            ),
                        entryProvider =
                            entryProvider {
                                entry<MoreNavKey> { Text(text = MORE_CONTENT) }
                                entry<ContactHomeNavKey>(
                                    clazzContentKey = { CONTACT_HOME_CONTENT_KEY },
                                    metadata = contactHomeListPaneMetadata(),
                                ) { Text(text = CONTACT_HOME_CONTENT) }
                                entry<ContactDetailNavKey>(
                                    metadata = ListDetailSceneStrategy.detailPane(sceneKey = ContactHomeNavKey),
                                ) { Text(text = ROUTE_CONTENT) }
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
        val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

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
