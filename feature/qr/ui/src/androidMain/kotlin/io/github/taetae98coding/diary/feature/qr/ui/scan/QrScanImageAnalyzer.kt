@file:OptIn(ExperimentalGetImage::class)

package io.github.taetae98coding.diary.feature.qr.ui.scan

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executor

internal class QrScanImageAnalyzer(
    private val scanner: BarcodeScanner,
    private val resultExecutor: Executor,
    private val onDetect: (String) -> Unit,
) : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        val mediaImage = image.image
        if (mediaImage == null) {
            image.close()
            return
        }

        // 인식이 끝나기 전에 프레임을 닫으면 버퍼가 재사용되어 인식이 깨지므로 완료 뒤에 닫는다.
        scanner
            .process(InputImage.fromMediaImage(mediaImage, image.imageInfo.rotationDegrees))
            .addOnSuccessListener(resultExecutor) { barcodeList ->
                barcodeList.forEach { barcode -> onDetect(barcode.rawValue.orEmpty()) }
            }.addOnCompleteListener { image.close() }
    }
}
