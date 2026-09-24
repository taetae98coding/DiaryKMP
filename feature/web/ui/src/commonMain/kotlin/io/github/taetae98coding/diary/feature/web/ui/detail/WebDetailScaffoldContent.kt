package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.taetae98coding.diary.compose.core.animation.DiaryCrossfade
import io.github.taetae98coding.diary.compose.core.layout.isCompactWidth
import io.github.taetae98coding.diary.compose.core.loading.DiaryLoadingBox
import io.github.taetae98coding.diary.compose.core.preview.ScreenPreview
import io.github.taetae98coding.diary.compose.core.theme.DiaryTheme
import io.github.taetae98coding.diary.compose.tag.entity.EntityTagInputUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.memo.WebDetailMemoTab
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPage
import io.github.taetae98coding.diary.feature.web.ui.detail.page.WebDetailPageUiState
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTab
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.WebDetailTabRow
import io.github.taetae98coding.diary.feature.web.ui.detail.tab.webDetailStartTabList
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormEvent
import io.github.taetae98coding.diary.feature.web.ui.form.WebFormState
import io.github.taetae98coding.diary.feature.web.ui.form.rememberWebDetailFormState
import io.github.taetae98coding.diary.feature.web.ui.previewWebDetail
import io.github.taetae98coding.diary.feature.web.ui.previewWebPage
import kotlin.uuid.Uuid

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
            memoContent = memoContent,
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
            memoContent = memoContent,
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

        DiaryCrossfade(
            targetState = state.tab,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1F),
        ) { tab ->
            when (tab) {
                WebDetailTab.FORM ->
                    FormArea(
                        onEvent = onEvent,
                        onFormEvent = onFormEvent,
                        modifier = Modifier.fillMaxSize(),
                        formState = formState,
                        isChangedProvider = isChangedProvider,
                        uiStateProvider = uiStateProvider,
                        tagUiStateProvider = tagUiStateProvider,
                    )

                WebDetailTab.PAGE ->
                    PageArea(
                        onEvent = onEvent,
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        uiStateProvider = uiStateProvider,
                        pageUiStateProvider = pageUiStateProvider,
                    )

                WebDetailTab.MEMO -> memoContent()
            }
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

            DiaryCrossfade(
                targetState = state.startTab,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1F),
            ) { tab ->
                when (tab) {
                    WebDetailTab.FORM ->
                        FormArea(
                            onEvent = onEvent,
                            onFormEvent = onFormEvent,
                            modifier = Modifier.fillMaxSize(),
                            formState = formState,
                            isChangedProvider = isChangedProvider,
                            uiStateProvider = uiStateProvider,
                            tagUiStateProvider = tagUiStateProvider,
                        )

                    WebDetailTab.MEMO -> memoContent()

                    // 시작 쪽 탭은 수정 폼과 메모만 가지므로 웹 페이지 탭은 이 자리에 오지 않는다.
                    WebDetailTab.PAGE -> Unit
                }
            }
        }
        PageArea(
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
private fun FormArea(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    onFormEvent: (WebFormEvent) -> Unit,
    modifier: Modifier,
    formState: WebFormState,
    isChangedProvider: () -> Boolean,
    uiStateProvider: () -> WebDetailUiState,
    tagUiStateProvider: () -> EntityTagInputUiState,
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is WebDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            WebDetailForm(
                onEvent = onEvent,
                onFormEvent = onFormEvent,
                modifier = Modifier.fillMaxSize(),
                state = formState,
                isChangedProvider = isChangedProvider,
                uiStateProvider = uiStateProvider,
                tagUiStateProvider = tagUiStateProvider,
            )
        } else {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun PageArea(
    onEvent: (WebDetailScaffoldEvent) -> Unit,
    modifier: Modifier,
    state: WebDetailScaffoldState,
    uiStateProvider: () -> WebDetailUiState,
    pageUiStateProvider: () -> WebDetailPageUiState,
) {
    DiaryCrossfade(
        targetState = uiStateProvider() is WebDetailUiState.Content,
        modifier = modifier,
    ) { isContent ->
        if (isContent) {
            WebDetailPage(
                onEvent = onEvent,
                modifier = Modifier.fillMaxSize(),
                state = state,
                urlProvider = { uiStateProvider().urlOrEmpty() },
                uiStateProvider = pageUiStateProvider,
            )
        } else {
            DiaryLoadingBox(modifier = Modifier.fillMaxSize())
        }
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
                WebDetailMemoTab(onEvent = {}, onMemoListEvent = {}, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
