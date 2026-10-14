package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiNetworkSuggestion
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.domain.qr.content.QrEventSchedule
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus

@Composable
internal actual fun rememberQrActionLauncher(): QrActionLauncher {
    val context = LocalContext.current

    return remember(context) { AndroidQrActionLauncher(context = context) }
}

internal class AndroidQrActionLauncher(
    private val context: Context,
) : QrActionLauncher {
    // WifiNetworkSuggestion은 WEP 네트워크를 만들 수 없다.
    override fun isSupported(action: QrAction): Boolean = !(action is QrAction.JoinWifi && action.security == QrWifiSecurity.WEP)

    // 받을 앱이 없는 인텐트는 ActivityNotFoundException으로, ASCII가 아닌 Wi-Fi 비밀번호는 IllegalArgumentException으로 알려진다.
    override suspend fun launch(action: QrAction): Boolean =
        try {
            context.startActivity(action.toIntent())
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        } catch (_: SecurityException) {
            false
        }
}

private fun QrAction.toIntent(): Intent =
    when (this) {
        is QrAction.OpenUrl -> Intent(Intent.ACTION_VIEW, Uri.parse(url))
        is QrAction.Call -> Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
        is QrAction.SendEmail -> toIntent()
        is QrAction.SendSms -> Intent(Intent.ACTION_SENDTO, Uri.fromParts("smsto", phoneNumber, null)).putExtraIfNotEmpty(SMS_BODY_EXTRA, message)
        is QrAction.ShowLocation -> Intent(Intent.ACTION_VIEW, Uri.parse(geoUri()))
        is QrAction.AddContact -> toIntent()
        is QrAction.AddEvent -> toIntent()
        is QrAction.JoinWifi -> toIntent()
    }

private fun QrAction.SendEmail.toIntent(): Intent =
    Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
        if (to.isNotEmpty()) putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
        putExtraIfNotEmpty(Intent.EXTRA_SUBJECT, subject)
        putExtraIfNotEmpty(Intent.EXTRA_TEXT, body)
    }

// 지도 앱이 좌표에 지점을 표시하도록 같은 좌표를 검색어로도 넘긴다.
private fun QrAction.ShowLocation.geoUri(): String {
    val center = "${coordinate.latitudeText},${coordinate.longitudeText}"

    return "geo:$center?q=$center"
}

private fun QrAction.AddContact.toIntent(): Intent =
    Intent(ContactsContract.Intents.Insert.ACTION).apply {
        type = ContactsContract.RawContacts.CONTENT_TYPE
        putExtraIfNotEmpty(ContactsContract.Intents.Insert.NAME, name)
        putExtraIfNotEmpty(ContactsContract.Intents.Insert.PHONE, phoneNumber)
        putExtraIfNotEmpty(ContactsContract.Intents.Insert.EMAIL, email)
        putExtraIfNotEmpty(ContactsContract.Intents.Insert.COMPANY, company)
        putExtraIfNotEmpty(ContactsContract.Intents.Insert.POSTAL, address)
        if (website.isNotEmpty()) {
            val websiteRow =
                ContentValues().apply {
                    put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    put(ContactsContract.CommonDataKinds.Website.URL, website)
                }
            putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, arrayListOf(websiteRow))
        }
    }

private fun QrAction.AddEvent.toIntent(): Intent =
    Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI).apply {
        putExtraIfNotEmpty(CalendarContract.Events.TITLE, title)
        putExtraIfNotEmpty(CalendarContract.Events.EVENT_LOCATION, location)
        putExtraIfNotEmpty(CalendarContract.Events.DESCRIPTION, description)
        when (val schedule = schedule) {
            is QrEventSchedule.AllDay -> {
                putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, schedule.start.toAllDayMillis())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, schedule.endInclusive.plus(1, DateTimeUnit.DAY).toAllDayMillis())
            }

            is QrEventSchedule.DateTime -> {
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, schedule.start.toEpochMilliseconds())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, schedule.end.toEpochMilliseconds())
            }

            null -> Unit
        }
    }

// 캘린더 제공자는 종일 일정의 시각을 UTC 자정으로 저장한다.
private fun LocalDate.toAllDayMillis(): Long = atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

private fun QrAction.JoinWifi.toIntent(): Intent {
    val suggestion =
        WifiNetworkSuggestion
            .Builder()
            .setSsid(ssid)
            .apply {
                if (security == QrWifiSecurity.WPA) setWpa2Passphrase(password)
                if (isHidden) setIsHiddenSsid(true)
            }.build()

    return Intent(Settings.ACTION_WIFI_ADD_NETWORKS).putParcelableArrayListExtra(Settings.EXTRA_WIFI_NETWORK_LIST, arrayListOf(suggestion))
}

private fun Intent.putExtraIfNotEmpty(
    name: String,
    value: String,
): Intent = if (value.isEmpty()) this else putExtra(name, value)

private const val SMS_BODY_EXTRA = "sms_body"
