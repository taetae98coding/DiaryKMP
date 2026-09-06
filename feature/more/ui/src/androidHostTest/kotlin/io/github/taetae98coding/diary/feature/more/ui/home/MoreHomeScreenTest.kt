package io.github.taetae98coding.diary.feature.more.ui.home

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.file.api.FileUri
import io.github.taetae98coding.diary.feature.more.ui.photo.PhotoPicker
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MoreHomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `TC-MORE-HOME-FEATURE-006 로그인 동작을 선택하면 Login 화면으로 이동한다`() {
        var navigateToLoginCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToLogin = { navigateToLoginCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_SIGN_IN_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToLoginCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-010 장소 항목을 선택하면 PlaceHome 화면으로 이동한다`() {
        var navigateToPlaceCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToPlace = { navigateToPlaceCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_PLACE_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToPlaceCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-014 황금연휴 항목을 선택하면 HolidayHome 화면으로 이동한다`() {
        var navigateToHolidayCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToHoliday = { navigateToHolidayCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_HOLIDAY_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToHolidayCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-015 검색 항목을 선택하면 SearchHome 화면으로 이동한다`() {
        var navigateToSearchCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToSearch = { navigateToSearchCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_SEARCH_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToSearchCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-016 웹 항목을 선택하면 WebHome 화면으로 이동한다`() {
        var navigateToWebCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToWeb = { navigateToWebCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_WEB_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToWebCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-019 연락처 항목을 선택하면 ContactHome 화면으로 이동한다`() {
        var navigateToContactCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToContact = { navigateToContactCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_CONTACT_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToContactCount shouldBe 1
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-013 설정 동작을 선택하면 SettingHome 화면으로 이동한다`() {
        var navigateToSettingCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToSetting = { navigateToSettingCount += 1 },
        )

        composeRule.onNodeWithContentDescription(DEFAULT_SETTING_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        navigateToSettingCount shouldBe 1
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-007 로그아웃 동작을 선택하면 로그아웃이 시작된다`() {
        var navigateToLoginCount = 0
        val viewModel =
            screenTestViewModel(
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL),
            )
        every { viewModel.signOut() } returns Unit
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToLogin = { navigateToLoginCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_SIGN_OUT_LABEL).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.signOut() }
        navigateToLoginCount shouldBe 0
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-017 사용자 상태에서 프로필을 선택하면 사진 선택 도구를 연다`() {
        val photoPicker = screenTestPhotoPicker()
        val viewModel =
            screenTestViewModel(
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL),
            )
        setMoreHomeScreen(
            viewModel = viewModel,
            photoPicker = photoPicker,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_PROFILE_IMAGE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { photoPicker.open() }
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-018 사진을 고르면 고른 사진을 프로필 이미지로 반영하도록 요청한다`() {
        val pickedUri = FileUri(PICKED_PHOTO_URI)
        val photoPicker = screenTestPhotoPicker(pickedUri = pickedUri)
        val viewModel =
            screenTestViewModel(
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL),
            )
        setMoreHomeScreen(
            viewModel = viewModel,
            photoPicker = photoPicker,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_PROFILE_IMAGE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        verify(exactly = 1) { viewModel.changeProfileImage(uri = pickedUri) }
    }

    @Test
    fun `TC-MORE-HOME-FEATURE-020 사진 선택을 취소하면 프로필 이미지 반영을 요청하지 않는다`() {
        val photoPicker = screenTestPhotoPicker(pickedUri = null)
        val viewModel =
            screenTestViewModel(
                MoreHomeAccountUiState.User(profileImage = null, email = USER_EMAIL),
            )
        setMoreHomeScreen(
            viewModel = viewModel,
            photoPicker = photoPicker,
        )

        composeRule.onNodeWithContentDescription(DEFAULT_PROFILE_IMAGE_DESCRIPTION).performClick()
        composeRule.waitForIdle()

        coVerify(exactly = 1) { photoPicker.open() }
        verify(exactly = 0) { viewModel.changeProfileImage(uri = any()) }
    }

    @Test
    fun `TC-MORE-HOME-DOMAIN-004 확인 중과 게스트 상태에서는 프로필을 선택해도 사진 선택 도구를 열지 않는다`() {
        val photoPicker = screenTestPhotoPicker()
        val accountUiState = MutableStateFlow<MoreHomeAccountUiState>(MoreHomeAccountUiState.Loading)
        val viewModel = mockk<MoreHomeAccountViewModel>()
        every { viewModel.uiState } returns accountUiState
        setMoreHomeScreen(
            viewModel = viewModel,
            photoPicker = photoPicker,
        )

        NON_USER_ACCOUNT_UI_STATES.forEach { uiState ->
            composeRule.runOnIdle { accountUiState.value = uiState }

            composeRule.onNodeWithContentDescription(DEFAULT_PROFILE_IMAGE_DESCRIPTION).performClick()
            composeRule.waitForIdle()
        }

        coVerify(exactly = 0) { photoPicker.open() }
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-021 QR 항목을 선택하면 QrHome 화면으로 이동한다`() {
        var navigateToQrCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToQr = { navigateToQrCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_QR_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToQrCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-022 디데이 항목을 선택하면 DDayHome 화면으로 이동한다`() {
        var navigateToDDayCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToDDay = { navigateToDDayCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_D_DAY_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToDDayCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-023 체크리스트 항목을 선택하면 ChecklistHome 화면으로 이동한다`() {
        var navigateToChecklistCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToChecklist = { navigateToChecklistCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_CHECKLIST_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToChecklistCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-024 파일 항목을 선택하면 FileHome 화면으로 이동한다`() {
        var navigateToFileCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToFile = { navigateToFileCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_FILE_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToFileCount shouldBe 1
    }

    @Test
    @Config(qualifiers = MENU_VISIBLE_QUALIFIERS)
    fun `TC-MORE-HOME-FEATURE-025 플레이리스트 항목을 선택하면 PlaylistHome 화면으로 이동한다`() {
        var navigateToPlaylistCount = 0
        val viewModel = screenTestViewModel(MoreHomeAccountUiState.Guest)
        setMoreHomeScreen(
            viewModel = viewModel,
            navigateToPlaylist = { navigateToPlaylistCount += 1 },
        )

        composeRule.onNodeWithText(DEFAULT_PLAYLIST_LABEL).performClick()
        composeRule.waitForIdle()

        navigateToPlaylistCount shouldBe 1
    }

    private fun setMoreHomeScreen(
        viewModel: MoreHomeAccountViewModel,
        navigateToChecklist: () -> Unit = {},
        navigateToContact: () -> Unit = {},
        navigateToDDay: () -> Unit = {},
        navigateToFile: () -> Unit = {},
        navigateToHoliday: () -> Unit = {},
        navigateToLogin: () -> Unit = {},
        navigateToPlace: () -> Unit = {},
        navigateToPlaylist: () -> Unit = {},
        navigateToQr: () -> Unit = {},
        navigateToSearch: () -> Unit = {},
        navigateToSetting: () -> Unit = {},
        navigateToWeb: () -> Unit = {},
        photoPicker: PhotoPicker = screenTestPhotoPicker(),
    ) {
        composeRule.setContent {
            DiaryTheme {
                MoreHomeScreen(
                    navigateToChecklist = navigateToChecklist,
                    navigateToContact = navigateToContact,
                    navigateToDDay = navigateToDDay,
                    navigateToFile = navigateToFile,
                    navigateToHoliday = navigateToHoliday,
                    navigateToLogin = navigateToLogin,
                    navigateToPlace = navigateToPlace,
                    navigateToPlaylist = navigateToPlaylist,
                    navigateToQr = navigateToQr,
                    navigateToSearch = navigateToSearch,
                    navigateToSetting = navigateToSetting,
                    navigateToWeb = navigateToWeb,
                    photoPicker = photoPicker,
                    viewModel = viewModel,
                )
            }
        }
    }

    public companion object {
        // 메뉴 격자는 화면에 보이는 항목만 배치하므로, 아래쪽 항목을 누르는 테스트에만 넉넉한 창을 준다.
        private const val MENU_VISIBLE_QUALIFIERS = "w480dp-h1600dp"

        private const val DEFAULT_SIGN_IN_LABEL = "Sign in"
        private const val DEFAULT_SIGN_OUT_LABEL = "Sign out"
        private const val DEFAULT_HOLIDAY_LABEL = "Golden Holiday"
        private const val DEFAULT_PLACE_LABEL = "Place"
        private const val DEFAULT_SEARCH_LABEL = "Search"
        private const val DEFAULT_SETTING_DESCRIPTION = "Settings"
        private const val DEFAULT_WEB_LABEL = "Web"
        private const val DEFAULT_PROFILE_IMAGE_DESCRIPTION = "Profile image"
        private const val DEFAULT_CONTACT_LABEL = "Contacts"
        private const val DEFAULT_QR_LABEL = "QR"
        private const val DEFAULT_D_DAY_LABEL = "D-Day"
        private const val DEFAULT_CHECKLIST_LABEL = "Checklist"
        private const val DEFAULT_FILE_LABEL = "Files"
        private const val DEFAULT_PLAYLIST_LABEL = "Playlist"
        private const val USER_EMAIL = "diary@example.com"
        private const val PICKED_PHOTO_URI = "content://media/external/images/media/1"
        private val NON_USER_ACCOUNT_UI_STATES =
            listOf(
                MoreHomeAccountUiState.Loading,
                MoreHomeAccountUiState.Guest,
            )

        private fun screenTestViewModel(uiState: MoreHomeAccountUiState): MoreHomeAccountViewModel {
            val viewModel = mockk<MoreHomeAccountViewModel>()
            every { viewModel.uiState } returns MutableStateFlow(uiState)
            every { viewModel.changeProfileImage(uri = any()) } returns Unit
            return viewModel
        }

        private fun screenTestPhotoPicker(pickedUri: FileUri? = null): PhotoPicker {
            val photoPicker = mockk<PhotoPicker>()
            coEvery { photoPicker.open() } returns pickedUri
            return photoPicker
        }
    }
}
