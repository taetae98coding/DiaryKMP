package io.github.taetae98coding.diary.feature.memo.ui.form

import androidx.compose.runtime.Composable
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import io.github.taetae98coding.diary.core.model.contact.Contact
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.web.Web
import kotlinx.coroutines.flow.Flow

internal class MemoFormSelectablePagingItems(
    val tag: LazyPagingItems<Tag>,
    val web: LazyPagingItems<Web>,
    val contact: LazyPagingItems<Contact>,
    val place: LazyPagingItems<Place>,
)

@Composable
internal fun collectMemoFormSelectablePagingItems(
    tag: Flow<PagingData<Tag>>,
    web: Flow<PagingData<Web>>,
    contact: Flow<PagingData<Contact>>,
    place: Flow<PagingData<Place>>,
): MemoFormSelectablePagingItems =
    MemoFormSelectablePagingItems(
        tag = tag.collectAsLazyPagingItems(),
        web = web.collectAsLazyPagingItems(),
        contact = contact.collectAsLazyPagingItems(),
        place = place.collectAsLazyPagingItems(),
    )
