package io.github.taetae98coding.diary.core.calendar.database.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class JvmCalendarDatabasePathTest :
    FunSpec({
        test("TC-0e0520d4-9745-466c-a0ed-d058bd1b67c5 캘린더 자료를 그 환경의 저장 위치에 둔다") {
            val userHome = Paths.get("/Users/user")
            val cases =
                mapOf(
                    "DiaryDev" to Paths.get("/Users/user/Library/Application Support/DiaryDev/calendar.db"),
                    "Diary" to Paths.get("/Users/user/Library/Application Support/Diary/calendar.db"),
                )

            cases.forEach { (databaseDirectory, expectedPath) ->
                resolveCalendarDatabasePath(userHome = userHome, databaseDirectory = databaseDirectory) shouldBe expectedPath
            }
        }
    })
