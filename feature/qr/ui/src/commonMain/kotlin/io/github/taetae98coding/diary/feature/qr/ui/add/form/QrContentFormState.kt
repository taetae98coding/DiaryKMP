package io.github.taetae98coding.diary.feature.qr.ui.add.form

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputState
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDateTimeInputState
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.core.model.place.toPlaceCoordinateText
import io.github.taetae98coding.diary.domain.qr.content.QrContent
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import io.github.taetae98coding.diary.domain.qr.content.coordinate
import io.github.taetae98coding.diary.domain.qr.content.detectQrFormat
import io.github.taetae98coding.diary.domain.qr.content.patch
import io.github.taetae98coding.diary.domain.qr.content.readContent
import io.github.taetae98coding.diary.domain.qr.content.recognizes
import io.github.taetae98coding.diary.feature.qr.ui.code.fitsInQrCode
import kotlin.time.Clock

@Stable
internal class QrContentFormState(
    formatState: MutableState<QrFormat>,
    wifiSecurityState: MutableState<QrWifiSecurity>,
    isWifiHiddenState: MutableState<Boolean>,
    private val fieldStateMap: Map<QrTextField, QrFieldState>,
    undoRecordState: MutableState<QrUndoRecord?>,
    val eventPeriodState: DiaryDateTimeInputState,
    val mapState: DiaryMapState,
    private val clock: Clock,
) {
    var format: QrFormat by formatState
        private set

    var wifiSecurity: QrWifiSecurity by wifiSecurityState

    var isWifiHidden: Boolean by isWifiHiddenState

    private var undoRecord: QrUndoRecord? by undoRecordState

    val raw: String
        get() = rawState.text

    val content: QrContent
        get() = contentOf(format = format)

    val qrValue: String by derivedStateOf {
        raw.takeIf { value -> value.isEmpty() || value.fitsInQrCode() }.orEmpty()
    }

    val canUndo: Boolean
        get() = undoRecord != null

    val spot: DiaryMapCoordinate?
        get() = (contentOf(format = QrFormat.LOCATION) as QrContent.Location).coordinate?.toDiaryMapCoordinate()

    private val rawState: QrFieldState
        get() = fieldState(QrTextField.TEXT)

    // QR 값에 마지막으로 반영한 포맷 입력이다. 다음에 고친 입력만 골라 QR 값에 쓰려고 비교한다.
    private var writtenContent: QrContent = contentOf(format = format)

    private val inputTransformationMap: Map<QrTextField, InputTransformation> =
        QrTextField.entries.associateWith { field ->
            QrFieldInputTransformation(
                field = field,
                rawProvider = { raw },
                writtenContentProvider = { writtenContent },
                contentOf = { format, editedField, editedText -> contentOf(format = format, editedField = editedField, editedText = editedText) },
            )
        }

    fun fieldState(field: QrTextField): QrFieldState = fieldStateMap.getValue(field)

    fun inputTransformation(field: QrTextField): InputTransformation = inputTransformationMap.getValue(field)

    fun selectFormat(format: QrFormat) {
        this.format = format
        loadFields()
    }

    fun applyScannedValue(value: String) {
        record()
        rawState.setText(value)
        selectFormat(detectQrFormat(value))
    }

    fun undo() {
        val record = undoRecord ?: return
        undoRecord = null
        rawState.setText(record.raw)
        selectFormat(record.format)
    }

    fun writeFields() {
        if (format == QrFormat.TEXT) return

        val content = content
        if (content == writtenContent) return

        val isWrittenValue = raw == writtenContent.encode()
        if (!format.recognizes(raw) && !isWrittenValue) record()
        rawState.setText(content.patch(raw = raw, previous = writtenContent))
        writtenContent = content
    }

    fun selectSpotOnMap(coordinate: DiaryMapCoordinate) {
        fieldState(QrTextField.LATITUDE).setText(coordinate.latitude.toPlaceCoordinateText())
        fieldState(QrTextField.LONGITUDE).setText(coordinate.longitude.toPlaceCoordinateText())
        mapState.selectSpot(spot)
    }

    fun clear() {
        rawState.clearText()
        undoRecord = null
        loadFields()
        mapState.selectSpot(null)
    }

    private fun record() {
        val raw = raw
        if (raw.isEmpty()) return

        undoRecord = QrUndoRecord(format = detectQrFormat(raw), raw = raw)
    }

    private fun loadFields() {
        applyFields(content = format.readContent(raw) ?: emptyContent(format = format, clock = clock))
        writtenContent = contentOf(format = format)
    }

    private fun contentOf(
        format: QrFormat,
        editedField: QrTextField? = null,
        editedText: String = "",
    ): QrContent {
        fun text(field: QrTextField): String = if (field == editedField) editedText else fieldState(field).text

        return when (format) {
            QrFormat.TEXT -> QrContent.Text(text = text(QrTextField.TEXT))

            QrFormat.URL -> QrContent.Url(url = text(QrTextField.URL))

            QrFormat.CONTACT ->
                QrContent.Contact(
                    name = text(QrTextField.CONTACT_NAME),
                    phoneNumber = text(QrTextField.CONTACT_PHONE_NUMBER),
                    email = text(QrTextField.CONTACT_EMAIL),
                    company = text(QrTextField.CONTACT_COMPANY),
                    address = text(QrTextField.CONTACT_ADDRESS),
                    website = text(QrTextField.CONTACT_WEBSITE),
                )

            QrFormat.WIFI ->
                QrContent.Wifi(
                    ssid = text(QrTextField.WIFI_SSID),
                    security = wifiSecurity,
                    password = text(QrTextField.WIFI_PASSWORD),
                    isHidden = isWifiHidden,
                )

            QrFormat.LOCATION ->
                QrContent.Location(
                    latitude = text(QrTextField.LATITUDE),
                    longitude = text(QrTextField.LONGITUDE),
                )

            QrFormat.EMAIL ->
                QrContent.Email(
                    to = text(QrTextField.EMAIL_TO),
                    subject = text(QrTextField.EMAIL_SUBJECT),
                    body = text(QrTextField.EMAIL_BODY),
                )

            QrFormat.PHONE -> QrContent.Phone(phoneNumber = text(QrTextField.PHONE_NUMBER))

            QrFormat.SMS ->
                QrContent.Sms(
                    phoneNumber = text(QrTextField.SMS_PHONE_NUMBER),
                    message = text(QrTextField.SMS_MESSAGE),
                )

            QrFormat.EVENT ->
                QrContent.Event(
                    title = text(QrTextField.EVENT_TITLE),
                    period = eventPeriodState.value?.toQrEventPeriod(),
                    location = text(QrTextField.EVENT_LOCATION),
                    description = text(QrTextField.EVENT_DESCRIPTION),
                )
        }
    }
}

