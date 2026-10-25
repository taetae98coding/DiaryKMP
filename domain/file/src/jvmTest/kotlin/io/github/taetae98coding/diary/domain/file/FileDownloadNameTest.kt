package io.github.taetae98coding.diary.domain.file

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FileDownloadNameTest :
    FunSpec({
        test("TC-f32cfd05-b47c-42da-b74c-0541b4e9d2ab 파일 이름에 쓸 수 없는 글자는 _로 바꿔 저장한다") {
            listOf(
                "report.pdf" to "report.pdf",
                "a/b\\c.txt" to "a_b_c.txt",
                "what?:*.txt" to "what___.txt",
                "\"x\"<y>|z.txt" to "_x__y__z.txt",
                "line1\nline2.txt" to "line1_line2.txt",
                "." to "_",
                ".." to "_",
            ).forEach { (name, expected) ->
                FileDownloadName(name = name).candidate(index = 0) shouldBe expected
            }
        }

        test("TC-f9cd7730-015c-4120-aa0a-dda6e46df8b0 겹칠 때 붙이는 번호는 확장자 앞에, 확장자가 없으면 이름 끝에 붙인다") {
            listOf(
                Triple("report.pdf", 1, "report (1).pdf"),
                Triple("report.pdf", 2, "report (2).pdf"),
                Triple("archive.tar.gz", 1, "archive.tar (1).gz"),
                Triple("README", 1, "README (1)"),
                Triple(".env", 1, ".env (1)"),
            ).forEach { (name, index, expected) ->
                FileDownloadName(name = name).candidate(index = index) shouldBe expected
            }
        }

        test("바꾼 글자가 있는 이름에도 번호는 바꾼 이름 뒤에 붙는다") {
            FileDownloadName(name = "a:b.txt").candidate(index = 3) shouldBe "a_b (3).txt"
        }
    })
