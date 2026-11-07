package io.github.taetae98coding.diary.library.foundation

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import io.kotest.property.Arb
import io.kotest.property.arbitrary.next
import io.kotest.property.arbitrary.string
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.readText
import kotlin.io.path.writeText

class SecurityScopedBookmarkTest :
    FunSpec({
        test("고른 폴더의 북마크로 접근을 얻으면 그 폴더에 쓸 수 있다") {
            val directory = createTempDirectory("diary-bookmark")

            try {
                val bookmark = SecurityScopedBookmark.create(path = directory.toString())

                bookmark.shouldNotBeBlank()
                SecurityScopedBookmark.startAccessing(bookmark = bookmark).use {
                    val content = Arb.string().next()
                    val file = directory.resolve("written.txt")
                    file.writeText(content)

                    file.readText() shouldBe content
                }
            } finally {
                directory.toFile().deleteRecursively()
            }
        }

        test("없는 폴더로는 북마크를 만들지 못한다") {
            val directory = createTempDirectory("diary-bookmark")
            Files.delete(directory)

            shouldThrow<IllegalStateException> { SecurityScopedBookmark.create(path = directory.toString()) }
        }

        test("북마크가 아닌 값으로는 접근을 얻지 못한다") {
            shouldThrow<IllegalStateException> { SecurityScopedBookmark.startAccessing(bookmark = Arb.string().next()) }
        }
    })
