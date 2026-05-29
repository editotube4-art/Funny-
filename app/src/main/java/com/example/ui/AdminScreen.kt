package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.FunnyDoseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(viewModel: FunnyDoseViewModel) {
    val context = LocalContext.current

    val adminSettings by viewModel.adminSettings.collectAsState()
    val adSales by viewModel.adRevenue.collectAsState()
    val totalDoses by viewModel.allPosts.collectAsState()

    // Form inputs
    var banTargetHandle by remember { mutableStateOf("") }
    var delTargetPostId by remember { mutableStateOf("") }
    var alertBroadTitle by remember { mutableStateOf("") }
    var alertBroadMessage by remember { mutableStateOf("") }

    // Read toggles with fallbacks
    val isNsfwEnabled = adminSettings["nsfw_filter_enabled"]?.toBoolean() ?: true
    val isUploadsActive = adminSettings["uploads_enabled"]?.toBoolean() ?: true
    val activeAdNetworks = adminSettings["ads_networks_active"] ?: "AdMob, Unity Ads"
    val adFrequency = adminSettings["frequency_ad_intervals"] ?: "3"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Panel 🛡️", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF07000B))
            )
        },
        containerColor = Color(0xFF0F0C20)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Social Platform Analytics",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // High Fidelity Dashboard metrics cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        title = "Estimated Rewards Pool",
                        value = "$${String.format("%.2f", adSales)}",
                        icon = Icons.Default.MonetizationOn,
                        color = Color(0xFF25F4EE), // Cyan
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        title = "Total Active Doses",
                        value = "${totalDoses.size + 1400}", // simulated total scale
                        icon = Icons.Default.TrendingUp,
                        color = Color(0xFFFE2C55), // Pink
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminMetricCard(
                        title = "Viral Video Creators",
                        value = "1,894",
                        icon = Icons.Default.People,
                        color = Color(0xFF7209B7), // Purple
                        modifier = Modifier.weight(1f)
                    )

                    AdminMetricCard(
                        title = "CDN Cache Hit",
                        value = "99.4%",
                        icon = Icons.Default.Dns,
                        color = Color(0xFFFFB703), // Orange
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Monetization ad configuration rows
            item {
                Text(
                    text = "Ad Placements & Monetization Controls",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Enabled Networks", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(activeAdNetworks, color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    val nextAdNetwork = if (activeAdNetworks.contains("Monetag")) "AdMob, Unity Ads" else "AdMob, Unity Ads, Monetag, Adsterra"
                                    viewModel.updateAdminToggles("ads_networks_active", nextAdNetwork)
                                    Toast.makeText(context, "SDK Networks Updated Live!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Toggle SDKs", fontSize = 11.sp, color = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Ad Load Frequency", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Show banner ad every $adFrequency feed views", color = Color.Gray, fontSize = 11.sp)
                            }
                            Button(
                                onClick = {
                                    val nextFreq = if (adFrequency == "3") "5" else "3"
                                    viewModel.updateAdminToggles("frequency_ad_intervals", nextFreq)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Intervals config", fontSize = 11.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Safety and Censorship controls
            item {
                Text(
                    text = "Safety & Censorship Controls",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // NSFW filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Active NSFW Smart Filter", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Enable to auto blur/delete crude material", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isNsfwEnabled,
                                onCheckedChange = { viewModel.updateAdminToggles("nsfw_filter_enabled", it.toString()) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFE2C55))
                            )
                        }

                        // Enable Uploads block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Accept New Creator Videos", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Toggle off to pause scaling upload data rates", color = Color.Gray, fontSize = 11.sp)
                            }
                            Switch(
                                checked = isUploadsActive,
                                onCheckedChange = { viewModel.updateAdminToggles("uploads_enabled", it.toString()) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFFE2C55))
                            )
                        }
                    }
                }
            }

            // Shadowban profiles input form
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Censor User / Post ID", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = banTargetHandle,
                            onValueChange = { banTargetHandle = it },
                            placeholder = { Text("Enter @handle to shadowban", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFE2C55),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        Button(
                            onClick = {
                                if (banTargetHandle.isNotEmpty()) {
                                    viewModel.executeAdminBan(banTargetHandle)
                                    Toast.makeText(context, "$banTargetHandle has been shadow-banned!", Toast.LENGTH_SHORT).show()
                                    banTargetHandle = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55)),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("Execute Shadowban", fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = delTargetPostId,
                            onValueChange = { delTargetPostId = it },
                            placeholder = { Text("Enter post ID to remove immediately", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFFFE2C55),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        Button(
                            onClick = {
                                if (delTargetPostId.isNotEmpty()) {
                                    viewModel.executeAdminPostDelete(delTargetPostId)
                                    Toast.makeText(context, "Post securely deleted!", Toast.LENGTH_SHORT).show()
                                    delTargetPostId = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7209B7)),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("Delete Post Entry", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Real Push Alerts Notification Sender
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7209B7).copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "Broadcaster: Real Push Alert", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = alertBroadTitle,
                            onValueChange = { alertBroadTitle = it },
                            placeholder = { Text("Push alert title", color = Color.Gray, fontSize = 12.sp) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF25F4EE),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        )

                        OutlinedTextField(
                            value = alertBroadMessage,
                            onValueChange = { alertBroadMessage = it },
                            placeholder = { Text("Push alert body content message...", color = Color.Gray, fontSize = 12.sp) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF25F4EE),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (alertBroadTitle.isNotEmpty() && alertBroadMessage.isNotEmpty()) {
                                    viewModel.sendAdminBroadCastAlert(alertBroadTitle, alertBroadMessage)
                                    Toast.makeText(context, "System alerts broadcasted live!", Toast.LENGTH_LONG).show()
                                    alertBroadTitle = ""
                                    alertBroadMessage = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25F4EE)),
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text("Broadcast Push Notification", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: Any,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // icon
                Icon(
                    imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, color = Color.Gray, fontSize = 11.sp)
        }
    }
}
