package com.nopoint.midpoint.helpers

import android.content.Context
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.WriterException
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.EncodeHintType
import com.nopoint.midpoint.R


class QRCodeHelper private constructor(context: Context) {
    var errorCorrectionLevel: ErrorCorrectionLevel? = null
    var margin: Int = 0
    var content: String? = null
    var width: Int = 0
    var height: Int = 0

    val qrCode: Bitmap?
        get() = generate()

    init {
        width = 600
        height = 600
    }

    fun getQRCOde(): Bitmap? {
        return generate()
    }

    // Set correction level to QR code
    fun setErrorCorrenctionLevel(level: ErrorCorrectionLevel): QRCodeHelper {
        errorCorrectionLevel = level
        return this
    }

    fun setContent(content: String): QRCodeHelper {
        this.content = content
        return this
    }

    fun setMargin(margin: Int): QRCodeHelper {
        this.margin = margin
        return this
    }

    private fun generate(): Bitmap? {
        val hintMap = HashMap<EncodeHintType, Any>()

        hintMap[EncodeHintType.CHARACTER_SET] = "utf-8"
        hintMap[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel!!
        hintMap[EncodeHintType.MARGIN] = margin

        try {
            val bitMatrix = QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                width,
                height,
                hintMap)
            val pixels = IntArray(width * height)

            for (i in 0 until height) {
                for (j in 0 until width) {
                    if (bitMatrix.get(j, i)) {
                        pixels[i * width + j] = Color.BLACK
                    } else {
                        pixels[i * width + j] = Color.WHITE
                    }
                }
            }

            return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return null
    }

    companion object {


        fun newInstance(context: Context): QRCodeHelper {
            return QRCodeHelper(context)
        }
    }
}

