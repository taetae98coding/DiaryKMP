package io.github.taetae98coding.diary.feature.search.ui.home

import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.memo.Memo
import io.github.taetae98coding.diary.core.model.memo.MemoDetail
import io.github.taetae98coding.diary.core.model.place.Place
import io.github.taetae98coding.diary.core.model.place.PlaceDetail
import io.github.taetae98coding.diary.core.model.tag.Tag
import io.github.taetae98coding.diary.core.model.tag.TagDetail
import io.github.taetae98coding.diary.core.model.web.Web
import io.github.taetae98coding.diary.core.model.web.WebDetail
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val QUERY: String = "여행"
internal const val OTHER_QUERY: String = "회의"

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun <T : Any> successPagingDataFlowOf(itemList: List<T>): Flow<Result<PagingData<T>>> = flowOf(Result.success(PagingData.from(itemList)))

internal fun <T : Any> failurePagingDataFlow(): Flow<Result<PagingData<T>>> = flowOf(Result.failure(IllegalStateException()))

internal fun searchMemo(title: String): Memo =
    fixtureMonkey
        .giveMeKotlinBuilder<Memo>()
        .setExp(Memo::detail, fixtureMonkey.giveMeOne<MemoDetail>().copy(title = title, dateTime = null))
        .setExp(Memo::isDeleted, false)
        .setExp(Memo::updatedAt, instant())
        .setExp(Memo::createdAt, instant())
        .sample()

internal fun searchTag(title: String): Tag =
    fixtureMonkey
        .giveMeKotlinBuilder<Tag>()
        .setExp(Tag::detail, fixtureMonkey.giveMeOne<TagDetail>().copy(title = title))
        .setExp(Tag::isDeleted, false)
        .setExp(Tag::updatedAt, instant())
        .setExp(Tag::createdAt, instant())
        .sample()

internal fun searchPlace(title: String): Place =
    fixtureMonkey
        .giveMeKotlinBuilder<Place>()
        .setExp(Place::detail, fixtureMonkey.giveMeOne<PlaceDetail>().copy(title = title))
        .setExp(Place::isDeleted, false)
        .setExp(Place::updatedAt, instant())
        .setExp(Place::createdAt, instant())
        .sample()

internal fun searchWeb(title: String): Web =
    fixtureMonkey
        .giveMeKotlinBuilder<Web>()
        .setExp(Web::detail, fixtureMonkey.giveMeOne<WebDetail>().copy(title = title, headerList = emptyList()))
        .setExp(Web::isDeleted, false)
        .setExp(Web::updatedAt, instant())
        .setExp(Web::createdAt, instant())
        .sample()

private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

internal fun searchId(): Uuid = fixtureMonkey.giveMeOne()
