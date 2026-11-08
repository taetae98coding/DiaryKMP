package io.github.taetae98coding.diary.core.network.api.dday.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

@Serializable(with = DDayRepeatRemoteEntitySerializer::class)
public sealed interface DDayRepeatRemoteEntity {
    public data class Days(
        val days: Int,
    ) : DDayRepeatRemoteEntity

    public data class Months(
        val months: Int,
    ) : DDayRepeatRemoteEntity
}

// 서버는 간격마다 days나 months 키 하나만 가진 객체를 받으므로, 종류를 구분하는 키를 붙이지 않고 그 키 하나로 직렬화한다.
internal object DDayRepeatRemoteEntitySerializer : KSerializer<DDayRepeatRemoteEntity> {
    private const val DAYS_KEY: String = "days"
    private const val MONTHS_KEY: String = "months"

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("io.github.taetae98coding.diary.core.network.api.dday.entity.DDayRepeatRemoteEntity") {
            element<Int>(DAYS_KEY, isOptional = true)
            element<Int>(MONTHS_KEY, isOptional = true)
        }

    override fun serialize(
        encoder: Encoder,
        value: DDayRepeatRemoteEntity,
    ) {
        val jsonEncoder = encoder as? JsonEncoder ?: throw SerializationException("DDayRepeatRemoteEntity supports JSON only.")

        jsonEncoder.encodeJsonElement(
            buildJsonObject {
                when (value) {
                    is DDayRepeatRemoteEntity.Days -> put(DAYS_KEY, value.days)
                    is DDayRepeatRemoteEntity.Months -> put(MONTHS_KEY, value.months)
                }
            },
        )
    }

    override fun deserialize(decoder: Decoder): DDayRepeatRemoteEntity {
        val jsonDecoder = decoder as? JsonDecoder ?: throw SerializationException("DDayRepeatRemoteEntity supports JSON only.")
        val json = jsonDecoder.decodeJsonElement().jsonObject
        val days = json[DAYS_KEY]

        return if (days != null) {
            DDayRepeatRemoteEntity.Days(days = days.jsonPrimitive.int)
        } else {
            val months = json[MONTHS_KEY] ?: throw SerializationException("DDayRepeatRemoteEntity requires days or months.")

            DDayRepeatRemoteEntity.Months(months = months.jsonPrimitive.int)
        }
    }
}
