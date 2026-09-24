package io.github.taetae98coding.diary.core.datastore.impl

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.okio.OkioSerializer
import io.github.taetae98coding.diary.core.datastore.api.setting.entity.GeminiSettingLocalEntity
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource

internal class SettingSerializer<T>(
    private val serializer: KSerializer<T>,
    override val defaultValue: T,
) : OkioSerializer<T> {
    override suspend fun readFrom(source: BufferedSource): T =
        try {
            json.decodeFromString(serializer, source.readUtf8())
        } catch (exception: SerializationException) {
            throw CorruptionException("Unable to read ${serializer.descriptor.serialName}.", exception)
        }

    override suspend fun writeTo(
        t: T,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(json.encodeToString(serializer, t))
    }

    private companion object {
        private val json = Json { ignoreUnknownKeys = true }
    }
}

internal val MapSettingSerializer: SettingSerializer<MapSettingData> =
    SettingSerializer(
        serializer = MapSettingData.serializer(),
        defaultValue = MapSettingData(),
    )

internal val BrowserSettingSerializer: SettingSerializer<BrowserSettingData> =
    SettingSerializer(
        serializer = BrowserSettingData.serializer(),
        defaultValue = BrowserSettingData(),
    )

internal val HolidaySettingSerializer: SettingSerializer<HolidaySettingData> =
    SettingSerializer(
        serializer = HolidaySettingData.serializer(),
        defaultValue = HolidaySettingData(),
    )

internal val SyncTimeSerializer: SettingSerializer<SyncTimeData> =
    SettingSerializer(
        serializer = SyncTimeData.serializer(),
        defaultValue = SyncTimeData(),
    )

internal val GeminiSettingSerializer: SettingSerializer<GeminiSettingLocalEntity> =
    SettingSerializer(
        serializer = GeminiSettingLocalEntity.serializer(),
        defaultValue = GeminiSettingLocalEntity(),
    )

internal val MusicDownloadProxySettingSerializer: SettingSerializer<MusicDownloadProxySettingData> =
    SettingSerializer(
        serializer = MusicDownloadProxySettingData.serializer(),
        defaultValue = MusicDownloadProxySettingData(),
    )
