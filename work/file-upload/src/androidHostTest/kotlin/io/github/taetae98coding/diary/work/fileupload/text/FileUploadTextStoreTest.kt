package io.github.taetae98coding.diary.work.fileupload.text

import android.content.Context
import com.navercorp.fixturemonkey.FixtureMonkey
import com.navercorp.fixturemonkey.kotlin.giveMeOne
import io.github.taetae98coding.diary.library.fixturemonkey.diaryFixtureMonkey
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.io.FileNotFoundException

private val fixtureMonkey: FixtureMonkey = diaryFixtureMonkey()

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class FileUploadTextStoreTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
    }

    @Test
    fun `TC-FILE-STORAGE-DATA-028 적어 둔 제목과 설명을 줄바꿈과 앞뒤 공백까지 그대로 다시 읽는다`() =
        runTest {
            val store = FileUploadTextStore(context = context, dispatcher = StandardTestDispatcher(testScheduler))
            val textList =
                listOf(
                    FileUploadText(title = " ${fixtureMonkey.giveMeOne<String>()} ", description = "첫 줄\n둘째 줄\r\n"),
                    FileUploadText(title = "제목", description = ""),
                    // 64KB를 넘는 문자열도 담을 수 있어야 한다.
                    FileUploadText(title = "가".repeat(30_000), description = "나".repeat(100_000)),
                )

            textList.forEach { text ->
                val id = store.write(text = text)

                store.read(id = id) shouldBe text
            }
        }

    @Test
    fun `지운 제목과 설명은 다시 읽을 수 없다`() =
        runTest {
            val store = FileUploadTextStore(context = context, dispatcher = StandardTestDispatcher(testScheduler))
            val id = store.write(text = FileUploadText(title = "제목", description = fixtureMonkey.giveMeOne<String>()))

            store.delete(id = id)

            shouldThrow<FileNotFoundException> { store.read(id = id) }
        }
}
