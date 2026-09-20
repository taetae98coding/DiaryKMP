package io.github.taetae98coding.diary.work.sync.work

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeKotlinBuilder
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.database.api.contact.datasource.AccountContactSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.contact.entity.ContactLocalEntity
import io.github.taetae98coding.diary.core.database.api.contact.transaction.AccountContactSyncTransaction
import io.github.taetae98coding.diary.core.database.api.memo.datasource.AccountMemoSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.memo.transaction.AccountMemoSyncTransaction
import io.github.taetae98coding.diary.core.database.api.memoplace.datasource.AccountMemoPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoplace.entity.MemoPlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoplace.transaction.AccountMemoPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.memotag.datasource.AccountMemoTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memotag.entity.MemoTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.memotag.transaction.AccountMemoTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.memoweb.datasource.AccountMemoWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.memoweb.entity.MemoWebLocalEntity
import io.github.taetae98coding.diary.core.database.api.memoweb.transaction.AccountMemoWebSyncTransaction
import io.github.taetae98coding.diary.core.database.api.music.datasource.AccountMusicSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.music.entity.MusicLocalEntity
import io.github.taetae98coding.diary.core.database.api.music.transaction.AccountMusicSyncTransaction
import io.github.taetae98coding.diary.core.database.api.place.datasource.AccountPlaceSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.place.entity.PlaceLocalEntity
import io.github.taetae98coding.diary.core.database.api.place.transaction.AccountPlaceSyncTransaction
import io.github.taetae98coding.diary.core.database.api.placetag.datasource.AccountPlaceTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.placetag.entity.PlaceTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.placetag.transaction.AccountPlaceTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.sync.datasource.SyncCursorLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.datasource.AccountTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.tag.entity.TagLocalEntity
import io.github.taetae98coding.diary.core.database.api.tag.transaction.AccountTagSyncTransaction
import io.github.taetae98coding.diary.core.database.api.taglink.datasource.AccountTagLinkSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.taglink.entity.TagLinkLocalEntity
import io.github.taetae98coding.diary.core.database.api.taglink.transaction.AccountTagLinkSyncTransaction
import io.github.taetae98coding.diary.core.database.api.web.datasource.AccountWebSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.web.entity.WebLocalEntity
import io.github.taetae98coding.diary.core.database.api.web.transaction.AccountWebSyncTransaction
import io.github.taetae98coding.diary.core.database.api.webtag.datasource.AccountWebTagSyncLocalDataSource
import io.github.taetae98coding.diary.core.database.api.webtag.entity.WebTagLocalEntity
import io.github.taetae98coding.diary.core.database.api.webtag.transaction.AccountWebTagSyncTransaction
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.network.api.contact.datasource.ContactRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.contact.entity.ContactPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memo.datasource.MemoRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memo.entity.MemoPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoplace.datasource.MemoPlaceRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memoplace.entity.MemoPlacePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memotag.datasource.MemoTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memotag.entity.MemoTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.memoweb.datasource.MemoWebRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.memoweb.entity.MemoWebPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.music.datasource.MusicRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.music.entity.MusicPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.place.datasource.PlaceRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.place.entity.PlacePullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.placetag.datasource.PlaceTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.placetag.entity.PlaceTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.tag.datasource.TagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.tag.entity.TagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.taglink.datasource.TagLinkRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.taglink.entity.TagLinkPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.web.datasource.WebRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.web.entity.WebPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.webtag.datasource.WebTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.webtag.entity.WebTagPullRemoteEntity
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.github.taetae98coding.diary.logger.core.DiaryLog
import io.github.taetae98coding.diary.logger.core.DiaryLogger
import io.github.taetae98coding.diary.logger.core.DiaryLoggerDelegate
import io.github.taetae98coding.diary.logger.crashlytics.api.CrashlyticsLog
import io.github.taetae98coding.diary.work.sync.mapper.toRemote
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal const val PULL_DELAY: Long = 1_000

internal const val PUSH_DELAY: Long = 1_000

internal val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

