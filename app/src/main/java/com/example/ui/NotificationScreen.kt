package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.NotificationEntity
import com.example.viewmodel.FunnyDoseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: FunnyDoseViewModel) {
    val alerts by viewModel.allNotifications.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alerts Notification Center", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF07000B)),
                actions = {
                    TextButton(onClick = { /* Simulated mark all read */ }) {
                        Text("Mark Read", color = Color(0xFF25F4EE), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            )
        },
        containerColor = Color(0xFF0F0C20)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (alerts.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Chat, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "No alerts yet!", color = Color.White, fontSize = 15.sp)
                        Text(text = "Engagement events appear here in real time.", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            items(alerts) { alert ->
                NotificationRowItem(alert = alert)
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun NotificationRowItem(alert: NotificationEntity) {
    // Left avatar visual, choosing colors by type
    val iconBg = when (alert.type) {
        "LIKE" -> Color(0xFFFE2C55).copy(alpha = 0.15f)
        "COMMENT" -> Color(0xFF7209B7).copy(alpha = 0.15f)
        "FOLLOW" -> Color(0xFF25F4EE).copy(alpha = 0.15f)
        else -> Color(0xFFFFCC00).copy(alpha = 0.15f)
    }

    val iconTint = when (alert.type) {
        "LIKE" -> Color(0xFFFE2C55)
        "COMMENT" -> Color(0xFF7209B7)
        "FOLLOW" -> Color(0xFF25F4EE)
        else -> Color(0xFFFFCC00)
    }

    val iconVector = when (alert.type) {
        "LIKE" -> Icons.Default.Favorite
        "COMMENT" -> Icons.Default.ModeComment
        "FOLLOW" -> Icons.Default.PersonAdd
        else -> Icons.Default.Campaign
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_row_${alert.id}")
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive icon badge or actor avatar
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                if (alert.actorAvatar.isNotEmpty()) {
                    AsyncImage(
                        model = alert.actorAvatar,
                        contentDescription = "Notification Actor Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(imageVector = iconVector, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.actorName,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = alert.actorHandle,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = alert.message,
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Time indication
            Text(text = "1m ago", color = Color.DarkGray, fontSize = 10.sp)
        }
    }
}
