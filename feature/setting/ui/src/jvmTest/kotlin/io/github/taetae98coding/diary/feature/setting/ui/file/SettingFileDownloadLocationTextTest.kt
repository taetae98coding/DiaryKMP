package io.github.taetae98coding.diary.feature.setting.ui.file

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileDownloadDirectory
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class SettingFileDownloadLocationTextTest :
    FunSpec({
        test("TC-54007111-1d12-4faf-89ae-1c4c34ad4b10 데스크톱 앱에서 고른 폴더의 보조 표시는 전체 경로다") {
            val directory = fixtureMonkey.giveMeOne<FileDownloadDirectory>()

            directory.supportingText() shouldBe SettingFileLocationSupportingText.Text(text = directory.uri)
        }
    })
