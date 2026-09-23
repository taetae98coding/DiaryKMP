package io.github.taetae98coding.diary.feature.contact.ui.detail

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.contact.ContactBirthday
import io.github.taetae98coding.diary.core.model.contact.ContactDetail
import io.github.taetae98coding.diary.core.model.contact.ContactPhoneNumber
import io.github.taetae98coding.diary.core.model.measure.Length
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

@Suppress("LongParameterList")
internal fun testContactDetail(
    name: String = "이름-${fixtureMonkey.giveMeOne<String>()}",
    description: String = "설명-${fixtureMonkey.giveMeOne<String>()}",
    height: Length? = null,
    footSize: Length? = null,
    birthday: ContactBirthday? = null,
    hometown: String = "",
    phoneNumberList: List<String> = emptyList(),
): ContactDetail =
    ContactDetail(
        name = name,
        description = description,
        height = height,
        footSize = footSize,
        birthday = birthday,
        hometown = hometown,
        phoneNumberList = phoneNumberList.map { number -> ContactPhoneNumber(number = number) },
    )
