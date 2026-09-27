package io.github.taetae98coding.diary.domain.qr.content

public sealed interface QrContent {
    public val format: QrFormat

    public fun encode(): String

    public data class Text(
        val text: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.TEXT

        override fun encode(): String = text
    }

    public data class Url(
        val url: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.URL

        override fun encode(): String = url.trim()
    }

    public data class Contact(
        val name: String = "",
        val phoneNumber: String = "",
        val email: String = "",
        val company: String = "",
        val address: String = "",
        val website: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.CONTACT

        override fun encode(): String = encodeContact()
    }

    public data class Wifi(
        val ssid: String = "",
        val security: QrWifiSecurity = QrWifiSecurity.WPA,
        val password: String = "",
        val isHidden: Boolean = false,
    ) : QrContent {
        override val format: QrFormat = QrFormat.WIFI

        override fun encode(): String = encodeWifi()
    }

    public data class Location(
        val latitude: String = "",
        val longitude: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.LOCATION

        override fun encode(): String = encodeLocation()
    }

    public data class Email(
        val to: String = "",
        val subject: String = "",
        val body: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.EMAIL

        override fun encode(): String = encodeEmail()
    }

    public data class Phone(
        val phoneNumber: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.PHONE

        override fun encode(): String = "tel:${phoneNumber.trim()}"
    }

    public data class Sms(
        val phoneNumber: String = "",
        val message: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.SMS

        override fun encode(): String = encodeSms()
    }

    public data class Event(
        val title: String = "",
        val period: QrEventPeriod? = null,
        val location: String = "",
        val description: String = "",
    ) : QrContent {
        override val format: QrFormat = QrFormat.EVENT

        override fun encode(): String = encodeEvent()
    }
}
