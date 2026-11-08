package com.haorong.spendlog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1F4D3A)
private val Paper = Color(0xFFF4F0E6)

@Composable
fun SpendLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Green,
            onPrimary = Color.White,
            background = Paper,
            surface = Color.White,
        ),
        content = content,
    )
}