internal fun QrContentFormState.requestFocusFirstField() {
    fieldState(format.firstTextField).requestFocus()
}

private fun QrContentFormState.applyFields(content: QrContent) {
    content.fieldTextMap().forEach { (field, text) -> fieldState(field).setText(text) }

    when (content) {
        is QrContent.Wifi -> {
            wifiSecurity = content.security
            isWifiHidden = content.isHidden
        }

        is QrContent.Event -> {
            val period = content.period
            if (period == null) eventPeriodState.hasDateTime = false else eventPeriodState.select(period.toDiaryDateTimeInputValue())
        }

        else -> Unit
    }
}

private fun QrContent.fieldTextMap(): Map<QrTextField, String> =
    when (this) {
        is QrContent.Text -> emptyMap()

        is QrContent.Url -> mapOf(QrTextField.URL to url)

        is QrContent.Contact ->
            mapOf(
                QrTextField.CONTACT_NAME to name,
                QrTextField.CONTACT_PHONE_NUMBER to phoneNumber,
                QrTextField.CONTACT_EMAIL to email,
                QrTextField.CONTACT_COMPANY to company,
                QrTextField.CONTACT_ADDRESS to address,
                QrTextField.CONTACT_WEBSITE to website,
            )

        is QrContent.Wifi -> mapOf(QrTextField.WIFI_SSID to ssid, QrTextField.WIFI_PASSWORD to password)

        is QrContent.Location -> mapOf(QrTextField.LATITUDE to latitude, QrTextField.LONGITUDE to longitude)

        is QrContent.Email -> mapOf(QrTextField.EMAIL_TO to to, QrTextField.EMAIL_SUBJECT to subject, QrTextField.EMAIL_BODY to body)

        is QrContent.Phone -> mapOf(QrTextField.PHONE_NUMBER to phoneNumber)

        is QrContent.Sms -> mapOf(QrTextField.SMS_PHONE_NUMBER to phoneNumber, QrTextField.SMS_MESSAGE to message)

        is QrContent.Event ->
            mapOf(
                QrTextField.EVENT_TITLE to title,
                QrTextField.EVENT_LOCATION to location,
                QrTextField.EVENT_DESCRIPTION to description,
            )
    }

