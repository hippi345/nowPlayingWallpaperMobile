package com.hippi345.nowplayingwallpaper.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.hippi345.nowplayingwallpaper.domain.WallpaperLayout

@Composable
fun WallpaperPreviewCard(
    layout: WallpaperLayout,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 4.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
        ) {
            if (layout.albumArtUrl != null) {
                AsyncImage(
                    model = layout.albumArtUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF121212)),
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xE6000000)),
                            startY = 200f,
                        ),
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = layout.headline,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = layout.subline,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFE0E0E0),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