internal data class TestContext(
    val accountId: Uuid,
    val getAccountUseCase: GetAccountUseCase,
    val tagSyncLocalDataSource: AccountTagSyncLocalDataSource,
    val placeSyncLocalDataSource: AccountPlaceSyncLocalDataSource,
    val webSyncLocalDataSource: AccountWebSyncLocalDataSource,
    val contactSyncLocalDataSource: AccountContactSyncLocalDataSource,
    val musicSyncLocalDataSource: AccountMusicSyncLocalDataSource,
    val memoSyncLocalDataSource: AccountMemoSyncLocalDataSource,
    val memoTagSyncLocalDataSource: AccountMemoTagSyncLocalDataSource,
    val memoPlaceSyncLocalDataSource: AccountMemoPlaceSyncLocalDataSource,
    val memoWebSyncLocalDataSource: AccountMemoWebSyncLocalDataSource,
    val tagLinkSyncLocalDataSource: AccountTagLinkSyncLocalDataSource,
    val webTagSyncLocalDataSource: AccountWebTagSyncLocalDataSource,
    val placeTagSyncLocalDataSource: AccountPlaceTagSyncLocalDataSource,
    val syncCursorLocalDataSource: SyncCursorLocalDataSource,
    val accountTagSyncTransaction: AccountTagSyncTransaction,
    val accountPlaceSyncTransaction: AccountPlaceSyncTransaction,
    val accountWebSyncTransaction: AccountWebSyncTransaction,
    val accountContactSyncTransaction: AccountContactSyncTransaction,
    val accountMemoSyncTransaction: AccountMemoSyncTransaction,
    val accountMemoTagSyncTransaction: AccountMemoTagSyncTransaction,
    val accountMemoPlaceSyncTransaction: AccountMemoPlaceSyncTransaction,
    val accountMemoWebSyncTransaction: AccountMemoWebSyncTransaction,
    val accountTagLinkSyncTransaction: AccountTagLinkSyncTransaction,
    val accountWebTagSyncTransaction: AccountWebTagSyncTransaction,
    val accountPlaceTagSyncTransaction: AccountPlaceTagSyncTransaction,
    val accountMusicSyncTransaction: AccountMusicSyncTransaction,
    val tagRemoteDataSource: TagRemoteDataSource,
    val placeRemoteDataSource: PlaceRemoteDataSource,
    val webRemoteDataSource: WebRemoteDataSource,
    val contactRemoteDataSource: ContactRemoteDataSource,
    val memoRemoteDataSource: MemoRemoteDataSource,
    val memoTagRemoteDataSource: MemoTagRemoteDataSource,
    val memoPlaceRemoteDataSource: MemoPlaceRemoteDataSource,
    val memoWebRemoteDataSource: MemoWebRemoteDataSource,
    val tagLinkRemoteDataSource: TagLinkRemoteDataSource,
    val webTagRemoteDataSource: WebTagRemoteDataSource,
    val placeTagRemoteDataSource: PlaceTagRemoteDataSource,
    val musicRemoteDataSource: MusicRemoteDataSource,
) {
    val subject: SyncWorkImpl =
        SyncWorkImpl(
            getAccountUseCase = getAccountUseCase,
            tagSyncWork =
                TagSyncWork(
                    accountTagSyncLocalDataSource = tagSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountTagSyncTransaction = accountTagSyncTransaction,
                    tagRemoteDataSource = tagRemoteDataSource,
                ),
            placeSyncWork =
                PlaceSyncWork(
                    accountPlaceSyncLocalDataSource = placeSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountPlaceSyncTransaction = accountPlaceSyncTransaction,
                    placeRemoteDataSource = placeRemoteDataSource,
                ),
            webSyncWork =
                WebSyncWork(
                    accountWebSyncLocalDataSource = webSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountWebSyncTransaction = accountWebSyncTransaction,
                    webRemoteDataSource = webRemoteDataSource,
                ),
            contactSyncWork =
                ContactSyncWork(
                    accountContactSyncLocalDataSource = contactSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountContactSyncTransaction = accountContactSyncTransaction,
                    contactRemoteDataSource = contactRemoteDataSource,
                ),
            musicSyncWork =
                MusicSyncWork(
                    accountMusicSyncLocalDataSource = musicSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountMusicSyncTransaction = accountMusicSyncTransaction,
                    musicRemoteDataSource = musicRemoteDataSource,
                ),
            memoSyncWork =
                MemoSyncWork(
                    accountMemoSyncLocalDataSource = memoSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountMemoSyncTransaction = accountMemoSyncTransaction,
                    memoRemoteDataSource = memoRemoteDataSource,
                ),
            memoTagSyncWork =
                MemoTagSyncWork(
                    accountMemoTagSyncLocalDataSource = memoTagSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountMemoTagSyncTransaction = accountMemoTagSyncTransaction,
                    memoTagRemoteDataSource = memoTagRemoteDataSource,
                ),
            memoPlaceSyncWork =
                MemoPlaceSyncWork(
                    accountMemoPlaceSyncLocalDataSource = memoPlaceSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountMemoPlaceSyncTransaction = accountMemoPlaceSyncTransaction,
                    memoPlaceRemoteDataSource = memoPlaceRemoteDataSource,
                ),
            memoWebSyncWork =
                MemoWebSyncWork(
                    accountMemoWebSyncLocalDataSource = memoWebSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountMemoWebSyncTransaction = accountMemoWebSyncTransaction,
                    memoWebRemoteDataSource = memoWebRemoteDataSource,
                ),
            tagLinkSyncWork =
                TagLinkSyncWork(
                    accountTagLinkSyncLocalDataSource = tagLinkSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountTagLinkSyncTransaction = accountTagLinkSyncTransaction,
                    tagLinkRemoteDataSource = tagLinkRemoteDataSource,
                ),
            webTagSyncWork =
                WebTagSyncWork(
                    accountWebTagSyncLocalDataSource = webTagSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountWebTagSyncTransaction = accountWebTagSyncTransaction,
                    webTagRemoteDataSource = webTagRemoteDataSource,
                ),
            placeTagSyncWork =
                PlaceTagSyncWork(
                    accountPlaceTagSyncLocalDataSource = placeTagSyncLocalDataSource,
                    syncCursorLocalDataSource = syncCursorLocalDataSource,
                    accountPlaceTagSyncTransaction = accountPlaceTagSyncTransaction,
                    placeTagRemoteDataSource = placeTagRemoteDataSource,
                ),
        )
}

