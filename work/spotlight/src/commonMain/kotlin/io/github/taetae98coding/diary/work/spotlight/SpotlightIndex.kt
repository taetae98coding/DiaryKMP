package io.github.taetae98coding.diary.work.spotlight

internal interface SpotlightIndex {
    suspend fun deleteAll()

    suspend fun upsert(itemList: List<SpotlightItem>)

    suspend fun delete(identifierList: List<String>)
}
