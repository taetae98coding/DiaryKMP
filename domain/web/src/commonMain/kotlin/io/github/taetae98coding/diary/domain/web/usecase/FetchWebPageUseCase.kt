package io.github.taetae98coding.diary.domain.web.usecase

import io.github.taetae98coding.diary.core.model.web.WebHeader
import io.github.taetae98coding.diary.core.model.web.WebPage
import io.github.taetae98coding.diary.domain.core.UseCase
import io.github.taetae98coding.diary.domain.web.repository.WebPageRepository
import org.koin.core.annotation.Factory

@Factory
public class FetchWebPageUseCase internal constructor(
    private val webPageRepository: WebPageRepository,
) : UseCase<FetchWebPageUseCase.Parameter, WebPage>() {
    override suspend fun execute(parameter: Parameter): WebPage =
        webPageRepository.fetch(
            url = parameter.url,
            headerList = parameter.headerList,
        )

    public data class Parameter(
        val url: String,
        val headerList: List<WebHeader>,
    )
}
