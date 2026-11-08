package io.github.taetae98coding.diary.work.spotlight

import io.github.taetae98coding.diary.core.database.api.memo.entity.MemoLocalEntity
import io.github.taetae98coding.diary.core.database.api.spotlight.datasource.SpotlightLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.core.testing.account.accountUser
import io.github.taetae98coding.diary.core.testing.checklist.checklistLocalEntity
import io.github.taetae98coding.diary.core.testing.contact.contactLocalEntity
import io.github.taetae98coding.diary.core.testing.dday.ddayLocalEntity
import io.github.taetae98coding.diary.core.testing.memo.memoLocalEntity
import io.github.taetae98coding.diary.core.testing.music.musicLocalEntity
import io.github.taetae98coding.diary.core.testing.place.placeLocalEntity
import io.github.taetae98coding.diary.core.testing.qr.qrLocalEntity
import io.github.taetae98coding.diary.core.testing.tag.tagLocalEntity
import io.github.taetae98coding.diary.core.testing.web.webLocalEntity
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.uuid.Uuid

class SpotlightWorkTest :
    FunSpec({
        test("TC-446731f1-5cf7-4659-b880-1715fbd0ac76 앱이 시작되면 담겨 있던 항목을 노출 대상과 맞춘다") {
            runTest {
                val account = Arb.accountUser().next()
                val deletedMemo = Arb.memoLocalEntity().next().toSpotlightItem()
                val otherAccountMemo = Arb.memoLocalEntity().next().toSpotlightItem()
                val indexedMemo = Arb.memoLocalEntity().next()
                val missingMemo = Arb.memoLocalEntity().next()
                val index = SpotlightIndexState(deletedMemo, otherAccountMemo, indexedMemo.toSpotlightItem())
                val dataSource = spotlightLocalDataSource(memoListOf = { accountId -> flowOf(if (accountId == account.id) listOf(indexedMemo, missingMemo) else emptyList()) })

                startWork(account = MutableStateFlow(Result.success(account)), dataSource = dataSource, index = index)

                index.identifierList() shouldContainExactlyInAnyOrder listOf(indexedMemo, missingMemo).map { it.toSpotlightItem().identifier }
            }
        }

        test("TC-75a4449a-4607-4404-bee9-cdfb3d735a29 현재 계정의 아홉 종류 항목을 담고 다른 계정의 항목은 담지 않는다") {
            runTest {
                val account = Arb.accountUser().next()
                val memo = Arb.memoLocalEntity().next()
                val tag = Arb.tagLocalEntity().next()
                val place = Arb.placeLocalEntity().next()
                val web = Arb.webLocalEntity().next()
                val contact = Arb.contactLocalEntity().next()
                val checklist = Arb.checklistLocalEntity().next()
                val qr = Arb.qrLocalEntity().next()
                val music = Arb.musicLocalEntity().next()
                val dday = Arb.ddayLocalEntity().next()
                val otherAccountMemo = Arb.memoLocalEntity().next()
                val dataSource =
                    spotlightLocalDataSource(
                        memoListOf = { accountId -> flowOf(if (accountId == account.id) listOf(memo) else listOf(otherAccountMemo)) },
                    )
                every { dataSource.getTagList(account.id) } returns flowOf(listOf(tag))
                every { dataSource.getPlaceList(account.id) } returns flowOf(listOf(place))
                every { dataSource.getWebList(account.id) } returns flowOf(listOf(web))
                every { dataSource.getContactList(account.id) } returns flowOf(listOf(contact))
                every { dataSource.getChecklistList(account.id) } returns flowOf(listOf(checklist))
                every { dataSource.getQrList(account.id) } returns flowOf(listOf(qr))
                every { dataSource.getMusicList(account.id) } returns flowOf(listOf(music))
                every { dataSource.getDDayList(account.id) } returns flowOf(listOf(dday))
                val index = SpotlightIndexState()

                startWork(account = MutableStateFlow(Result.success(account)), dataSource = dataSource, index = index)

                index.itemList() shouldContainExactlyInAnyOrder
                    listOf(
                        memo.toSpotlightItem(),
                        tag.toSpotlightItem(),
                        place.toSpotlightItem(),
                        web.toSpotlightItem(),
                        contact.toSpotlightItem(),
                        checklist.toSpotlightItem(),
                        qr.toSpotlightItem(),
                        music.toSpotlightItem(),
                        dday.toSpotlightItem(),
                    )
            }
        }

        test("TC-ffa2902e-19f4-4595-a5ba-f0d96e826b91 현재 계정을 확인할 수 없으면 어떤 항목도 담지 않는다") {
            runTest {
                val index = SpotlightIndexState(Arb.memoLocalEntity().next().toSpotlightItem(), Arb.tagLocalEntity().next().toSpotlightItem())
                val dataSource = spotlightLocalDataSource(memoListOf = { flowOf(listOf(Arb.memoLocalEntity().next())) })

                startWork(account = MutableStateFlow(Result.failure(IllegalStateException())), dataSource = dataSource, index = index)

                index.items.shouldBeEmpty()
            }
        }

        test("TC-585b6caf-41ba-4f5b-9ff0-eec55d5a3beb 기기에 저장된 메모가 바뀌면 Spotlight에 반영한다") {
            runTest {
                val account = Arb.accountUser().next()
                val memoList = MutableStateFlow(emptyList<MemoLocalEntity>())
                val dataSource = spotlightLocalDataSource(memoListOf = { accountId -> if (accountId == account.id) memoList else flowOf(emptyList()) })
                val index = SpotlightIndexState()
                val memo = Arb.memoLocalEntity().next().copy(isFinished = false, isDeleted = false)

                startWork(account = MutableStateFlow(Result.success(account)), dataSource = dataSource, index = index)

                // 메모 추가
                memoList.value = listOf(memo)
                runCurrent()
                index.itemList() shouldBe listOf(memo.toSpotlightItem())

                // 메모 제목 수정
                val renamed = memo.copy(detail = memo.detail.copy(title = Arb.nonBlankString().next()))
                memoList.value = listOf(renamed)
                runCurrent()
                index.itemList() shouldBe listOf(renamed.toSpotlightItem())

                // 메모 완료: 저장소는 완료된 메모도 그대로 돌려준다.
                memoList.value = listOf(renamed.copy(isFinished = true))
                runCurrent()
                index.itemList() shouldBe listOf(renamed.toSpotlightItem())

                // 메모 삭제: 저장소가 삭제된 메모를 돌려주지 않는다.
                memoList.value = emptyList()
                runCurrent()
                index.items.shouldBeEmpty()

                // 삭제한 메모 되돌리기
                memoList.value = listOf(renamed)
                runCurrent()
                index.itemList() shouldBe listOf(renamed.toSpotlightItem())

                // 동기화로 내려받은 메모 추가
                val pulled = Arb.memoLocalEntity().next()
                memoList.value = listOf(renamed, pulled)
                runCurrent()
                index.itemList() shouldContainExactlyInAnyOrder listOf(renamed.toSpotlightItem(), pulled.toSpotlightItem())
            }
        }

        test("TC-29e44189-cc2f-43d5-9a38-b738e75f615d 계정이 바뀌면 이전 계정의 항목을 빼고 바뀐 계정의 항목을 담는다") {
            listOf(
                Account.Guest to Arb.accountUser().next(),
                Arb.accountUser().next() to Account.Guest,
            ).forEach { (before, after) ->
                runTest {
                    val beforeMemo = Arb.memoLocalEntity().next()
                    val afterMemo = Arb.memoLocalEntity().next()
                    val account = MutableStateFlow(Result.success(before))
                    val dataSource =
                        spotlightLocalDataSource(
                            memoListOf = { accountId ->
                                when (accountId) {
                                    before.id -> flowOf(listOf(beforeMemo))
                                    after.id -> flowOf(listOf(afterMemo))
                                    else -> flowOf(emptyList())
                                }
                            },
                        )
                    val index = SpotlightIndexState()

                    startWork(account = account, dataSource = dataSource, index = index)
                    index.itemList() shouldBe listOf(beforeMemo.toSpotlightItem())

                    account.value = Result.success(after)
                    runCurrent()

                    index.itemList() shouldBe listOf(afterMemo.toSpotlightItem())
                }
            }
        }

        test("색인 반영이 실패하면 다음 변경에서 색인을 전부 다시 맞춘다") {
            runTest {
                val account = Arb.accountUser().next()
                val memoList = MutableStateFlow(listOf(Arb.memoLocalEntity().next()))
                val dataSource = spotlightLocalDataSource(memoListOf = { memoList })
                val index = SpotlightIndexState()
                index.failNextUpsert = true

                startWork(account = MutableStateFlow(Result.success(account)), dataSource = dataSource, index = index)
                index.items.shouldBeEmpty()

                val next = memoList.value + Arb.memoLocalEntity().next()
                memoList.value = next
                runCurrent()

                coVerify(exactly = 2) { index.mock.deleteAll() }
                index.itemList() shouldContainExactlyInAnyOrder next.map { it.toSpotlightItem() }
            }
        }

        test("바뀌지 않은 항목은 다시 담지 않는다") {
            runTest {
                val account = Arb.accountUser().next()
                val unchanged = Arb.memoLocalEntity().next()
                val memoList = MutableStateFlow(listOf(unchanged))
                val dataSource = spotlightLocalDataSource(memoListOf = { memoList })
                val index = SpotlightIndexState()

                startWork(account = MutableStateFlow(Result.success(account)), dataSource = dataSource, index = index)

                val added = Arb.memoLocalEntity().next()
                memoList.value = listOf(unchanged, added)
                runCurrent()

                coVerify(exactly = 1) { index.mock.upsert(listOf(added.toSpotlightItem())) }
            }
        }
    })

