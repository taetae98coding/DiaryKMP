package io.github.taetae98coding.diary.feature.qr.ui.home

import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.qr.Qr
import io.github.taetae98coding.diary.core.testing.qr.qr
import io.github.taetae98coding.diary.core.testing.qr.qrDetail
import io.github.taetae98coding.diary.feature.qr.ui.code.QrCodeValueKey
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal fun qrPagingDataOf(qrList: List<Qr>): PagingData<Qr> =
    PagingData.from(
        data = qrList,
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = true),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
    )

internal fun loadingQrPagingData(): PagingData<Qr> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Loading,
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun failedQrPagingData(): PagingData<Qr> =
    PagingData.from(
        data = emptyList(),
        sourceLoadStates =
            LoadStates(
                refresh = LoadState.Error(IllegalStateException(fixtureMonkey.giveMeOne<String>())),
                prepend = LoadState.NotLoading(endOfPaginationReached = false),
                append = LoadState.NotLoading(endOfPaginationReached = false),
            ),
    )

internal fun testQr(titlePrefix: String = ""): Qr =
    fixtureMonkey.qrDetail().let { detail ->
        fixtureMonkey.qr(isDeleted = false, detail = detail.copy(title = titlePrefix + detail.title))
    }

internal fun SemanticsNodeInteractionsProvider.qrCodeValueList(): List<String> =
    onAllNodes(SemanticsMatcher.keyIsDefined(QrCodeValueKey), useUnmergedTree = true)
        .fetchSemanticsNodes()
        .map { node -> node.config[QrCodeValueKey] }