internal fun sessionValidUser(accountId: Uuid): Account.User =
    fixtureMonkey
        .giveMeKotlinBuilder<Account.User>()
        .setExp(Account.User::id, accountId)
        .setExp(Account.User::isSessionValid, true)
        .sample()

internal fun sessionInvalidUser(accountId: Uuid): Account.User =
    fixtureMonkey
        .giveMeKotlinBuilder<Account.User>()
        .setExp(Account.User::id, accountId)
        .setExp(Account.User::isSessionValid, false)
        .sample()

internal class TestException(
    message: String,
) : RuntimeException(message)

internal fun context(
    accountId: Uuid = fixtureMonkey.giveMeOne(),
    accountFlow: Flow<Result<Account>> = flowOf(Result.success(sessionValidUser(accountId = accountId))),
    tagList: List<TagLocalEntity> = emptyList(),
    placeList: List<PlaceLocalEntity> = emptyList(),
    webList: List<WebLocalEntity> = emptyList(),
    contactList: List<ContactLocalEntity> = emptyList(),
    memoList: List<MemoLocalEntity> = emptyList(),
    memoTagList: List<MemoTagLocalEntity> = emptyList(),
    memoPlaceList: List<MemoPlaceLocalEntity> = emptyList(),
    memoWebList: List<MemoWebLocalEntity> = emptyList(),
    tagLinkList: List<TagLinkLocalEntity> = emptyList(),
    webTagList: List<WebTagLocalEntity> = emptyList(),
    placeTagList: List<PlaceTagLocalEntity> = emptyList(),
    musicList: List<MusicLocalEntity> = emptyList(),
): TestContext {
    val context = mockedTestContext(accountId = accountId, accountFlow = accountFlow)

    context.stubPending(
        tagList = tagList,
        placeList = placeList,
        webList = webList,
        contactList = contactList,
        memoList = memoList,
        memoTagList = memoTagList,
        memoPlaceList = memoPlaceList,
        memoWebList = memoWebList,
        tagLinkList = tagLinkList,
        webTagList = webTagList,
        placeTagList = placeTagList,
        musicList = musicList,
    )
    context.stubTransactions()
    context.stubRemoteDataSources()

    return context
}

