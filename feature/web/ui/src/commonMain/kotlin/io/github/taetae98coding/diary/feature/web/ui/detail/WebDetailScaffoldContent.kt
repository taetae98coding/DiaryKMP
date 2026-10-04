package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.layout.isCompactWidth
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.compose.web.previewWebPage
import io.github.taetae98coding.diary.feature.core.memo.EntityDetailMemoTab
import io.github.taetae98coding.diary.feature.web.ui.Res
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTabRow
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.webDetailStartTabList
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_empty_description
import io.github.taetae98coding.diary.feature.web.ui.web_detail_memo_empty_title
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

private const val MEMO_CONTENT_STATE_KEY: String = "WebDetailMemoContent"

@Composable
internal fun WebDetailScaffoldContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier = Modifier,
    state: WebDetailScaffoldState = rememberWebDetailScaffoldState(),
    formState: WebFormState = rememberWebDetailFormState(),
    isChangedProvider: () -> Boolean = { false },
    uiStateProvider: () -> WebDetailUiState = { WebDetailUiState.Loading },
    pageUiStateProvider: () -> WebDetailPageUiState = { WebDetailPageUiState.Loading },
    tagUiStateProvider: () -> EntityTagInputUiState = { EntityTagInputUiState() },
    memoContent: @Composable () -> Unit,
) {
    // 다른 탭으로 바꾸면 메모 탭의 구성이 사라지므로 돌아왔을 때 목록 위치를 되찾도록 저장 상태를 탭 밖에 보관한다.
    // 창 너비가 바뀌면 새 배치가 먼저 구성되고 이전 배치가 나중에 사라져 저장 상태를 넘겨받지 못하므로, 메모 탭의 구성 자체를 새 배치로 옮긴다.
    val memoStateHolder = rememberSaveableStateHolder()
    val currentMemoContent by rememberUpdatedState(memoContent)
    val movableMemoContent =
        remember(memoStateHolder) {
            movableContentOf {
                memoStateHolder.SaveableStateProvider(key = MEMO_CONTENT_STATE_KEY) { currentMemoContent() }
            }
        }

    if (isCompactWidth()) {
        CompactContent(
            onEvent = onEvent,
            onFormEvent = onFormEvent,
            modifier = modifier,
            state = state,
            formState = formState,
            isChangedProvider = isChangedProvider,
            uiStateProvider = uiStateProvider,
            pageUiStateProvider = pageUiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
            memoContent = movableMemoContent,
        )
    } else {
        WideContent(
            onEvent = onEvent,
            onFormEvent = onFormEvent,
            modifier = modifier,
            state = state,
            formState = formState,
            isChangedProvider = isChangedProvider,
            uiStateProvider = uiStateProvider,
            pageUiStateProvider = pageUiStateProvider,
            tagUiStateProvider = tagUiStateProvider,
            memoContent = movableMemoContent,
        )
    }
}

@Composable
private fun CompactContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier,
    state: WebDetailScaffoldState,
    formState: WebFormState,
    isChangedProvider: () -> Boolean,
    uiStateProvider: () -> WebDetailUiState,
    pageUiStateProvider: () -> WebDetailPageUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
    memoContent: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        WebDetailTabRow(
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth(),
            state = state,
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
        ) {
            DiaryCrossfade(
                targetState = state.tab,
                modifier = Modifier.fillMaxSize(),
            ) { tab ->
                when (tab) {
                    WebDetailTab.FORM ->
                        WebDetailFormArea(
                            onFormEvent = onFormEvent,
                            modifier = Modifier.fillMaxSize(),
                            formState = formState,
                            uiStateProvider = uiStateProvider,
                            tagUiStateProvider = tagUiStateProvider,
                        )

                    WebDetailTab.PAGE ->
                        WebDetailPageArea(
                            onEvent = onEvent,
                            modifier = Modifier.fillMaxSize(),
                            state = state,
                            uiStateProvider = uiStateProvider,
                            pageUiStateProvider = pageUiStateProvider,
                        )

                    WebDetailTab.MEMO -> memoContent()
                }
            }

            WebDetailTabFloatingActionButton(
                onEvent = onEvent,
                tabProvider = { state.tab },
                modifier =
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(DiaryTheme.dimens.screenPaddingValues),
                isChangedProvider = isChangedProvider,
                uiStateProvider = uiStateProvider,
            )
        }
    }
}

