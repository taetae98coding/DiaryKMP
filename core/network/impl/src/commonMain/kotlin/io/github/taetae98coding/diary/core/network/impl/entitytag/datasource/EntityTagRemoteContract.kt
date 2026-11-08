package io.github.taetae98coding.diary.core.network.impl.entitytag.datasource

import io.github.taetae98coding.diary.core.network.api.entitytag.entity.EntityTagKindRemoteEntity

// 서버 함수와 JSON 키는 종류마다 따로 정해져 있어, 같은 형태의 요청을 종류별 이름으로 옮긴다.
internal data class EntityTagRemoteContract(
    val pushFunction: String,
    val pullFunction: String,
    val listKey: String,
    val itemKey: String,
    val entityIdKey: String,
)

internal val EntityTagKindRemoteEntity.contract: EntityTagRemoteContract
    get() =
        when (this) {
            EntityTagKindRemoteEntity.WEB -> contract(functionName = "web-tag", keyPrefix = "web")
            EntityTagKindRemoteEntity.PLACE -> contract(functionName = "place-tag", keyPrefix = "place")
            EntityTagKindRemoteEntity.CONTACT -> contract(functionName = "contact-tag", keyPrefix = "contact")
            EntityTagKindRemoteEntity.CHECKLIST -> contract(functionName = "checklist-tag", keyPrefix = "checklist")
            EntityTagKindRemoteEntity.MUSIC -> contract(functionName = "music-tag", keyPrefix = "music")
            EntityTagKindRemoteEntity.QR -> contract(functionName = "qr-tag", keyPrefix = "qr")
            EntityTagKindRemoteEntity.DDAY -> contract(functionName = "dday-tag", keyPrefix = "dday")
        }

private fun contract(
    functionName: String,
    keyPrefix: String,
): EntityTagRemoteContract =
    EntityTagRemoteContract(
        pushFunction = "v1-sync-push-$functionName",
        pullFunction = "v1-sync-pull-$functionName",
        listKey = "${keyPrefix}TagList",
        itemKey = "${keyPrefix}Tag",
        entityIdKey = "${keyPrefix}Id",
    )
