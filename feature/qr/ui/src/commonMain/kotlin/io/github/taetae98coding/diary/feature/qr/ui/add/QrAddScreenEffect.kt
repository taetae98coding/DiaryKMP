package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.withFrameNanos
import io.github.taetae98coding.diary.compose.core.effect.CollectEffect
import io.github.taetae98coding.diary.compose.core.snackbar.showImmediate
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.feature.qr.ui.Res
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_succeeded_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_add_title_blank_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_contact_name_empty_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_coordinate_invalid_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_email_to_empty_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_event_title_empty_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_phone_number_empty_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_text_empty_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_url_empty_message
import io.github.taetae98coding.diary.feature.qr.ui.qr_wifi_ssid_empty_message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun QrAddScreenEffect(
    effect: Flow<QrAddEffect> = emptyFlow(),
    state: QrAddFormState = rememberQrAddFormState(),
) {
    val coroutineScope = rememberCoroutineScope()
    val addSucceededMessage = stringResource(Res.string.qr_add_succeeded_message)
    val titleBlankMessage = stringResource(Res.string.qr_add_title_blank_message)
    val emptyMessageMap = QrFormat.entries.associateWith { format -> stringResource(format.emptyMessage) }

    CollectEffect(effect) { value ->
        when (value) {
            is QrAddEffect.AddSucceeded -> {
                state.clear()
                state.tab = QrAddTab.INFO
                state.scrollState.scrollTo(0)
                awaitTabComposed()
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = addSucceededMessage) }
            }

            is QrAddEffect.TitleBlank -> {
                state.tab = QrAddTab.INFO
                awaitTabComposed()
                state.titleState.requestFocus()
                coroutineScope.launch { state.hostState.showImmediate(message = titleBlankMessage) }
            }

            is QrAddEffect.ValueEmpty -> {
                val message = emptyMessageMap.getValue(state.contentState.format)

                state.tab = QrAddTab.QR
                awaitTabComposed()
                state.contentState.requestFocusFirstField()
                coroutineScope.launch { state.hostState.showImmediate(message = message) }
            }
        }
    }
}

// 탭을 바꾼 직후에는 새 탭의 입력이 아직 구성되지 않아 초점을 받을 수 없으므로, 바뀐 탭이 그려진 다음 프레임까지 기다린다.
private suspend fun awaitTabComposed() {
    Snapshot.sendApplyNotifications()
    withFrameNanos { }
}

private val QrFormat.emptyMessage: StringResource
    get() =
        when (this) {
            QrFormat.TEXT -> Res.string.qr_text_empty_message

            QrFormat.URL -> Res.string.qr_url_empty_message

            QrFormat.CONTACT -> Res.string.qr_contact_name_empty_message

            QrFormat.WIFI -> Res.string.qr_wifi_ssid_empty_message

            QrFormat.LOCATION -> Res.string.qr_coordinate_invalid_message

            QrFormat.EMAIL -> Res.string.qr_email_to_empty_message

            QrFormat.PHONE,
            QrFormat.SMS,
            -> Res.string.qr_phone_number_empty_message

            QrFormat.EVENT -> Res.string.qr_event_title_empty_message
        }
