package io.github.taetae98coding.diary.feature.web.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.domain.web.usecase.FetchWebPageUseCase
import io.github.taetae98coding.diary.domain.web.usecase.FindWebUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
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

    private var isStarted: Boolean = false
    private var job: Job? = null
    private var requestedDetail: WebDetail? = null

    fun load() {
        if (isStarted) return

        isStarted = true
        startLoad()
    }

    fun retry() {
        if (job?.isActive == true) return

        startLoad()
    }

    // 사용자가 실행한 수정으로 저장된 URL이나 요청 헤더가 바뀌었을 때만 다시 불러온다.
    // 아직 한 번도 불러온 적이 없으면 응답 본문 방식이 될 때 불러오므로 여기에서 요청하지 않는다.
    fun refresh() {
        if (!isStarted) return

        viewModelScope.launch {
            val detail = findWebDetail()
            val requested = requestedDetail

            if (requested != null && requested.url == detail.url && requested.headerList == detail.headerList) return@launch

            startLoad(detail = detail)
        }
    }

    private fun startLoad(detail: WebDetail? = null) {
        // 불러오는 동안 수정으로 대상이 바뀌면 앞선 요청의 결과를 화면에 반영하지 않는다.
        job?.cancel()
        uiState.value = WebDetailPageUiState.Loading

        job =
            viewModelScope.launch {
                val target = detail ?: findWebDetail()
                requestedDetail = target

                fetchWebPageUseCase(parameter = FetchWebPageUseCase.Parameter(url = target.url, headerList = target.headerList))
                    .onSuccess { page -> uiState.value = WebDetailPageUiState.Content(page = page) }
                    .onFailure { uiState.value = WebDetailPageUiState.Failure }
            }
    }

    private suspend fun findWebDetail(): WebDetail =
        findWebUseCase(parameter = id)
            .mapNotNull { result -> result.getOrNull()?.detail }
            .first()
}
