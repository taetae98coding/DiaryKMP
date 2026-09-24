package io.github.taetae98coding.diary.core.model.contact

import kotlinx.datetime.LocalDate
import kotlin.uuid.Uuid

public data class LunarContactBirthday(
    val contactId: Uuid,
    val name: String,
    val birthday: LocalDate,
)
