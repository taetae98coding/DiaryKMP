package io.github.taetae98coding.diary.domain.share

public sealed interface ShareContent {
    public data class Text(
        val text: String,
        val subject: String = "",
    ) : ShareContent

    public data class Contact(
        val vCard: String,
    ) : ShareContent

    public data class Event(
        val iCalendar: String,
    ) : ShareContent
}
