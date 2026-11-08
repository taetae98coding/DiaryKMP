@file:OptIn(ExperimentalCoroutinesApi::class)

package io.github.taetae98coding.diary.work.spotlight

import io.github.taetae98coding.diary.core.database.api.spotlight.datasource.SpotlightLocalDataSource
import io.github.taetae98coding.diary.core.model.account.Account
import io.github.taetae98coding.diary.domain.account.usecase.GetAccountUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

// 현재 계정의 노출 대상을 지켜보며 Spotlight 색인을 맞춘다. 끝나지 않으므로 앱 수명의 scope에서 실행한다.
internal class SpotlightWork(
    private val getAccountUseCase: GetAccountUseCase,
    private val spotlightLocalDataSource: SpotlightLocalDataSource,
    private val spotlightIndex: SpotlightIndex,
) {
    suspend fun doWork() {
        // 색인에 반영한 항목이다. null이면 색인에 무엇이 있는지 알 수 없어(앱 시작 직후나 반영 실패 뒤) 전부 지우고 다시 넣는다.
        var indexedItemMap: Map<String, SpotlightItem>? = null

        getItemList()
            .distinctUntilChanged()
            .collect { itemList ->
                val itemMap = itemList.associateBy(SpotlightItem::identifier)

                indexedItemMap =
                    try {
                        submit(indexedItemMap = indexedItemMap, itemMap = itemMap)
                        itemMap
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (_: Throwable) {
                        null
                    }
            }
    }

    private fun getItemList(): Flow<List<SpotlightItem>> =
        getAccountUseCase(parameter = Unit).flatMapLatest { result ->
            result.fold(
                onSuccess = ::getItemList,
                // 현재 계정을 확인할 수 없으면 어떤 항목도 Spotlight에 나타나지 않아야 하므로 빈 목록으로 바꿔 색인을 비운다.
                onFailure = { flowOf(emptyList()) },
            )
        }

    private fun getItemList(account: Account): Flow<List<SpotlightItem>> {
        val accountId = account.id

        return combine(
            spotlightLocalDataSource.getMemoList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getTagList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getPlaceList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getWebList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getContactList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getChecklistList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getQrList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
            spotlightLocalDataSource.getMusicList(accountId = accountId).map { list -> list.map { it.toSpotlightItem() } },
        ) { itemListArray -> itemListArray.flatMap { it } }
    }

    private suspend fun submit(
        indexedItemMap: Map<String, SpotlightItem>?,
        itemMap: Map<String, SpotlightItem>,
    ) {
        if (indexedItemMap == null) {
            spotlightIndex.deleteAll()
            if (itemMap.isNotEmpty()) spotlightIndex.upsert(itemList = itemMap.values.toList())

            return
        }

        val deletedIdentifierList = (indexedItemMap.keys - itemMap.keys).toList()
        val changedItemList = itemMap.values.filter { item -> indexedItemMap[item.identifier] != item }

        if (deletedIdentifierList.isNotEmpty()) spotlightIndex.delete(identifierList = deletedIdentifierList)
        if (changedItemList.isNotEmpty()) spotlightIndex.upsert(itemList = changedItemList)
    }
}
