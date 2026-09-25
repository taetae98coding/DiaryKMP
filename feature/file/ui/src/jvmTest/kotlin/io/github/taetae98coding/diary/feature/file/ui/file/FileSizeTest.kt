package io.github.taetae98coding.diary.feature.file.ui.file

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class FileSizeTest :
    FunSpec({
        test("1 이상이 되는 가장 큰 단위로 나타내고 소수점 아래 둘째 자리부터 버린다") {
            mapOf(
                0L to FileSize(value = "0", unit = FileSizeUnit.BYTE),
                512L to FileSize(value = "512", unit = FileSizeUnit.BYTE),
                1_023L to FileSize(value = "1023", unit = FileSizeUnit.BYTE),
                1_024L to FileSize(value = "1.0", unit = FileSizeUnit.KILOBYTE),
                1_535L to FileSize(value = "1.4", unit = FileSizeUnit.KILOBYTE),
                1_048_575L to FileSize(value = "1023.9", unit = FileSizeUnit.KILOBYTE),
                1_048_576L to FileSize(value = "1.0", unit = FileSizeUnit.MEGABYTE),
                24_536_678L to FileSize(value = "23.3", unit = FileSizeUnit.MEGABYTE),
                24_536_679L to FileSize(value = "23.4", unit = FileSizeUnit.MEGABYTE),
                52_428_799L to FileSize(value = "49.9", unit = FileSizeUnit.MEGABYTE),
                52_428_800L to FileSize(value = "50.0", unit = FileSizeUnit.MEGABYTE),
            ).forEach { (size, expected) ->
                size.toFileSize() shouldBe expected
            }
        }
    })
