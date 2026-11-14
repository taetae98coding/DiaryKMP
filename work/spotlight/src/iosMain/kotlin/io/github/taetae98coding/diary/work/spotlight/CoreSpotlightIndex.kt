@file:OptIn(ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.work.spotlight

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.CoreSpotlight.CSSearchableIndex
import platform.CoreSpotlight.CSSearchableItem
import platform.CoreSpotlight.CSSearchableItemAttributeSet
import platform.CoreSpotlight.contentDescription
import platform.CoreSpotlight.thumbnailData
import platform.CoreSpotlight.title
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.distantFuture
import platform.UniformTypeIdentifiers.UTTypeText
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class CoreSpotlightIndex(
    private val searchableIndex: CSSearchableIndex,
    private val iconRenderer: SpotlightIconRenderer,
    private val dispatcher: CoroutineDispatcher,
) : SpotlightIndex {
    override suspend fun deleteAll() {
        await { completion -> searchableIndex.deleteAllSearchableItemsWithCompletionHandler(completion) }
    }

    override suspend fun upsert(itemList: List<SpotlightItem>) {
        // 항목마다 아이콘을 그려 PNG로 바꾸는 동안 호출한 스레드가 막히므로 메인 스레드 밖에서 만든다.
        val searchableItemList =
            withContext(dispatcher) {
                itemList.map { item ->
                    ensureActive()
                    item.toSearchableItem()
                }
            }

        await { completion -> searchableIndex.indexSearchableItems(items = searchableItemList, completionHandler = completion) }
    }

    override suspend fun delete(identifierList: List<String>) {
        await { completion -> searchableIndex.deleteSearchableItemsWithIdentifiers(identifiers = identifierList, completionHandler = completion) }
    }

    private suspend fun await(block: (completion: (NSError?) -> Unit) -> Unit) {
        suspendCancellableCoroutine { continuation ->
            block { error ->
                if (!continuation.isActive) return@block

                if (error == null) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(SpotlightIndexException(message = error.localizedDescription))
                }
            }
        }
    }

    private fun SpotlightItem.toSearchableItem(): CSSearchableItem {
        val attributeSet =
            CSSearchableItemAttributeSet(contentType = UTTypeText).also { attributeSet ->
                attributeSet.title = title
                attributeSet.contentDescription = description.ifEmpty { null }
                attributeSet.thumbnailData = iconRenderer.render(icon = icon, color = iconColor)
            }

        // 만료일을 정하지 않으면 시스템이 한 달 뒤 항목을 지우므로, 삭제될 때까지 남도록 만료일을 먼 미래로 둔다.
        return CSSearchableItem(uniqueIdentifier = identifier, domainIdentifier = null, attributeSet = attributeSet).also { item ->
            item.expirationDate = NSDate.distantFuture
        }
    }
}

internal class SpotlightIndexException(
    message: String,
) : RuntimeException(message)
