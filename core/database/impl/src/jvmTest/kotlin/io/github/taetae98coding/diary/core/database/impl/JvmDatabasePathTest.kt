package io.github.taetae98coding.diary.core.database.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.nio.file.Paths

class JvmDatabasePathTest :
    FunSpec({
        test("TC-eeadacf6-ec33-4aa2-a248-0ba79aa60735 flavor에 맞는 데이터베이스 저장 경로 선택") {
            val userHome = Paths.get("/Users/user")
            val cases =
                mapOf(
                    "DiaryDev" to Paths.get("/Users/user/Library/Application Support/DiaryDev/diary.db"),
                    "Diary" to Paths.get("/Users/user/Library/Application Support/Diary/diary.db"),
                )

            cases.forEach { (databaseDirectory, expectedPath) ->
                resolveDatabasePath(userHome, databaseDirectory) shouldBe expectedPath
            }
        }

        test("TC-f725c335-e15d-4411-aaae-72b543402133 개발과 운영 데이터베이스 저장 영역 분리") {
            val userHome = Paths.get("/Users/user")
            val devPath = resolveDatabasePath(userHome, "DiaryDev")
            val realPath = resolveDatabasePath(userHome, "Diary")

            devPath shouldNotBe realPath
        }
    })
