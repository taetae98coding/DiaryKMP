package io.github.taetae98coding.diary.domain.file.exception

public class FileUnreadableException(
    public val name: String,
    override val cause: Throwable,
) : Exception("File cannot be read. name=$name", cause)