@Composable
private fun WideContent(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier,
    state: WebDetailScaffoldState,
    formState: WebFormState,
    isChangedProvider: () -> Boolean,
    uiStateProvider: () -> WebDetailUiState,
    pageUiStateProvider: () -> WebDetailPageUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
    memoContent: @Composable () -> Unit,
) {
    Row(modifier = modifier) {
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1F),
        ) {
            WebDetailTabRow(
                onEvent = onEvent,
                modifier = Modifier.fillMaxWidth(),
                state = state,
                tabList = webDetailStartTabList,
                selectedTabProvider = { state.startTab },
            )

            StartTabArea(
                onEvent = onEvent,
                onFormEvent = onFormEvent,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1F),
                state = state,
                formState = formState,
                isChangedProvider = isChangedProvider,
                uiStateProvider = uiStateProvider,
                tagUiStateProvider = tagUiStateProvider,
                memoContent = memoContent,
            )
        }
        WebDetailPageArea(
            onEvent = onEvent,
            modifier =
                Modifier
                    .fillMaxHeight()
                    .weight(1F),
            state = state,
            uiStateProvider = uiStateProvider,
            pageUiStateProvider = pageUiStateProvider,
        )
    }
}

// 조회 중과 내용 표시 사이에서만 전환한다. 내용이 갱신될 때마다 전환하면 입력 영역의 스크롤 위치가 처음으로 돌아간다.
@Composable
private fun StartTabArea(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier,
    state: WebDetailScaffoldState,
    formState: WebFormState,
    isChangedProvider: () -> Boolean,
    uiStateProvider: () -> WebDetailUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
    memoContent: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        DiaryCrossfade(
            targetState = state.startTab,
            modifier = Modifier.fillMaxSize(),
        ) { tab ->
            when (tab) {
                WebDetailTab.FORM ->
                    WebDetailFormArea(
                        onFormEvent = onFormEvent,
                        modifier = Modifier.fillMaxSize(),
                        formState = formState,
                        uiStateProvider = uiStateProvider,
                        tagUiStateProvider = tagUiStateProvider,
                    )

                WebDetailTab.MEMO -> memoContent()

                // 시작 쪽 탭은 수정 폼과 메모만 가지므로 웹 페이지 탭은 이 자리에 오지 않는다.
                WebDetailTab.PAGE -> Unit
            }
        }

        WebDetailTabFloatingActionButton(
            onEvent = onEvent,
            tabProvider = { state.startTab },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(DiaryTheme.dimens.screenPaddingValues),
            isChangedProvider = isChangedProvider,
            uiStateProvider = uiStateProvider,
        )
    }
}

@ScreenPreview
@Composable
private fun WebDetailScaffoldContentPreview() {
    DiaryTheme {
        Surface {
            WebDetailScaffoldContent(
                onEvent = {},
                onFormEvent = {},
                modifier = Modifier.fillMaxSize(),
                formState = rememberWebDetailFormState(initialDetail = previewWebDetail()),
                uiStateProvider = { WebDetailUiState.Content(id = Uuid.NIL, detail = previewWebDetail()) },
                pageUiStateProvider = { WebDetailPageUiState.Content(page = previewWebPage()) },
            ) {
                EntityDetailMemoTab(emptyTitle = stringResource(Res.string.web_detail_memo_empty_title), emptyDescription = stringResource(Res.string.web_detail_memo_empty_description), onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
