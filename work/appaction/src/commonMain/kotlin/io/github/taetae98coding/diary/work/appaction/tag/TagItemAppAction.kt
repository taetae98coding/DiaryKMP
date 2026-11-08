package io.github.taetae98coding.diary.work.appaction.tag

import io.github.taetae98coding.diary.domain.memo.usecase.PageFinishedTagMemoUseCase
import io.github.taetae98coding.diary.domain.memo.usecase.PageTagMemoUseCase
import io.github.taetae98coding.diary.domain.place.usecase.PageTagPlaceUseCase
import io.github.taetae98coding.diary.domain.web.usecase.PageTagWebUseCase
import io.github.taetae98coding.diary.work.appaction.core.AppActionPage
import io.github.taetae98coding.diary.work.appaction.core.map
import io.github.taetae98coding.diary.work.appaction.core.readPage
import io.github.taetae98coding.diary.work.appaction.core.runAppAction
import io.github.taetae98coding.diary.work.appaction.core.toPageOffset
import io.github.taetae98coding.diary.work.appaction.core.toSortArgument
import io.github.taetae98coding.diary.work.appaction.core.toUuidArgument
import io.github.taetae98coding.diary.work.appaction.memo.AppActionMemo
import io.github.taetae98coding.diary.work.appaction.memo.memoSortList
import io.github.taetae98coding.diary.work.appaction.memo.toAppActionMemo
import io.github.taetae98coding.diary.work.appaction.place.AppActionPlace
import io.github.taetae98coding.diary.work.appaction.place.toAppActionPlace
import io.github.taetae98coding.diary.work.appaction.web.AppActionWeb
import io.github.taetae98coding.diary.work.appaction.web.toAppActionWeb
import org.koin.core.annotation.Factory

@Factory
public class TagItemAppAction internal constructor(
    private val pageTagMemoUseCase: PageTagMemoUseCase,
    private val pageFinishedTagMemoUseCase: PageFinishedTagMemoUseCase,
    private val pageTagWebUseCase: PageTagWebUseCase,
    private val pageTagPlaceUseCase: PageTagPlaceUseCase,
) {
    public suspend fun memoList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageTagMemoUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(memoSortList))

            pageTagMemoUseCase
                .readPage(parameter = parameter, offset = cursor.toPageOffset())
                .map { memo -> memo.toAppActionMemo() }
        }

    public suspend fun memoFinishedList(
        tagId: String,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionMemo> =
        runAppAction {
            val parameter = PageFinishedTagMemoUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), sort = sort.toSortArgument(memoSortList))

            pageFinishedTagMemoUseCase
                .readPage(parameter = parameter, offset = cursor.toPageOffset())
                .map { memo -> memo.toAppActionMemo() }
        }

    public suspend fun webList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionWeb> =
        runAppAction {
            val parameter = PageTagWebUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(tagSortList))

            pageTagWebUseCase
                .readPage(parameter = parameter, offset = cursor.toPageOffset())
                .map { web -> web.toAppActionWeb() }
        }

    public suspend fun placeList(
        tagId: String,
        scope: String?,
        sort: String?,
        cursor: String?,
    ): AppActionPage<AppActionPlace> =
        runAppAction {
            val parameter = PageTagPlaceUseCase.Parameter(tagId = tagId.toUuidArgument(name = TAG_ID), scope = scope.toTagScopeArgument(), sort = sort.toSortArgument(tagSortList))

            pageTagPlaceUseCase
                .readPage(parameter = parameter, offset = cursor.toPageOffset())
                .map { place -> place.toAppActionPlace() }
        }
}
