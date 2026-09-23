package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.feature.more.ui.home.account.MoreHomeAccountUiState
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MoreHomeScaffoldTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 제목은 더보기다`() {
        setMoreHomeScaffold()

        composeRule.onNodeWithText("더보기").assertExists()
    }

    @Test
    fun `기본 환경에서 제목은 More다`() {
        setMoreHomeScaffold()

        composeRule.onNodeWithText(DEFAULT_TITLE).assertExists()
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-003 확인 중 상태에서는 라벨과 계정 동작을 표시하지 않는다`() {
        setMoreHomeScaffold(accountUiStateProvider = { MoreHomeAccountUiState.Loading })

        composeRule.onNodeWithText(DEFAULT_GUEST_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SIGN_OUT_LABEL).assertDoesNotExist()
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MORE-HOME-FEATURE-004 한국어 게스트 상태에서 게스트 라벨과 로그인 동작을 표시한다`() {
        setMoreHomeScaffold(accountUiStateProvider = { MoreHomeAccountUiState.Guest })

        composeRule.onNodeWithText("게스트").assertExists()
        composeRule.onNodeWithText("로그인").assert(hasClickAction())
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-004 기본 게스트 상태에서 게스트 라벨과 로그인 동작을 표시한다`() {
        setMoreHomeScaffold(accountUiStateProvider = { MoreHomeAccountUiState.Guest })

        composeRule.onNodeWithText(DEFAULT_GUEST_LABEL).assertExists()
        composeRule.onNodeWithText(DEFAULT_SIGN_IN_LABEL).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko")
    fun `TC-MORE-HOME-FEATURE-005 한국어 사용자 상태에서 이메일과 로그아웃 동작을 표시한다`() {
        setMoreHomeScaffold(
            accountUiStateProvider = {
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL)
            },
        )

        composeRule.onNodeWithText(USER_EMAIL).assertExists()
        composeRule.onNodeWithText("게스트").assertDoesNotExist()
        composeRule.onNodeWithText("로그아웃").assert(hasClickAction())
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-005 기본 사용자 상태에서 이메일과 로그아웃 동작을 표시한다`() {
        setMoreHomeScaffold(
            accountUiStateProvider = {
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL)
            },
        )

        composeRule.onNodeWithText(USER_EMAIL).assertExists()
        composeRule.onNodeWithText(DEFAULT_GUEST_LABEL).assertDoesNotExist()
        composeRule.onNodeWithText(DEFAULT_SIGN_OUT_LABEL).assert(hasClickAction())
    }

    @Test
    @Config(qualifiers = "ko-$MENU_VISIBLE_QUALIFIERS")
    fun `TC-MORE-HOME-FEATURE-008 한국어 환경에서 한글이 아닌 이름을 앞에 두고 가나다순으로 표시한다`() {
        setMoreHomeScaffold()

        inReadingOrder(KOREAN_MENU_LABELS) shouldBe KOREAN_MENU_LABELS
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-008 기본 환경에서 메뉴 항목을 한국어 이름과 같은 순서로 표시한다`() {
        setMoreHomeScaffold()

        inReadingOrder(DEFAULT_MENU_LABELS) shouldBe DEFAULT_MENU_LABELS
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-009 각 메뉴 항목을 선택할 수 있다`() {
        setMoreHomeScaffold()

        DEFAULT_MENU_LABELS.forEach { label ->
            composeRule.onNodeWithText(label).assert(hasClickAction())
        }
    }

    @Test
    @Config(qualifiers = "ko")
    fun `한국어 환경에서 프로필 사진 선택 동작 접근성 이름을 제공한다`() {
        setMoreHomeScaffold(accountUiStateProvider = { userAccountUiState() })

        profileClickLabel(profileImageDescription = "프로필 이미지") shouldBe "프로필 사진 선택"
    }

    @Test
    fun `기본 환경에서 프로필 사진 선택 동작 접근성 이름을 제공한다`() {
        setMoreHomeScaffold(accountUiStateProvider = { userAccountUiState() })

        profileClickLabel(profileImageDescription = DEFAULT_PROFILE_IMAGE_DESCRIPTION) shouldBe DEFAULT_PROFILE_PHOTO_PICKER_LABEL
    }

    @Test
    fun `확인 중과 게스트 상태에서는 프로필을 누를 수 없다`() {
        val accountUiState = mutableStateOf<MoreHomeAccountUiState>(MoreHomeAccountUiState.Loading)
        setMoreHomeScaffold(accountUiStateProvider = { accountUiState.value })

        NON_USER_ACCOUNT_UI_STATES.forEach { uiState ->
            composeRule.runOnIdle { accountUiState.value = uiState }

            composeRule.onNodeWithContentDescription(DEFAULT_PROFILE_IMAGE_DESCRIPTION).assert(!hasClickAction())
        }
    }

    private fun profileClickLabel(profileImageDescription: String): String? =
        composeRule
            .onNodeWithContentDescription(profileImageDescription)
            .fetchSemanticsNode()
            .config[SemanticsActions.OnClick]
            .label

    private fun inReadingOrder(labels: List<String>): List<String> =
        labels
            .map { label -> label to composeRule.onNodeWithText(label).fetchSemanticsNode().boundsInRoot }
            .sortedWith(compareBy({ it.second.top }, { it.second.left }))
            .map { it.first }

    private fun setMoreHomeScaffold(
        accountUiStateProvider: () -> MoreHomeAccountUiState = { MoreHomeAccountUiState.Guest },
        onEvent: (MoreHomeScaffoldEvent) -> Unit = {},
    ) {
        composeRule.setContent {
            DiaryTheme {
                MoreHomeScaffold(
                    accountUiStateProvider = accountUiStateProvider,
                    onEvent = onEvent,
                )
            }
        }
    }

    public companion object {
        // 메뉴 격자는 화면에 보이는 항목만 배치하므로, 모든 항목을 한 번에 확인하는 테스트에만 넉넉한 창을 준다.
        private const val MENU_VISIBLE_QUALIFIERS = "w480dp-h1600dp"

        private const val DEFAULT_TITLE = "More"
        private const val DEFAULT_GUEST_LABEL = "Guest"
        private const val DEFAULT_SIGN_IN_LABEL = "Sign in"
        private const val DEFAULT_SIGN_OUT_LABEL = "Sign out"
        private const val DEFAULT_PROFILE_IMAGE_DESCRIPTION = "Profile image"
        private const val DEFAULT_PROFILE_PHOTO_PICKER_LABEL = "Choose profile photo"
        private const val USER_EMAIL = "diary@example.com"
        private val NON_USER_ACCOUNT_UI_STATES =
            listOf(
                MoreHomeAccountUiState.Loading,
                MoreHomeAccountUiState.Guest,
            )
        private val KOREAN_MENU_LABELS =
            listOf(
                "QR",
                "검색",
                "디데이",
                "연락처",
                "웹",
                "장소",
                "체크리스트",
                "파일",
                "플레이리스트",
                "황금연휴",
            )
        private val DEFAULT_MENU_LABELS =
            listOf(
                "QR",
                "Search",
                "D-Day",
                "Contacts",
                "Web",
                "Place",
                "Checklist",
                "Files",
                "Playlist",
                "Golden Holiday",
            )

        private fun userAccountUiState(): MoreHomeAccountUiState = MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL)
    }
}
