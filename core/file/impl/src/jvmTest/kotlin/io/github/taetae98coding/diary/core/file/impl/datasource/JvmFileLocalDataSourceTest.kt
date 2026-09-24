package io.github.taetae98coding.diary.core.file.impl.datasource

import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.core.model.file.FileUri
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrowAny
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import java.io.File
import java.net.URI

private val fixtureMonkey: FixtureMonkey =
    diaryFixtureMonkey()

class JvmFileLocalDataSourceTest :
    FunSpec({
        test("파일 크기를 바이트 수로 알린다") {
            val bytes = fileBytes()
            val dataSource = JvmFileLocalDataSource(dispatcher = Dispatchers.Default)

            dataSource.size(uri = fileUri(bytes = bytes)) shouldBe bytes.size.toLong()
        }

        test("파일 내용을 열어 읽으면 쓴 내용과 같다") {
            val bytes = fileBytes()
            val dataSource = JvmFileLocalDataSource(dispatcher = Dispatchers.Default)

            val read = dataSource.openSource(uri = fileUri(bytes = bytes)).buffered().use { source -> source.readByteArray() }

            read shouldBe bytes
        }

        test("지운 파일은 더 읽을 수 없다") {
            val uri = fileUri(bytes = fileBytes())
            val dataSource = JvmFileLocalDataSource(dispatcher = Dispatchers.Default)

            dataSource.delete(uri = uri)

            File(URI(uri.value)).exists() shouldBe false
            shouldThrowAny { dataSource.openSource(uri = uri) }
        }

        test("없는 파일을 지워도 실패하지 않는다") {
            val uri = fileUri(bytes = fileBytes())
            val dataSource = JvmFileLocalDataSource(dispatcher = Dispatchers.Default)

            dataSource.delete(uri = uri)
            dataSource.delete(uri = uri)
        }
    }) {
    public companion object {
        private fun fileBytes(): ByteArray = "file-${fixtureMonkey.giveMeOne<String>()}".encodeToByteArray()

        private fun fileUri(bytes: ByteArray): FileUri {
            val file = File.createTempFile("file-local-data-source-test", null)

            file.deleteOnExit()
            file.writeBytes(bytes)

            return FileUri(file.toURI().toString())
        }
    }
}
