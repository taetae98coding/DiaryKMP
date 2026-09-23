@file:OptIn(ExperimentalWasmJsInterop::class)

package io.github.taetae98coding.diary.library.room3.driver

import androidx.sqlite.SQLiteDriver
import androidx.sqlite.driver.web.WebWorkerSQLiteDriver
import org.w3c.dom.Worker

public fun webWorkerSQLiteDriver(): SQLiteDriver = WebWorkerSQLiteDriver(createWorker())

private fun createWorker(): Worker = js("""new Worker(new URL("sqlite-wasm-worker/worker.js", import.meta.url))""")
