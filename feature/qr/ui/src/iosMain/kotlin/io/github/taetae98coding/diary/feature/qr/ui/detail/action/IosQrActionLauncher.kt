@file:OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)

package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import io.github.taetae98coding.diary.core.model.location.Coordinate
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import io.github.taetae98coding.diary.domain.qr.content.QrEventSchedule
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import platform.Contacts.CNContact
import platform.Contacts.CNContactVCardSerialization
import platform.ContactsUI.CNContactViewController
import platform.ContactsUI.CNContactViewControllerDelegateProtocol
import platform.EventKit.EKEvent
import platform.EventKit.EKEventStore
import platform.EventKitUI.EKEventEditViewAction
import platform.EventKitUI.EKEventEditViewController
import platform.EventKitUI.EKEventEditViewDelegateProtocol
import platform.Foundation.NSDate
import platform.Foundation.NSString
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.NetworkExtension.NEHotspotConfiguration
import platform.NetworkExtension.NEHotspotConfigurationErrorAlreadyAssociated
import platform.NetworkExtension.NEHotspotConfigurationErrorUserDenied
import platform.NetworkExtension.NEHotspotConfigurationManager
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.time.Instant

private const val APPLE_MAP_URI = "https://maps.apple.com/"

@Composable
internal actual fun rememberQrActionLauncher(): QrActionLauncher {
    val viewController = LocalUIViewController.current

    return remember(viewController) { IosQrActionLauncher(viewController = viewController) }
}

internal class IosQrActionLauncher(
    private val viewController: UIViewController,
) : QrActionLauncher {
    // 시스템 화면의 delegate는 약한 참조라 화면이 떠 있는 동안 여기서 붙잡아 둔다.
    private val contactDelegate = ContactViewDelegate()
    private val eventDelegate = EventEditViewDelegate()

    override fun isSupported(action: QrAction): Boolean = true

    override suspend fun launch(action: QrAction): Boolean =
        when (action) {
            is QrAction.OpenUrl -> open(uri = action.url)

            is QrAction.Call -> open(uri = action.telUri())

            is QrAction.SendEmail -> open(uri = action.mailtoUri())

            // iOS 메시지 앱은 번호 뒤의 본문을 '&'로 구분해 읽는다.
            is QrAction.SendSms -> open(uri = action.smsUri(bodySeparator = '&'))

            is QrAction.ShowLocation -> open(uri = action.coordinate.appleMapUri())

            is QrAction.AddContact -> presentContact(vCard = action.vCard)

            is QrAction.AddEvent -> presentEvent(action = action)

            is QrAction.JoinWifi -> joinWifi(action = action)
        }

    private fun presentContact(vCard: String): Boolean {
        val contact = vCard.toContactOrNull() ?: return false
        val contactViewController = CNContactViewController.viewControllerForNewContact(contact)
        contactViewController.delegate = contactDelegate

        viewController.presentViewController(UINavigationController(rootViewController = contactViewController), animated = true, completion = null)
        return true
    }

    private fun presentEvent(action: QrAction.AddEvent): Boolean {
        val eventStore = EKEventStore()
        val event =
            EKEvent.eventWithEventStore(eventStore).apply {
                title = action.title
                location = action.location.ifEmpty { null }
                notes = action.description.ifEmpty { null }
                applySchedule(schedule = action.schedule)
            }
        val eventEditViewController =
            EKEventEditViewController().apply {
                this.eventStore = eventStore
                this.event = event
                editViewDelegate = eventDelegate
            }

        viewController.presentViewController(eventEditViewController, animated = true, completion = null)
        return true
    }
}

private fun String.toContactOrNull(): CNContact? {
    val data = NSString.create(string = this).dataUsingEncoding(NSUTF8StringEncoding) ?: return null

    return CNContactVCardSerialization.contactsWithData(data = data, error = null)?.firstOrNull() as? CNContact
}

// NEHotspotConfigurationManager는 사용자의 거절과 이미 연결된 상태도 오류로 돌려준다.
private suspend fun joinWifi(action: QrAction.JoinWifi): Boolean {
    val configuration =
        when (action.security) {
            QrWifiSecurity.NONE -> NEHotspotConfiguration(sSID = action.ssid)
            QrWifiSecurity.WPA -> NEHotspotConfiguration(sSID = action.ssid, passphrase = action.password, isWEP = false)
            QrWifiSecurity.WEP -> NEHotspotConfiguration(sSID = action.ssid, passphrase = action.password, isWEP = true)
        }.apply { hidden = action.isHidden }

    return suspendCancellableCoroutine { continuation ->
        NEHotspotConfigurationManager.sharedManager.applyConfiguration(configuration) { error ->
            continuation.resume(error == null || error.code in handedOffWifiErrorCodeSet)
        }
    }
}

private val handedOffWifiErrorCodeSet: Set<Long> = setOf(NEHotspotConfigurationErrorUserDenied, NEHotspotConfigurationErrorAlreadyAssociated)

// iOS는 주소를 열지 못한 것을 예외가 아니라 완료 결과로 알려 준다.
private suspend fun open(uri: String): Boolean {
    val url = NSURL.URLWithString(uri) ?: return false

    return suspendCancellableCoroutine { continuation ->
        UIApplication.sharedApplication.openURL(
            url = url,
            options = emptyMap<Any?, Any>(),
            completionHandler = { isOpened -> continuation.resume(isOpened) },
        )
    }
}

private fun Coordinate.appleMapUri(): String {
    val center = "$latitudeText,$longitudeText"

    return "$APPLE_MAP_URI?ll=$center&q=$center"
}

// 종일 일정은 기기의 현지 날짜로 표시되므로 현지 자정으로 넘긴다.
private fun EKEvent.applySchedule(schedule: QrEventSchedule?) {
    when (schedule) {
        is QrEventSchedule.AllDay -> {
            val timeZone = TimeZone.currentSystemDefault()
            allDay = true
            startDate = schedule.start.atStartOfDayIn(timeZone).toNSDate()
            endDate = schedule.endInclusive.atStartOfDayIn(timeZone).toNSDate()
        }

        is QrEventSchedule.DateTime -> {
            startDate = schedule.start.toNSDate()
            endDate = schedule.end.toNSDate()
        }

        null -> Unit
    }
}

private fun Instant.toNSDate(): NSDate = NSDate.dateWithTimeIntervalSince1970(toEpochMilliseconds() / MILLIS_PER_SECOND)

private const val MILLIS_PER_SECOND = 1_000.0

private class ContactViewDelegate :
    NSObject(),
    CNContactViewControllerDelegateProtocol {
    override fun contactViewController(
        viewController: CNContactViewController,
        didCompleteWithContact: CNContact?,
    ) {
        viewController.dismissViewControllerAnimated(flag = true, completion = null)
    }
}

private class EventEditViewDelegate :
    NSObject(),
    EKEventEditViewDelegateProtocol {
    override fun eventEditViewController(
        controller: EKEventEditViewController,
        didCompleteWithAction: EKEventEditViewAction,
    ) {
        controller.dismissViewControllerAnimated(flag = true, completion = null)
    }
}
