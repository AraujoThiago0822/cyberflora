package com.cyberflora.frontend.screens

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Mostra "15092026" como "15/09/2026". O valor guardado continua só com dígitos. */
class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val out = StringBuilder()
        for (i in digits.indices) {
            out.append(digits[i])
            if ((i == 1 || i == 3) && i != digits.lastIndex) out.append('/')
        }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int = when {
                offset <= 1 -> offset
                offset <= 3 -> offset + 1
                else -> offset + 2
            }.coerceAtMost(out.length)

            override fun transformedToOriginal(offset: Int): Int = when {
                offset <= 2 -> offset
                offset <= 5 -> offset - 1
                else -> offset - 2
            }.coerceAtMost(digits.length)
        }
        return TransformedText(AnnotatedString(out.toString()), mapping)
    }
}