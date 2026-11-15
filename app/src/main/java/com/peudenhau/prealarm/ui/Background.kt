package com.peudenhau.prealarm.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.peudenhau.prealarm.R

@Composable
fun CuteBackground(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.bg_cute),
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop,
        alignment = Alignment.BottomCenter,
        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
    )
}
