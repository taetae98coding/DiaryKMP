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

        test("TC-FILE-DOWNLOAD-FEATURE-001 데스크톱 앱의 기본 다운로드 위치는 사용자 홈의 Downloads 폴더다") {
            resolveFileDownloadDefaultDirectory(userHome = Paths.get("/Users/user")) shouldBe Paths.get("/Users/user/Downloads")
        }
    })
