package io.github.taetae98coding.diary.domain.integrity.usecase

import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdict
import io.github.taetae98coding.diary.core.model.integrity.PlayIntegrityVerdictValue
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private const val LIST_SEPARATOR = ","
private const val REQUEST_DETAILS = "requestDetails"
private const val APP_INTEGRITY = "appIntegrity"
private const val DEVICE_INTEGRITY = "deviceIntegrity"

private val fieldNameMap: Map<List<String>, String> =
    mapOf(
        listOf(REQUEST_DETAILS, "requestPackageName") to "request_package_name",
        listOf(REQUEST_DETAILS, "requestHash") to "request_hash",
        listOf(REQUEST_DETAILS, "timestampMillis") to "timestamp_millis",
        listOf(APP_INTEGRITY, "appRecognitionVerdict") to "app_recognition_verdict",
        listOf(APP_INTEGRITY, "packageName") to "package_name",
        listOf(APP_INTEGRITY, "certificateSha256Digest") to "certificate_sha256_digest",
        listOf(APP_INTEGRITY, "versionCode") to "version_code",
        listOf(DEVICE_INTEGRITY, "deviceRecognitionVerdict") to "device_recognition_verdict",
        listOf(DEVICE_INTEGRITY, "deviceAttributes", "sdkVersion") to "sdk_version",
        listOf(DEVICE_INTEGRITY, "recentDeviceActivity", "deviceActivityLevel") to "device_activity_level",
        listOf("accountDetails", "appLicensingVerdict") to "app_licensing_verdict",
        listOf("environmentDetails", "appAccessRiskVerdict", "appsDetected") to "apps_detected",
        listOf("environmentDetails", "playProtectVerdict") to "play_protect_verdict",
    )

internal fun PlayIntegrityVerdict.toFlatVerdict(): JsonObject =
    JsonObject(
        fieldNameMap
            .mapNotNull { (path, name) -> findValue(path)?.toPrimitive()?.let { value -> name to value } }
            .toMap(),
    )

private fun PlayIntegrityVerdict.findValue(path: List<String>): PlayIntegrityVerdictValue? = path.drop(1).fold(fieldMap[path.first()]) { value, key -> (value as? PlayIntegrityVerdictValue.Group)?.fieldMap?.get(key) }

private fun PlayIntegrityVerdictValue.toPrimitive(): JsonPrimitive? =
    when (this) {
        is PlayIntegrityVerdictValue.Group, is PlayIntegrityVerdictValue.Null -> null

        is PlayIntegrityVerdictValue.ValueList -> {
            val contentList = valueList.mapNotNull { value -> value.toPrimitive()?.content }
            if (contentList.isEmpty()) null else JsonPrimitive(contentList.joinToString(separator = LIST_SEPARATOR))
        }

        is PlayIntegrityVerdictValue.Text -> JsonPrimitive(value)

        is PlayIntegrityVerdictValue.Number -> JsonPrimitive(value)

        is PlayIntegrityVerdictValue.Flag -> JsonPrimitive(value.toString())
    }