private fun mockedTestContext(
    accountId: Uuid,
    accountFlow: Flow<Result<Account>>,
): TestContext =
    TestContext(
        accountId = accountId,
        getAccountUseCase = mockk<GetAccountUseCase>().apply { every { this@apply(parameter = Unit) } returns accountFlow },
        tagSyncLocalDataSource = mockk(),
        placeSyncLocalDataSource = mockk(),
        webSyncLocalDataSource = mockk(),
        contactSyncLocalDataSource = mockk(),
        musicSyncLocalDataSource = mockk(),
        memoSyncLocalDataSource = mockk(),
        memoTagSyncLocalDataSource = mockk(),
        memoPlaceSyncLocalDataSource = mockk(),
        memoWebSyncLocalDataSource = mockk(),
        tagLinkSyncLocalDataSource = mockk(),
        webTagSyncLocalDataSource = mockk(),
        placeTagSyncLocalDataSource = mockk(),
        syncCursorLocalDataSource = mockk(),
        accountTagSyncTransaction = mockk(),
        accountPlaceSyncTransaction = mockk(),
        accountWebSyncTransaction = mockk(),
        accountContactSyncTransaction = mockk(),
        accountMemoSyncTransaction = mockk(),
        accountMemoTagSyncTransaction = mockk(),
        accountMemoPlaceSyncTransaction = mockk(),
        accountMemoWebSyncTransaction = mockk(),
        accountTagLinkSyncTransaction = mockk(),
        accountWebTagSyncTransaction = mockk(),
        accountPlaceTagSyncTransaction = mockk(),
        accountMusicSyncTransaction = mockk(),
        tagRemoteDataSource = mockk(),
        placeRemoteDataSource = mockk(),
        webRemoteDataSource = mockk(),
        contactRemoteDataSource = mockk(),
        memoRemoteDataSource = mockk(),
        memoTagRemoteDataSource = mockk(),
        memoPlaceRemoteDataSource = mockk(),
        memoWebRemoteDataSource = mockk(),
        tagLinkRemoteDataSource = mockk(),
        webTagRemoteDataSource = mockk(),
        placeTagRemoteDataSource = mockk(),
        musicRemoteDataSource = mockk(),
    )

private fun TestContext.stubPending(
    tagList: List<TagLocalEntity>,
    placeList: List<PlaceLocalEntity>,
    webList: List<WebLocalEntity>,
    contactList: List<ContactLocalEntity>,
    memoList: List<MemoLocalEntity>,
    memoTagList: List<MemoTagLocalEntity>,
    memoPlaceList: List<MemoPlaceLocalEntity>,
    memoWebList: List<MemoWebLocalEntity>,
    tagLinkList: List<TagLinkLocalEntity>,
    webTagList: List<WebTagLocalEntity>,
    placeTagList: List<PlaceTagLocalEntity>,
    musicList: List<MusicLocalEntity>,
) {
    coEvery { tagSyncLocalDataSource.findPending(accountId = accountId) } returns tagList
    coEvery { placeSyncLocalDataSource.findPending(accountId = accountId) } returns placeList
    coEvery { webSyncLocalDataSource.findPending(accountId = accountId) } returns webList
    coEvery { contactSyncLocalDataSource.findPending(accountId = accountId) } returns contactList
    coEvery { memoSyncLocalDataSource.findPending(accountId = accountId) } returns memoList
    coEvery { memoTagSyncLocalDataSource.findPending(accountId = accountId) } returns memoTagList
    coEvery { memoPlaceSyncLocalDataSource.findPending(accountId = accountId) } returns memoPlaceList
    coEvery { memoWebSyncLocalDataSource.findPending(accountId = accountId) } returns memoWebList
    coEvery { tagLinkSyncLocalDataSource.findPending(accountId = accountId) } returns tagLinkList
    coEvery { webTagSyncLocalDataSource.findPending(accountId = accountId) } returns webTagList
    coEvery { placeTagSyncLocalDataSource.findPending(accountId = accountId) } returns placeTagList
    coEvery { musicSyncLocalDataSource.findPending(accountId = accountId) } returns musicList
    coEvery { syncCursorLocalDataSource.find(accountId = any(), kind = any()) } returns 0L
}

internal fun tags(size: Int): List<TagLocalEntity> = List(size) { tag() }

internal fun tag(): TagLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<TagLocalEntity>()
        .setExp(TagLocalEntity::updatedAt, instant())
        .setExp(TagLocalEntity::createdAt, instant())
        .sample()

internal fun memos(size: Int): List<MemoLocalEntity> = List(size) { memo() }

internal fun memo(): MemoLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoLocalEntity>()
        .setExp(MemoLocalEntity::updatedAt, instant())
        .setExp(MemoLocalEntity::createdAt, instant())
        .sample()

