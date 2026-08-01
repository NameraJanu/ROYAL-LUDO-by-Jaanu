package com.example.ludo.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsKabaddi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HowToPlayDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Official Ludo Rules",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                RuleItem(
                    icon = Icons.Default.Casino,
                    title = "Leaving the Yard",
                    description = "Roll a 6 on the dice to move a token out of your home yard onto the starting square."
                )
                Spacer(modifier = Modifier.height(12.dp))
                RuleItem(
                    icon = Icons.Default.Casino,
                    title = "Extra Turns",
                    description = "Rolling a 6 grants an extra roll! Rolling 3 consecutive sixes cancels the turn."
                )
                Spacer(modifier = Modifier.height(12.dp))
                RuleItem(
                    icon = Icons.Default.SportsKabaddi,
                    title = "Capturing Opponents",
                    description = "Landing on an opponent's token sends it back to their yard and grants you an extra bonus roll!"
                )
                Spacer(modifier = Modifier.height(12.dp))
                RuleItem(
                    icon = Icons.Default.Shield,
                    title = "Safe Star Tiles",
                    description = "Tokens on Star tiles or Start squares are protected from captures."
                )
                Spacer(modifier = Modifier.height(12.dp))
                RuleItem(
                    icon = Icons.Default.Flag,
                    title = "Winning the Game",
                    description = "Navigate all 4 of your tokens around the board into the center goal to win!"
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got It!", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun RuleItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
