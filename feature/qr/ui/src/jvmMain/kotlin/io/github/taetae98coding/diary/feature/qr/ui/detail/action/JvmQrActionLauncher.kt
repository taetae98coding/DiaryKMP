package io.github.taetae98coding.diary.feature.qr.ui.detail.action

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.taetae98coding.diary.domain.qr.content.QrAction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.qualifier.named
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.nio.file.Files

private const val CONTACT_FILE_SUFFIX = ".vcf"
private const val EVENT_FILE_SUFFIX = ".ics"
private const val FILE_PREFIX = "qr"

@Composable
internal actual fun rememberQrActionLauncher(): QrActionLauncher {
    val dispatcher = koinInject<CoroutineDispatcher>(qualifier = named<QrActionDispatcher>())

    return remember(dispatcher) { JvmQrActionLauncher(dispatcher = dispatcher) }
}

internal class JvmQrActionLauncher(
    private val dispatcher: CoroutineDispatcher,
) : QrActionLauncher {
    override fun isSupported(action: QrAction): Boolean = action !is QrAction.JoinWifi

    // 앱을 찾아 넘기는 동안과 임시 파일을 쓰는 동안 호출한 스레드가 막히므로 메인 스레드 밖에서 한다.
    // 받을 앱이 없으면 IOException으로, 그 환경이 열기를 지원하지 않으면 UnsupportedOperationException으로 알려진다.
    override suspend fun launch(action: QrAction): Boolean =
        withContext(dispatcher) {
            try {
                when (action) {
                    is QrAction.OpenUrl -> browse(uri = action.url)
                    is QrAction.Call -> browse(uri = action.telUri())
                    is QrAction.SendEmail -> browse(uri = action.mailtoUri())
                    is QrAction.SendSms -> browse(uri = action.smsUri(bodySeparator = '?'))
                    is QrAction.ShowLocation -> browse(uri = action.coordinate.googleMapWebUri())
                    is QrAction.AddContact -> openFile(content = action.vCard, suffix = CONTACT_FILE_SUFFIX)
                    is QrAction.AddEvent -> openFile(content = action.calendarFile, suffix = EVENT_FILE_SUFFIX)
                    is QrAction.JoinWifi -> false
                }
            } catch (_: IOException) {
                false
            } catch (_: UnsupportedOperationException) {
                false
            } catch (_: URISyntaxException) {
                false
            } catch (_: SecurityException) {
                false
            }
        }

    private fun browse(uri: String): Boolean {
        if (!Desktop.isDesktopSupported()) return false

        Desktop.getDesktop().browse(URI(uri))
        return true
    }

    // 넘겨받은 앱이 파일을 나중에 읽을 수 있으므로 앱이 끝날 때 지운다.
    private fun openFile(
        content: String,
        suffix: String,
    ): Boolean {
        if (!Desktop.isDesktopSupported()) return false

        val file = Files.createTempFile(FILE_PREFIX, suffix).toFile()
        file.deleteOnExit()
        file.writeText(content)
        Desktop.getDesktop().open(file)
        return true
    }
}
