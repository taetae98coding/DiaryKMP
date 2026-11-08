package io.github.taetae98coding.diary.work.appaction.search

import io.github.taetae98coding.diary.core.model.list.ListSort
import io.github.taetae98coding.diary.domain.search.usecase.SearchDDayUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchMemoUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchPlaceUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchTagUseCase
import io.github.taetae98coding.diary.domain.search.usecase.SearchWebUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.dday.AppActionDDay
import io.github.taetae98coding.diary.work.appaction.dday.toAppActionDDay
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.toAppActionMemo
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.place.toAppActionPlace
import io.github.taetae98coding.diary.work.appaction.tag.AppActionTag
import io.github.taetae98coding.diary.work.appaction.tag.toAppActionTag
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.github.taetae98coding.diary.work.appaction.web.toAppActionWeb
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.core.annotation.Factory
import kotlin.time.Clock

internal val searchSortList: List<ListSort> = listOf(ListSort.TITLE, ListSort.RECENTLY_UPDATED)

@Factory
public class SearchAppAction internal constructor(
    private val searchMemoUseCase: SearchMemoUseCase,
    private val searchTagUseCase: SearchTagUseCase,
    private val searchPlaceUseCase: SearchPlaceUseCase,
    private val searchWebUseCase: SearchWebUseCase,
    private val searchDDayUseCase: SearchDDayUseCase,
    private val clock: Clock,
) {
    public suspend fun memo(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            searchMemoUseCase
                .readPage(
                    parameter = SearchMemoUseCase.Parameter(query = query, sort = sort.toSortArgument(searchSortList)),
                    offset = cursor.toPageOffset(),
                ).map { memo -> memo.toAppActionMemo() }
        }

    public suspend fun tag(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionTag> =
        runAppAction {
            searchTagUseCase
                .readPage(
                    parameter = SearchTagUseCase.Parameter(query = query, sort = sort.toSortArgument(searchSortList)),
                    offset = cursor.toPageOffset(),
                ).map { tag -> tag.toAppActionTag() }
        }

    public suspend fun place(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionPlace> =
        runAppAction {
            searchPlaceUseCase
                .readPage(
                    parameter = SearchPlaceUseCase.Parameter(query = query, sort = sort.toSortArgument(searchSortList)),
                    offset = cursor.toPageOffset(),
                ).map { place -> place.toAppActionPlace() }
        }

    public suspend fun web(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionWeb> =
        runAppAction {
            searchWebUseCase
                .readPage(
                    parameter = SearchWebUseCase.Parameter(query = query, sort = sort.toSortArgument(searchSortList)),
                    offset = cursor.toPageOffset(),
                ).map { web -> web.toAppActionWeb() }
        }

    public suspend fun dday(
        query: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionDDay> =
        runAppAction {
            val today = clock.todayIn(TimeZone.currentSystemDefault())

            searchDDayUseCase
                .readPage(
                    parameter = SearchDDayUseCase.Parameter(query = query, sort = sort.toSortArgument(searchSortList)),
                    offset = cursor.toPageOffset(),
                ).map { dday -> dday.toAppActionDDay(today = today) }
        }
}
