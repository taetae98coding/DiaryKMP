package io.github.taetae98coding.diary.core.file.impl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Paths

class JvmAppFileDirectoryTest :
    FunSpec({
        test("TC-9cf0dcfd-c823-4e15-ba98-6e26de922081 내려받은 파일을 그 환경의 저장 위치에 둔다") {
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

        test("TC-fa368167-d81a-4307-a63d-7e49c56ec884 데스크톱 앱의 기본 다운로드 위치는 사용자 홈의 Downloads 폴더다") {
            resolveFileDownloadDefaultDirectory(userHome = Paths.get("/Users/user")) shouldBe Paths.get("/Users/user/Downloads")
        }
    })
