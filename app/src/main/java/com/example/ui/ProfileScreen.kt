package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PostEntity
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FunnyDoseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: FunnyDoseViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val posts by viewModel.allPosts.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Filter posts from creator
    val creatorPosts = remember(posts, currentUser) {
        if (currentUser == null) emptyList() else posts.filter { it.authorHandle == currentUser!!.handle }
    }

    if (currentUser == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0C20)),
            contentAlignment = Alignment.Center
        ) {
            Button(onClick = { viewModel.navigateTo(AppScreen.AUTH) }) {
                Text("Login to view profile")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentUser!!.handle, color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF07000B)),
                actions = {
                    // Logout button
                    IconButton(onClick = { viewModel.logOutCurrent() }) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout", tint = Color(0xFFFE2C55))
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
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Main user info space
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile avatar image frame
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        AsyncImage(
                            model = currentUser!!.avatarUrl,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Creator Display Name and Verified status badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentUser!!.username,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentUser!!.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Creator status",
                                tint = Color(0xFF25F4EE), // Cyan badge
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = currentUser!!.handle,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Profile User statistics row counts links
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ProfileStatItem(count = "${currentUser!!.followersCount}", label = "Followers")
                        ProfileStatItem(count = "${currentUser!!.followingCount}", label = "Following")
                        ProfileStatItem(count = "${currentUser!!.totalLikes}", label = "Total Likes")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description Bio text
                    Text(
                        text = currentUser!!.bio,
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Profile action button rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("edit_profile_action_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Biography", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.05f))
            }

            // Glimpse creator grid of posts
            item {
                Text(
                    text = "My Published Doses (${creatorPosts.size})",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                if (creatorPosts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.PostAdd, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "You haven't uploaded any memes yet!", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            // List of creator posts
            items(creatorPosts) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Visual thumbnail
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(getGradientBrushByCode(post.contentUrl)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.ContentCut, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = post.caption, color = Color.White, fontSize = 13.sp, maxLines = 1)
                            Text(text = "Likes: ${post.likesCount} | Comments: ${post.commentsCount}", color = Color.Gray, fontSize = 10.sp)
                        }

                        IconButton(onClick = { viewModel.executeAdminPostDelete(post.id) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                        }
                    }
                }
            }
        }

        // Edit Profile Dialog Popup
        if (showEditProfileDialog) {
            EditProfileDialog(
                currentUser = currentUser!!,
                onDismiss = { showEditProfileDialog = false },
                onSave = { name, bio ->
                    viewModel.updateProfile(name, bio)
                    showEditProfileDialog = false
                }
            )
        }
    }
}

@Composable
fun ProfileStatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 11.sp
        )
    }
}

// Dialog Layout
@Composable
fun EditProfileDialog(
    currentUser: com.example.data.UserEntity,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var dName by remember { mutableStateOf(currentUser.username) }
    var dBio by remember { mutableStateOf(currentUser.bio) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Update Public Biography", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dName,
                    onValueChange = { dName = it },
                    label = { Text("Display Name") }
                )
                OutlinedTextField(
                    value = dBio,
                    onValueChange = { dBio = it },
                    label = { Text("Short Bio") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(dName, dBio) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55))
            ) {
                Text("Save Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}
