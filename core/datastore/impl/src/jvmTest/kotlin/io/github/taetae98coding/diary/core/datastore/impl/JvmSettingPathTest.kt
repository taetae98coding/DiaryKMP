package io.github.taetae98coding.diary.core.datastore.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths

class JvmSettingPathTest :
    FunSpec({
        test("TC-SETTING-MAP-DATA-006 flavor에 맞는 설정 저장 영역 선택") {
            val userHome = Paths.get("/Users/user")
            val cases =
                mapOf(
                    "DiaryDev" to Paths.get("/Users/user/Library/Application Support/DiaryDev"),
                    "Diary" to Paths.get("/Users/user/Library/Application Support/Diary"),
                )

            cases.forEach { (settingDirectory, expectedPath) ->
                resolveSettingDirectory(userHome, settingDirectory) shouldBe expectedPath
            }
        }

        test("TC-SETTING-MAP-DATA-006 개발과 운영 설정 저장 영역 분리") {
            val userHome = Paths.get("/Users/user")
            val devPath = resolveSettingDirectory(userHome, "DiaryDev")
            val realPath = resolveSettingDirectory(userHome, "Diary")

            devPath shouldNotBe realPath
        }

        test("역할별 설정은 같은 저장 영역 안에서 서로 다른 파일을 쓴다") {
            val directory = resolveSettingDirectory(Paths.get("/Users/user"), "Diary")
            val names = listOf(DataStoreModule.MAP_SETTING_NAME, DataStoreModule.HOLIDAY_SETTING_NAME, DataStoreModule.GEMINI_SETTING_NAME)

            names.map { name -> directory.resolve(name) }.toSet().size shouldBe names.size
        }
    })
