package io.github.taetae98coding.diary.core.file.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class JvmAppFileDirectoryTest :
    FunSpec({
        test("TC-JVM-DATABASE-STORAGE-DATA-004 내려받은 파일을 그 환경의 저장 위치에 둔다") {
            val userHome = Paths.get("/Users/user")
            val cases =
                mapOf(
                    "DiaryDev" to Paths.get("/Users/user/Library/Application Support/DiaryDev"),
                    "Diary" to Paths.get("/Users/user/Library/Application Support/Diary"),
                )

            cases.forEach { (directoryName, expectedPath) ->
                resolveAppFileDirectory(userHome = userHome, directoryName = directoryName) shouldBe expectedPath
            }
        }
    })
