package io.github.taetae98coding.diary.domain.file

private const val REPLACEMENT = '_'
private const val EXTENSION_SEPARATOR = '.'
private val UNSAFE_CHARACTER_SET: Set<Char> = setOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')
private val RESERVED_NAME_SET: Set<String> = setOf(".", "..")

internal class FileDownloadName(
    name: String,
) {
    private val safeName: String = name.toSafeFileName()

    fun candidate(index: Int): String {
        if (index == 0) return safeName

        val extensionIndex = safeName.lastIndexOf(EXTENSION_SEPARATOR).takeIf { position -> position > 0 }

        return if (extensionIndex == null) {
            "$safeName ($index)"
        } else {
            "${safeName.substring(0, extensionIndex)} ($index)${safeName.substring(extensionIndex)}"
        }
    }
}

private fun String.toSafeFileName(): String {
    val replaced = map { character -> if (character in UNSAFE_CHARACTER_SET || character.isISOControl()) REPLACEMENT else character }.joinToString(separator = "")

    return if (replaced in RESERVED_NAME_SET) REPLACEMENT.toString() else replaced
}
