package com.example.ludo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ludo.model.BoardTheme
import com.example.ludo.model.Player

@Composable
fun PlayerCard(
    player: Player,
    isActive: Boolean,
    theme: BoardTheme = BoardTheme.ROYAL,
    modifier: Modifier = Modifier
) {
    val playerThemeColor = player.color.getThemeColor(theme)

    val borderColor by animateColorAsState(
        targetValue = if (isActive) Color(0xFFFFD700) else Color(0xFF3B354D),
        label = "playerBorder"
    )

    val bgColor = if (isActive) Color(0xFF2E283E) else Color(0xFF1E1A29)

    val infiniteTransition = rememberInfiniteTransition(label = "activeCardPulse")
    val activeDotScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "activeDotScale"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.clip(RoundedCornerShape(14.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(playerThemeColor)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .scale(activeDotScale)
                                .clip(CircleShape)
                                .background(Color(0xFFFFD700))
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = player.name,
                            fontSize = 13.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isActive) Color(0xFFFFD700) else Color.White
                        )
                        if (player.isBot) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = "Bot",
                                tint = Color(0xFFAEAAAE),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "Home: ${player.tokensHome}/4 • Yard: ${player.tokensInYard}",
                        fontSize = 11.sp,
                        color = Color(0xFFAEAAAE)
                    )
                }
            }
        }
    }
}
