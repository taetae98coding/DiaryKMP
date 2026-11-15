package io.github.taetae98coding.diary.feature.setting.ui.mcp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.appbar.DiaryNavigateUpTopBar
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.systemscroll.systemScroll
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.core.model.mcp.McpServerState
import io.github.taetae98coding.diary.feature.setting.ui.Res
import io.github.taetae98coding.diary.feature.setting.ui.setting_mcp_title
import io.github.taetae98coding.diary.feature.setting.ui.setting_navigate_up_button_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingMcpScaffold(
    onEvent: (SettingMcpScaffoldEvent) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    uiStateProvider: () -> SettingMcpUiState = { SettingMcpUiState.Loading },
    componentVisibleProvider: () -> SettingMcpScaffoldComponentVisible = { SettingMcpScaffoldComponentVisible() },
) {
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            DiaryNavigateUpTopBar(
                title = stringResource(Res.string.setting_mcp_title),
                onNavigateUp = { onEvent(SettingMcpScaffoldEvent.ClickNavigateUp) },
                navigateUpContentDescription = stringResource(Res.string.setting_navigate_up_button_content_description),
                isNavigateUpVisibleProvider = { componentVisibleProvider().isNavigateUpButtonVisible },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .systemScroll(listState) { listState.animateScrollToItem(0) },
            state = listState,
            contentPadding = DiaryTheme.dimens.screenPaddingValues,
            verticalArrangement = Arrangement.spacedBy(DiaryTheme.dimens.itemSpacing),
        ) {
            item(key = CONNECTION_HEADER_KEY) {
                SettingMcpConnectionHeader(modifier = Modifier.fillMaxWidth())
            }

            when (val uiState = uiStateProvider()) {
                is SettingMcpUiState.Loading -> Unit

                is SettingMcpUiState.Content -> {
                    item(key = CONNECTION_GROUP_KEY) {
                        SettingMcpConnectionGroup(
                            isEnabled = uiState.isEnabled,
                            serverState = uiState.serverState,
                            onEvent = onEvent,
                            modifier =
                                Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@ScreenPreview
@Composable
private fun SettingMcpScaffoldPreview() {
    DiaryTheme {
        SettingMcpScaffold(
            onEvent = {},
            uiStateProvider = {
                SettingMcpUiState.Content(
                    isEnabled = true,
                    serverState = McpServerState.Running(address = "http://127.0.0.1:38450/mcp"),
                )
            },
        )
    }
}

private const val CONNECTION_HEADER_KEY = "connectionHeader"
private const val CONNECTION_GROUP_KEY = "connectionGroup"
