package io.github.taetae98coding.diary.share

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import io.github.taetae98coding.diary.DiaryActivity
import io.github.taetae98coding.diary.R
import io.github.taetae98coding.diary.app.shared.share.ShareStreamReader
import io.github.taetae98coding.diary.app.shared.share.shareAddDeepLinkOf
import io.github.taetae98coding.diary.domain.share.ShareFile
import io.github.taetae98coding.diary.domain.share.ShareReadResult
import io.github.taetae98coding.diary.domain.share.ShareRequest
import io.github.taetae98coding.diary.domain.share.read
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

// 공유하던 앱의 작업에서 실행되므로 앱 작업을 앞으로 가져오는 일은 DiaryActivity를 새 작업 플래그로 열어 시스템에 맡긴다.
internal class ShareActivity : ComponentActivity() {
    private val streamReader: ShareStreamReader by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action != Intent.ACTION_SEND) {
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                handle(result = readShare())
            } finally {
                finish()
            }
        }
    }

    private suspend fun readShare(): ShareReadResult {
        val stream = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        if (stream != null && !streamReader.isReadable(stream)) return ShareReadResult.Unreadable

        val file =
            if (stream == null) {
                ShareFile.None
            } else {
                ShareFile.Attached(mimeType = streamReader.mimeType(stream).ifEmpty { intent.type.orEmpty() })
            }
        val request =
            ShareRequest(
                text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString().orEmpty(),
                subject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)?.toString().orEmpty(),
                file = file,
            )

        return request.read(readFileText = { stream?.let { uri -> streamReader.readText(uri) }.orEmpty() })
    }

    private fun handle(result: ShareReadResult) {
        when (result) {
            is ShareReadResult.Read -> openDiary(deepLink = shareAddDeepLinkOf(content = result.content))
            ShareReadResult.TooLong -> Toast.makeText(applicationContext, R.string.share_too_long, Toast.LENGTH_SHORT).show()
            ShareReadResult.Unreadable -> Unit
        }
    }

    // DiaryActivity는 singleTop이라 앱 작업의 맨 위에 있으면 onNewIntent로 주소를 받고, 앱 작업이 없으면 새 작업의 시작 화면으로 받는다.
    private fun openDiary(deepLink: String) {
        val intent =
            Intent(this, DiaryActivity::class.java)
                .setAction(Intent.ACTION_VIEW)
                .setData(deepLink.toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        startActivity(intent)
    }
}
