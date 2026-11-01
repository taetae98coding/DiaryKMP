package io.github.taetae98coding.diary.feature.web.ui.detail.page

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.domain.web.usecase.FetchWebPageUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import kotlin.uuid.Uuid

@KoinViewModel
internal class WebDetailPageViewModel(
    @InjectedParam private val id: Uuid,
    private val findWebUseCase: FindWebUseCase,
    private val fetchWebPageUseCase: FetchWebPageUseCase,
) : ViewModel() {
    val uiState: StateFlow<WebDetailPageUiState>
        field = MutableStateFlow<WebDetailPageUiState>(WebDetailPageUiState.Loading)

    private val retryCount = MutableStateFlow(0)
    private var isStarted: Boolean = false

    fun load() {
        if (isStarted) return
        isStarted = true

        viewModelScope.launch {
            val requestFlow =
                findWebUseCase(parameter = id)
                    .mapNotNull { result -> result.getOrNull()?.detail }
                    .map { detail -> FetchWebPageUseCase.Parameter(url = detail.url, headerList = detail.headerList) }
                    .distinctUntilChanged()

            // 새 요청이 오면 진행 중이던 불러오기를 취소해 최신 저장 내용의 결과만 반영한다.
            combine(requestFlow, retryCount) { parameter, _ -> parameter }
                .collectLatest { parameter ->
                    uiState.value = WebDetailPageUiState.Loading
                    fetchWebPageUseCase(parameter = parameter)
                        .onSuccess { page -> uiState.value = WebDetailPageUiState.Content(page = page) }
                        .onFailure { uiState.value = WebDetailPageUiState.Failure }
                }
        }
    }

    fun retry() {
        // 다시 시도는 실패 안내에서만 제공하므로 불러오는 동안 전달된 요청은 처리하지 않는다.
        if (uiState.value != WebDetailPageUiState.Failure) return

        uiState.value = WebDetailPageUiState.Loading
        retryCount.update { count -> count + 1 }
    }
}