// Spotlight 색인의 내용을 관찰하려고 mock의 응답이 담긴 항목을 기록한다.
private class SpotlightIndexState(
    vararg initialItem: SpotlightItem,
) {
    val items: MutableMap<String, SpotlightItem> = initialItem.associateBy(SpotlightItem::identifier).toMutableMap()
    var failNextUpsert: Boolean = false

    val mock: SpotlightIndex =
        mockk {
            coEvery { deleteAll() } answers { items.clear() }
            coEvery { delete(any()) } answers { firstArg<List<String>>().forEach(items::remove) }
            coEvery { upsert(any()) } answers {
                if (failNextUpsert) {
                    failNextUpsert = false
                    throw IllegalStateException()
                }
                firstArg<List<SpotlightItem>>().forEach { item -> items[item.identifier] = item }
            }
        }

    fun itemList(): List<SpotlightItem> = items.values.toList()

    fun identifierList(): List<String> = items.keys.toList()
}

private fun spotlightLocalDataSource(memoListOf: (Uuid) -> Flow<List<MemoLocalEntity>>): SpotlightLocalDataSource =
    mockk {
        every { getMemoList(any()) } answers { memoListOf(firstArg()) }
        every { getTagList(any()) } returns flowOf(emptyList())
        every { getPlaceList(any()) } returns flowOf(emptyList())
        every { getWebList(any()) } returns flowOf(emptyList())
        every { getContactList(any()) } returns flowOf(emptyList())
        every { getChecklistList(any()) } returns flowOf(emptyList())
        every { getQrList(any()) } returns flowOf(emptyList())
        every { getMusicList(any()) } returns flowOf(emptyList())
        every { getDDayList(any()) } returns flowOf(emptyList())
    }

private fun TestScope.startWork(
    account: Flow<Result<Account>>,
    dataSource: SpotlightLocalDataSource,
    index: SpotlightIndexState,
) {
    val getAccountUseCase = mockk<GetAccountUseCase>()
    every { getAccountUseCase(parameter = Unit) } returns account

    backgroundScope.launch {
        SpotlightWork(
            getAccountUseCase = getAccountUseCase,
            spotlightLocalDataSource = dataSource,
            spotlightIndex = index.mock,
        ).doWork()
    }
    runCurrent()
}
