package com.haorong.spendlog

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF1F4D3A)
private val GreenDark = Color(0xFF16382A)
private val Clay = Color(0xFFC46B3A)
private val Paper = Color(0xFFF6F1E7)
private val Card = Color(0xFFFFFCF7)
private val Ink = Color(0xFF1C1A16)
private val Muted = Color(0xFF6E675C)

private val SpendColors = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD7E6DC),
    onPrimaryContainer = GreenDark,
    secondary = Clay,
    onSecondary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Card,
    onSurface = Ink,
    onSurfaceVariant = Muted,
    outline = Color(0xFFD9D0C3),
    error = Color(0xFF9C2F2F),
)

@Composable
fun SpendLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SpendColors,
        content = content,
    )
}
