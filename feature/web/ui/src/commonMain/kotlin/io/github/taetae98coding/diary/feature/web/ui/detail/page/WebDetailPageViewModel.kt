package io.github.taetae98coding.diary.feature.web.ui.detail.page

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
