@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.domain.qr.content.QrAction

private const val CONTACT_FILE_NAME = "contact.vcf"
private const val CONTACT_MIME_TYPE = "text/vcard"
private const val EVENT_FILE_NAME = "event.ics"
private const val EVENT_MIME_TYPE = "text/calendar"

@Composable
internal actual fun rememberQrActionLauncher(): QrActionLauncher = remember { WasmQrActionLauncher() }

internal class WasmQrActionLauncher : QrActionLauncher {
    override fun isSupported(action: QrAction): Boolean = action !is QrAction.JoinWifi

    // 브라우저는 전화·메일·문자 주소를 맡긴 앱과 내려받기의 결과를 알려 주지 않으므로 넘긴 것으로 본다.
    override suspend fun launch(action: QrAction): Boolean =
        when (action) {
            is QrAction.OpenUrl -> openTab(url = action.url)
            is QrAction.Call -> true.also { assignLocation(url = action.telUri()) }
            is QrAction.SendEmail -> true.also { assignLocation(url = action.mailtoUri()) }
            is QrAction.SendSms -> true.also { assignLocation(url = action.smsUri(bodySeparator = '?')) }
            is QrAction.ShowLocation -> openTab(url = action.coordinate.googleMapWebUri())
            is QrAction.AddContact -> true.also { download(content = action.vCard, fileName = CONTACT_FILE_NAME, mimeType = CONTACT_MIME_TYPE) }
            is QrAction.AddEvent -> true.also { download(content = action.calendarFile, fileName = EVENT_FILE_NAME, mimeType = EVENT_MIME_TYPE) }
            is QrAction.JoinWifi -> false
        }
}

// 새 탭이 막히면 window.open이 null을 돌려준다. 열린 탭이 이 앱을 조작하지 못하도록 opener를 끊는다.
@Suppress("UnusedParameter")
private fun openTab(url: String): Boolean =
    js(
        """
        (() => {
            const opened = window.open(url, '_blank');
            if (opened) {
                opened.opener = null;
            }
            return opened !== null;
        })()
        """,
    )

// 전화·메일·문자 주소는 맡긴 앱으로 넘어갈 뿐 페이지를 바꾸지 않으므로 새 탭을 남기지 않도록 현재 창에서 연다.
@Suppress("UnusedParameter")
private fun assignLocation(url: String): Unit = js("window.location.assign(url)")

@Suppress("UnusedParameter")
private fun download(
    content: String,
    fileName: String,
    mimeType: String,
): Unit =
    js(
        """
        (() => {
            const url = URL.createObjectURL(new Blob([content], { type: mimeType }));
            const anchor = document.createElement('a');
            anchor.href = url;
            anchor.download = fileName;
            anchor.click();
            setTimeout(() => URL.revokeObjectURL(url), 0);
        })()
        """,
    )
