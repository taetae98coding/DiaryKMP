package io.github.taetae98coding.diary.feature.qr.ui.add

import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputState
import io.github.taetae98coding.diary.compose.core.input.DiaryDateTimeInputValue
import io.github.taetae98coding.diary.compose.core.input.rememberDiaryDateTimeInputState
import io.github.taetae98coding.diary.compose.map.DiaryMapCoordinate
import io.github.taetae98coding.diary.compose.map.DiaryMapState
import io.github.taetae98coding.diary.compose.map.rememberDiaryMapState
import io.github.taetae98coding.diary.compose.place.toDiaryMapCoordinate
import io.github.taetae98coding.diary.compose.place.toDiaryMapProvider
import io.github.taetae98coding.diary.core.model.map.MapProvider
import io.github.taetae98coding.diary.domain.place.toPlaceCoordinateText
import io.github.taetae98coding.diary.domain.qr.content.MAX_QR_UNDO_RECORD_COUNT
import io.github.taetae98coding.diary.domain.qr.content.QrContent
import io.github.taetae98coding.diary.domain.qr.content.QrEventPeriod
import io.github.taetae98coding.diary.domain.qr.content.QrFormat
import io.github.taetae98coding.diary.domain.qr.content.QrWifiSecurity
import io.github.taetae98coding.diary.domain.qr.content.coordinate
import io.github.taetae98coding.diary.domain.qr.content.detectQrFormat
import io.github.taetae98coding.diary.domain.qr.content.patch
import io.github.taetae98coding.diary.domain.qr.content.readContent
import io.github.taetae98coding.diary.domain.qr.content.recognizes
import io.github.taetae98coding.diary.feature.qr.ui.code.fitsInQrCode
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Stable
internal class QrContentFormState(
    formatState: MutableState<QrFormat>,
    wifiSecurityState: MutableState<QrWifiSecurity>,
    isWifiHiddenState: MutableState<Boolean>,
    private val fieldStateMap: Map<QrTextField, QrFieldState>,
    private val undoRecordList: SnapshotStateList<QrUndoRecord>,
    val eventPeriodState: DiaryDateTimeInputState,
    val mapState: DiaryMapState,
    private val clock: Clock,
) {
    var format: QrFormat by formatState
        private set

    var wifiSecurity: QrWifiSecurity by wifiSecurityState

    var isWifiHidden: Boolean by isWifiHiddenState

    val raw: String
        get() = rawState.text

    val content: QrContent
        get() = contentOf(format = format)

    val qrValue: String by derivedStateOf {
        raw.takeIf { value -> value.isEmpty() || value.fitsInQrCode() }.orEmpty()
    }

    val canUndo: Boolean by derivedStateOf { undoRecordList.isNotEmpty() }

    val spot: DiaryMapCoordinate?
        get() = (contentOf(format = QrFormat.LOCATION) as QrContent.Location).coordinate?.toDiaryMapCoordinate()

    private val rawState: QrFieldState
        get() = fieldState(QrTextField.TEXT)

    // QR 값에 마지막으로 반영한 포맷 입력이다. 다음에 고친 입력만 골라 QR 값에 쓰려고 비교한다.
    private var writtenContent: QrContent = contentOf(format = format)

    private val inputTransformationMap: Map<QrTextField, InputTransformation> =
        QrTextField.entries.associateWith { field -> QrFieldInputTransformation(field = field) }

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
        val record = undoRecordList.removeLastOrNull() ?: return
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
        undoRecordList.clear()
        loadFields()
        mapState.selectSpot(null)
    }

    private fun record() {
        val raw = raw
        if (raw.isEmpty()) return

        undoRecordList += QrUndoRecord(format = detectQrFormat(raw), raw = raw)
        while (undoRecordList.size > MAX_QR_UNDO_RECORD_COUNT) undoRecordList.removeAt(0)
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

    private inner class QrFieldInputTransformation(
        private val field: QrTextField,
    ) : InputTransformation {
        override fun TextFieldBuffer.transformInput() {
            val format = field.format
            if (format == QrFormat.LOCATION) return

            val edited = asCharSequence().toString()
            val value =
                if (format == QrFormat.TEXT) {
                    edited
                } else {
                    contentOf(format = format, editedField = field, editedText = edited).patch(raw = raw, previous = writtenContent)
                }

            if (value.isNotEmpty() && !value.fitsInQrCode()) {
                revertAllChanges()
            }
        }
    }
}

internal fun QrContentFormState.requestFocusFirstField() {
    fieldState(format.firstTextField).requestFocus()
}