private fun TestContext.stubTransactions() {
    coEvery { accountTagSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountPlaceSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountWebSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountContactSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountMemoSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountMemoTagSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountMemoPlaceSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountMemoWebSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountTagLinkSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountWebTagSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountPlaceTagSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountMusicSyncTransaction.clearPending(any(), any()) } returns Unit
    coEvery { accountTagSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountPlaceSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountWebSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountContactSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountMemoSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountMemoTagSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountMemoPlaceSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountMemoWebSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountTagLinkSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountWebTagSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountPlaceTagSyncTransaction.save(any(), any(), any()) } returns Unit
    coEvery { accountMusicSyncTransaction.save(any(), any(), any()) } returns Unit
}

private fun TestContext.stubRemoteDataSources() {
    coEvery { tagRemoteDataSource.push(any()) } returns Unit
    coEvery { placeRemoteDataSource.push(any()) } returns Unit
    coEvery { webRemoteDataSource.push(any()) } returns Unit
    coEvery { contactRemoteDataSource.push(any()) } returns Unit
    coEvery { memoRemoteDataSource.push(any()) } returns Unit
    coEvery { memoTagRemoteDataSource.push(any()) } returns Unit
    coEvery { memoPlaceRemoteDataSource.push(any()) } returns Unit
    coEvery { memoWebRemoteDataSource.push(any()) } returns Unit
    coEvery { tagLinkRemoteDataSource.push(any()) } returns Unit
    coEvery { webTagRemoteDataSource.push(any()) } returns Unit
    coEvery { placeTagRemoteDataSource.push(any()) } returns Unit
    coEvery { musicRemoteDataSource.push(any()) } returns Unit
    coEvery { tagRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { placeRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { webRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { contactRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { memoRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { memoTagRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { memoPlaceRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { memoWebRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { tagLinkRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { webTagRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { placeTagRemoteDataSource.pull(any()) } returns emptyList()
    coEvery { musicRemoteDataSource.pull(any()) } returns emptyList()
}

internal fun places(size: Int): List<PlaceLocalEntity> = List(size) { place() }

internal fun place(): PlaceLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<PlaceLocalEntity>()
        .setExp(PlaceLocalEntity::updatedAt, instant())
        .setExp(PlaceLocalEntity::createdAt, instant())
        .sample()

internal fun webs(size: Int): List<WebLocalEntity> = List(size) { web() }

internal fun web(): WebLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<WebLocalEntity>()
        .setExp(WebLocalEntity::updatedAt, instant())
        .setExp(WebLocalEntity::createdAt, instant())
        .sample()

internal fun contacts(size: Int): List<ContactLocalEntity> = List(size) { contact() }

internal fun contact(): ContactLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<ContactLocalEntity>()
        .setExp(ContactLocalEntity::updatedAt, instant())
        .setExp(ContactLocalEntity::createdAt, instant())
        .sample()

internal fun contactPulls(usnList: List<Long>): List<ContactPullRemoteEntity> =
    usnList.map { usn ->
        ContactPullRemoteEntity(contact = contact().toRemote(), usn = usn)
    }

internal fun memoPlaces(size: Int): List<MemoPlaceLocalEntity> = List(size) { memoPlace() }

internal fun memoPlace(): MemoPlaceLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoPlaceLocalEntity>()
        .setExp(MemoPlaceLocalEntity::updatedAt, instant())
        .setExp(MemoPlaceLocalEntity::createdAt, instant())
        .sample()

internal fun memoWebs(size: Int): List<MemoWebLocalEntity> = List(size) { memoWeb() }

internal fun memoWeb(isDeleted: Boolean = fixtureMonkey.giveMeOne<Boolean>()): MemoWebLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoWebLocalEntity>()
        .setExp(MemoWebLocalEntity::isDeleted, isDeleted)
        .setExp(MemoWebLocalEntity::updatedAt, instant())
        .setExp(MemoWebLocalEntity::createdAt, instant())
        .sample()

internal fun memoWebPulls(usnList: List<Long>): List<MemoWebPullRemoteEntity> =
    usnList.map { usn ->
        MemoWebPullRemoteEntity(memoWeb = memoWeb().toRemote(), usn = usn)
    }

internal fun memoTags(size: Int): List<MemoTagLocalEntity> = List(size) { memoTag() }

internal fun memoTag(): MemoTagLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MemoTagLocalEntity>()
        .setExp(MemoTagLocalEntity::updatedAt, instant())
        .setExp(MemoTagLocalEntity::createdAt, instant())
        .sample()

internal fun tagLinks(size: Int): List<TagLinkLocalEntity> = List(size) { tagLink() }

internal fun tagLink(): TagLinkLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<TagLinkLocalEntity>()
        .setExp(TagLinkLocalEntity::updatedAt, instant())
        .setExp(TagLinkLocalEntity::createdAt, instant())
        .sample()

internal fun tagLinkPulls(usnList: List<Long>): List<TagLinkPullRemoteEntity> =
    usnList.map { usn ->
        TagLinkPullRemoteEntity(tagLink = tagLink().toRemote(), usn = usn)
    }

internal fun tagPulls(usnList: List<Long>): List<TagPullRemoteEntity> =
    usnList.map { usn ->
        TagPullRemoteEntity(tag = tag().toRemote(), usn = usn)
    }

internal fun memoPulls(usnList: List<Long>): List<MemoPullRemoteEntity> =
    usnList.map { usn ->
        MemoPullRemoteEntity(memo = memo().toRemote(), usn = usn)
    }

internal fun memoTagPulls(usnList: List<Long>): List<MemoTagPullRemoteEntity> =
    usnList.map { usn ->
        MemoTagPullRemoteEntity(memoTag = memoTag().toRemote(), usn = usn)
    }

internal fun placePulls(usnList: List<Long>): List<PlacePullRemoteEntity> =
    usnList.map { usn ->
        PlacePullRemoteEntity(place = place().toRemote(), usn = usn)
    }

internal fun webPulls(usnList: List<Long>): List<WebPullRemoteEntity> =
    usnList.map { usn ->
        WebPullRemoteEntity(web = web().toRemote(), usn = usn)
    }

internal fun memoPlacePulls(usnList: List<Long>): List<MemoPlacePullRemoteEntity> =
    usnList.map { usn ->
        MemoPlacePullRemoteEntity(memoPlace = memoPlace().toRemote(), usn = usn)
    }

internal fun recordCrashlyticsLog(): List<CrashlyticsLog> {
    val reportList = mutableListOf<CrashlyticsLog>()
    val delegate = mockk<DiaryLoggerDelegate>()

    every { delegate.log(log = any()) } answers {
        val log = firstArg<DiaryLog>()
        if (log is CrashlyticsLog) {
            reportList += log
        }
    }

    DiaryLogger.add(delegate = delegate)

    return reportList
}

private fun instant(): Instant = Instant.fromEpochMilliseconds(fixtureMonkey.giveMeOne<Long>())

internal fun webTags(size: Int): List<WebTagLocalEntity> = List(size) { webTag() }

internal fun webTag(): WebTagLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<WebTagLocalEntity>()
        .setExp(WebTagLocalEntity::updatedAt, instant())
        .setExp(WebTagLocalEntity::createdAt, instant())
        .sample()

internal fun webTagPulls(usnList: List<Long>): List<WebTagPullRemoteEntity> =
    usnList.map { usn ->
        WebTagPullRemoteEntity(webTag = webTag().toRemote(), usn = usn)
    }

internal fun placeTags(size: Int): List<PlaceTagLocalEntity> = List(size) { placeTag() }

internal fun placeTag(): PlaceTagLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<PlaceTagLocalEntity>()
        .setExp(PlaceTagLocalEntity::updatedAt, instant())
        .setExp(PlaceTagLocalEntity::createdAt, instant())
        .sample()

internal fun placeTagPulls(usnList: List<Long>): List<PlaceTagPullRemoteEntity> =
    usnList.map { usn ->
        PlaceTagPullRemoteEntity(placeTag = placeTag().toRemote(), usn = usn)
    }

internal fun musics(size: Int): List<MusicLocalEntity> = List(size) { music() }

internal fun music(): MusicLocalEntity =
    fixtureMonkey
        .giveMeKotlinBuilder<MusicLocalEntity>()
        .setExp(MusicLocalEntity::updatedAt, instant())
        .setExp(MusicLocalEntity::createdAt, instant())
        .sample()

internal fun musicPulls(usnList: List<Long>): List<MusicPullRemoteEntity> =
    usnList.map { usn ->
        MusicPullRemoteEntity(music = music().toRemote(), usn = usn)
    }
