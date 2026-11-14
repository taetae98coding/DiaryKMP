@file:OptIn(ExperimentalComposeUiApi::class)

package io.github.taetae98coding.diary.compose.core.share

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard
import io.github.taetae98coding.diary.library.fixture.nonBlankString
import io.github.taetae98coding.diary.library.fixture.randomByteArray
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.next
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable

class JvmContentSharerTest :
    FunSpec({
        test("TC-b1822141-1c4e-474d-adb2-d1e47fff9352 데스크톱은 글만 복사로 제공한다") {
            val sharer = JvmContentSharer(clipboard = mockk())

            sharer.isTextSupported shouldBe true
            ShareFileType.entries.forEach { type -> sharer.isFileSupported(type = type) shouldBe false }
            sharer.isSupported(content = ShareContent.Text(text = Arb.nonBlankString().next())) shouldBe true
        }

        test("TC-20ffa175-8b1c-4c31-8baf-df1271a3286c 데스크톱 글 공유는 제목 없이 복사한다") {
            val clipboard = mockk<Clipboard>()
            val entry = slot<ClipEntry>()
            coEvery { clipboard.setClipEntry(capture(entry)) } just runs
            val content = ShareContent.Text(text = Arb.nonBlankString().next(), title = Arb.nonBlankString().next())

            JvmContentSharer(clipboard = clipboard).share(content = content) shouldBe ShareResult.COPIED

            (entry.captured.nativeClipEntry as Transferable).getTransferData(DataFlavor.stringFlavor) shouldBe content.text
        }

        test("TC-bf69ce8b-10ed-455b-8c52-9f2e2e5f115f 데스크톱 복사에 실패하면 공유하지 못한다") {
            val clipboard = mockk<Clipboard>()
            coEvery { clipboard.setClipEntry(any()) } throws IllegalStateException(Arb.nonBlankString().next())

            JvmContentSharer(clipboard = clipboard).share(content = ShareContent.Text(text = Arb.nonBlankString().next())) shouldBe ShareResult.FAILED
        }

        test("데스크톱에서 파일 공유를 실행하면 클립보드에 쓰지 않고 공유하지 못한 것으로 끝난다") {
            val clipboard = mockk<Clipboard>()
            val file = ShareContent.File(bytes = Arb.randomByteArray().next(), name = Arb.nonBlankString().next(), type = Arb.enum<ShareFileType>().next())

            JvmContentSharer(clipboard = clipboard).share(content = file) shouldBe ShareResult.FAILED

            coVerify(exactly = 0) { clipboard.setClipEntry(any()) }
        }
    })