private fun QrContentFormState.applyFields(content: QrContent) {
    fun setFields(vararg fieldList: Pair<QrTextField, String>) {
        fieldList.forEach { (field, text) -> fieldState(field).setText(text) }
    }

    when (content) {
        is QrContent.Text -> Unit

        is QrContent.Url -> setFields(QrTextField.URL to content.url)

        is QrContent.Contact ->
            setFields(
                QrTextField.CONTACT_NAME to content.name,
                QrTextField.CONTACT_PHONE_NUMBER to content.phoneNumber,
                QrTextField.CONTACT_EMAIL to content.email,
                QrTextField.CONTACT_COMPANY to content.company,
                QrTextField.CONTACT_ADDRESS to content.address,
                QrTextField.CONTACT_WEBSITE to content.website,
            )

        is QrContent.Wifi -> {
            setFields(QrTextField.WIFI_SSID to content.ssid, QrTextField.WIFI_PASSWORD to content.password)
            wifiSecurity = content.security
            isWifiHidden = content.isHidden
        }

        is QrContent.Location -> setFields(QrTextField.LATITUDE to content.latitude, QrTextField.LONGITUDE to content.longitude)

        is QrContent.Email ->
            setFields(QrTextField.EMAIL_TO to content.to, QrTextField.EMAIL_SUBJECT to content.subject, QrTextField.EMAIL_BODY to content.body)

        is QrContent.Phone -> setFields(QrTextField.PHONE_NUMBER to content.phoneNumber)

        is QrContent.Sms -> setFields(QrTextField.SMS_PHONE_NUMBER to content.phoneNumber, QrTextField.SMS_MESSAGE to content.message)

        is QrContent.Event -> {
            setFields(
                QrTextField.EVENT_TITLE to content.title,
                QrTextField.EVENT_LOCATION to content.location,
                QrTextField.EVENT_DESCRIPTION to content.description,
            )
            val period = content.period
            if (period == null) eventPeriodState.hasDateTime = false else eventPeriodState.select(period.toDiaryDateTimeInputValue())
        }
    }
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

internal data class QrUndoRecord(
    val format: QrFormat,
    val raw: String,
)

private val QrTextField.format: QrFormat
    get() =
        when (this) {
            QrTextField.TEXT -> QrFormat.TEXT

            QrTextField.URL -> QrFormat.URL

            QrTextField.CONTACT_NAME,
            QrTextField.CONTACT_PHONE_NUMBER,
            QrTextField.CONTACT_EMAIL,
            QrTextField.CONTACT_COMPANY,
            QrTextField.CONTACT_ADDRESS,
            QrTextField.CONTACT_WEBSITE,
            -> QrFormat.CONTACT

            QrTextField.WIFI_SSID,
            QrTextField.WIFI_PASSWORD,
            -> QrFormat.WIFI

            QrTextField.LATITUDE,
            QrTextField.LONGITUDE,
            -> QrFormat.LOCATION

            QrTextField.EMAIL_TO,
            QrTextField.EMAIL_SUBJECT,
            QrTextField.EMAIL_BODY,
            -> QrFormat.EMAIL

            QrTextField.PHONE_NUMBER -> QrFormat.PHONE

            QrTextField.SMS_PHONE_NUMBER,
            QrTextField.SMS_MESSAGE,
            -> QrFormat.SMS

            QrTextField.EVENT_TITLE,
            QrTextField.EVENT_LOCATION,
            QrTextField.EVENT_DESCRIPTION,
            -> QrFormat.EVENT
        }

private fun DiaryDateTimeInputValue.toQrEventPeriod(): QrEventPeriod =
    when (this) {
        is DiaryDateTimeInputValue.AllDay -> QrEventPeriod.AllDay(start = dateRange.start, endInclusive = dateRange.endInclusive)
        is DiaryDateTimeInputValue.DateTime -> QrEventPeriod.DateTime(start = start, endInclusive = endInclusive)
    }

private fun QrEventPeriod.toDiaryDateTimeInputValue(): DiaryDateTimeInputValue =
    when (this) {
        is QrEventPeriod.AllDay -> DiaryDateTimeInputValue.AllDay(dateRange = start..endInclusive)
        is QrEventPeriod.DateTime -> DiaryDateTimeInputValue.DateTime(start = start, endInclusive = endInclusive)
    }

private fun Clock.todayEventPeriod(): QrEventPeriod {
    val today = now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    return QrEventPeriod.AllDay(start = today, endInclusive = today)
}

@Composable
internal fun rememberQrContentFormState(
    defaultProvider: MapProvider? = null,
    clock: Clock = Clock.System,
): QrContentFormState {
    val formatState = rememberSaveable(stateSaver = QrFormatSaver) { mutableStateOf(QrFormat.TEXT) }
    val wifiSecurityState = rememberSaveable(stateSaver = QrWifiSecuritySaver) { mutableStateOf(QrWifiSecurity.WPA) }
    val isWifiHiddenState = rememberSaveable { mutableStateOf(false) }
    val fieldStateMap = QrTextField.entries.associateWith { field -> key(field) { rememberQrFieldState() } }
    val undoRecordList = rememberSaveable(saver = QrUndoRecordListSaver) { mutableListOf<QrUndoRecord>().toMutableStateList() }
    val initialEventPeriod = remember(clock) { clock.todayEventPeriod().toDiaryDateTimeInputValue() }
    val eventPeriodState = rememberDiaryDateTimeInputState(initialValue = initialEventPeriod)
    val mapState =
        if (defaultProvider == null) {
            rememberDiaryMapState()
        } else {
            key(defaultProvider) {
                rememberDiaryMapState(initialProvider = defaultProvider.toDiaryMapProvider())
            }
        }

    return remember(formatState, wifiSecurityState, isWifiHiddenState, fieldStateMap, undoRecordList, eventPeriodState, mapState, clock) {
        QrContentFormState(
            formatState = formatState,
            wifiSecurityState = wifiSecurityState,
            isWifiHiddenState = isWifiHiddenState,
            fieldStateMap = fieldStateMap,
            undoRecordList = undoRecordList,
            eventPeriodState = eventPeriodState,
            mapState = mapState,
            clock = clock,
        )
    }
}

private val QrFormatSaver: Saver<QrFormat, String> = Saver(save = { format -> format.name }, restore = QrFormat::valueOf)

private val QrWifiSecuritySaver: Saver<QrWifiSecurity, String> = Saver(save = { security -> security.name }, restore = QrWifiSecurity::valueOf)

private val QrUndoRecordListSaver: Saver<SnapshotStateList<QrUndoRecord>, Any> =
    listSaver(
        save = { recordList -> recordList.flatMap { record -> listOf(record.format.name, record.raw) } },
        restore = { saved ->
            saved
                .chunked(2)
                .map { (format, raw) -> QrUndoRecord(format = QrFormat.valueOf(format), raw = raw) }
                .toMutableStateList()
        },
    )
