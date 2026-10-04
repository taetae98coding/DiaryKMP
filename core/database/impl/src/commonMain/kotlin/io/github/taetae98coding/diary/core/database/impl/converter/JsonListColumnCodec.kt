package io.github.taetae98coding.diary.core.database.impl.converter

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

// Room은 제네릭 변환 함수를 처리하지 못하므로 변환기는 타입별 클래스로 두고 JSON 처리만 여기서 공유한다.
internal class JsonListColumnCodec<T>(
    elementSerializer: KSerializer<T>,
) {
    private val listSerializer: KSerializer<List<T>> = ListSerializer(elementSerializer)

    fun encode(list: List<T>): String = Json.encodeToString(listSerializer, list)

    fun decode(value: String): List<T> = Json.decodeFromString(listSerializer, value)
}
