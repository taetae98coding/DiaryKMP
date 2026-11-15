package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.runtime.Immutable

@Immutable
public sealed interface ShareContent {
    public val title: String

    @Immutable
    public data class Text(
        val text: String,
        override val title: String = "",
    ) : ShareContent

    @Immutable
    public class File(
        public val bytes: ByteArray,
        public val name: String,
        public val type: ShareFileType,
        public val text: String = "",
        override val title: String = "",
    ) : ShareContent {
        public val fileName: String get() = type.fileNameOf(name = name)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is File) return false

            return bytes.contentEquals(other.bytes) && name == other.name && type == other.type && text == other.text && title == other.title
        }

        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = HASH_MULTIPLIER * result + name.hashCode()
            result = HASH_MULTIPLIER * result + type.hashCode()
            result = HASH_MULTIPLIER * result + text.hashCode()
            result = HASH_MULTIPLIER * result + title.hashCode()
            return result
        }

        override fun toString(): String = "File(bytes=${bytes.size}B, name=$name, type=$type, text=$text, title=$title)"
    }
}

private const val HASH_MULTIPLIER = 31