private fun emptyContent(
    format: QrFormat,
    clock: Clock,
): QrContent =
    when (format) {
        QrFormat.TEXT -> QrContent.Text()
        QrFormat.URL -> QrContent.Url()
        QrFormat.CONTACT -> QrContent.Contact()
        QrFormat.WIFI -> QrContent.Wifi()
        QrFormat.LOCATION -> QrContent.Location()
        QrFormat.EMAIL -> QrContent.Email()
        QrFormat.PHONE -> QrContent.Phone()
        QrFormat.SMS -> QrContent.Sms()
        QrFormat.EVENT -> QrContent.Event(period = clock.todayEventPeriod())
    }

@Composable
internal fun rememberQrContentFormState(
    initialValue: String = "",
    defaultProvider: MapProvider? = null,
    clock: Clock = Clock.System,
): QrContentFormState {
    // 일정 기간 입력 상태가 초기값을 키로 삼으므로, 이후에 들어온 값으로 바뀌어 입력 상태가 새로 만들어지지 않도록 처음 값을 고정한다.
    val filledValue = rememberSaveable { initialValue }
    val initialFormat = remember(filledValue) { detectQrFormat(filledValue) }
    val initialContent = remember(initialFormat, filledValue, clock) { initialFormat.readContent(filledValue) ?: emptyContent(format = initialFormat, clock = clock) }
    val initialFieldTextMap = remember(initialContent, filledValue) { initialContent.fieldTextMap() + (QrTextField.TEXT to filledValue) }
    val initialWifi = initialContent as? QrContent.Wifi
    val formatState = rememberSaveable(stateSaver = QrFormatSaver) { mutableStateOf(initialFormat) }
    val wifiSecurityState = rememberSaveable(stateSaver = QrWifiSecuritySaver) { mutableStateOf(initialWifi?.security ?: QrWifiSecurity.WPA) }
    val isWifiHiddenState = rememberSaveable { mutableStateOf(initialWifi?.isHidden ?: false) }
    val fieldStateMap = QrTextField.entries.associateWith { field -> key(field) { rememberQrFieldState(initialText = initialFieldTextMap[field].orEmpty()) } }
    val undoRecordState = rememberSaveable(stateSaver = QrUndoRecordSaver) { mutableStateOf<QrUndoRecord?>(null) }
    val initialEventPeriod =
        remember(initialContent, clock) {
            if (initialContent is QrContent.Event) initialContent.period?.toDiaryDateTimeInputValue() else clock.todayEventPeriod().toDiaryDateTimeInputValue()
        }
    val eventPeriodState = rememberDiaryDateTimeInputState(initialValue = initialEventPeriod)
    val mapState =
        if (defaultProvider == null) {
            rememberDiaryMapState()
        } else {
            key(defaultProvider) {
                rememberDiaryMapState(initialProvider = defaultProvider.toDiaryMapProvider())
            }
        }

    return remember(formatState, wifiSecurityState, isWifiHiddenState, fieldStateMap, undoRecordState, eventPeriodState, mapState, clock) {
        QrContentFormState(
            formatState = formatState,
            wifiSecurityState = wifiSecurityState,
            isWifiHiddenState = isWifiHiddenState,
            fieldStateMap = fieldStateMap,
            undoRecordState = undoRecordState,
            eventPeriodState = eventPeriodState,
            mapState = mapState,
            clock = clock,
        )
    }
}

private val QrFormatSaver: Saver<QrFormat, String> = Saver(save = { format -> format.name }, restore = QrFormat::valueOf)

private val QrWifiSecuritySaver: Saver<QrWifiSecurity, String> = Saver(save = { security -> security.name }, restore = QrWifiSecurity::valueOf)
