package io.github.taetae98coding.diary.core.network.impl.entitytag.datasource

import io.github.taetae98coding.diary.core.network.api.entitytag.datasource.EntityTagRemoteDataSource
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagPullRemoteEntity
import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagRemoteEntity
import io.github.taetae98coding.diary.core.network.impl.sync.entity.PullRequestRemoteEntity
import io.github.taetae98coding.diary.core.supabase.api.SupabaseFunction
import io.github.taetae98coding.diary.core.supabase.api.invoke
import io.ktor.client.call.body
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.koin.core.annotation.Factory

@Factory
internal class EntityTagRemoteDataSourceImpl(
    private val supabaseFunction: SupabaseFunction,
) : EntityTagRemoteDataSource {
    override suspend fun push(
        kind: EntityTagKindRemoteEntity,
        entityTagList: List<EntityTagRemoteEntity>,
    ) {
        val contract = kind.contract

        supabaseFunction(
            function = contract.pushFunction,
            body =
                buildJsonObject {
                    put(
                        contract.listKey,
                        buildJsonArray {
                            entityTagList.forEach { entityTag -> add(entityTag.toJson(contract = contract)) }
                        },
                    )
                },
        )
    }

    override suspend fun pull(
        kind: EntityTagKindRemoteEntity,
        usn: Long,
    ): List<EntityTagPullRemoteEntity> {
        val contract = kind.contract
        val response =
            supabaseFunction(
                function = contract.pullFunction,
                body = PullRequestRemoteEntity(usn = usn),
            ).body<JsonObject>()

        return response.getValue(contract.listKey).jsonArray.map { element ->
            val pull = element.jsonObject

            EntityTagPullRemoteEntity(
                entityTag = pull.getValue(contract.itemKey).jsonObject.toEntityTag(contract = contract),
                usn = pull.getValue(USN_KEY).jsonPrimitive.long,
            )
        }
    }

    private fun EntityTagRemoteEntity.toJson(contract: EntityTagRemoteContract): JsonObject =
        buildJsonObject {
            put(contract.entityIdKey, Json.encodeToJsonElement(entityId))
            put(TAG_ID_KEY, Json.encodeToJsonElement(tagId))
            put(IS_DELETED_KEY, Json.encodeToJsonElement(isDeleted))
            put(UPDATED_AT_KEY, Json.encodeToJsonElement(updatedAt))
            put(CREATED_AT_KEY, Json.encodeToJsonElement(createdAt))
        }

    private fun JsonObject.toEntityTag(contract: EntityTagRemoteContract): EntityTagRemoteEntity =
        EntityTagRemoteEntity(
            entityId = Json.decodeFromJsonElement(getValue(contract.entityIdKey)),
            tagId = Json.decodeFromJsonElement(getValue(TAG_ID_KEY)),
            isDeleted = Json.decodeFromJsonElement(getValue(IS_DELETED_KEY)),
            updatedAt = Json.decodeFromJsonElement(getValue(UPDATED_AT_KEY)),
            createdAt = Json.decodeFromJsonElement(getValue(CREATED_AT_KEY)),
        )

    private companion object {
        const val TAG_ID_KEY: String = "tagId"
        const val IS_DELETED_KEY: String = "isDeleted"
        const val UPDATED_AT_KEY: String = "updatedAt"
        const val CREATED_AT_KEY: String = "createdAt"
        const val USN_KEY: String = "usn"
    }
}
