package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FunnyDoseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(viewModel: FunnyDoseViewModel) {
    val context = LocalContext.current

    var selectedType by remember { mutableStateOf("TEXT") } // TEXT | IMAGE | VIDEO
    var captionText by remember { mutableStateOf("") }
    var hashtagText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Desi") }
    var searchMemePrompt by remember { mutableStateOf("") }

    // Preset categories for dropdown
    val categories = listOf("Desi", "Office", "Cricket", "Bollywood", "School", "Gaming", "Love", "Anime")

    // Premade meme formats/illustrations for IMAGE type
    val memeTemplates = listOf(
        "https://images.unsplash.com/photo-1541562232579-512a21360020?w=300&auto=format&fit=crop", // Distracted boyfriend mock
        "https://images.unsplash.com/photo-1518020382113-a7e8fc38eac9?w=300&auto=format&fit=crop", // Drake happy dog look
        "https://images.unsplash.com/photo-1531538606174-0f90ff5dce83?w=300&auto=format&fit=crop", // Corporate board meeting hand
        "https://images.unsplash.com/photo-1579373903781-fd5c0c30c4cd?w=300&auto=format&fit=crop"  // Two buttons sweat mock
    )
    var selectedTemplateUrl by remember { mutableStateOf(memeTemplates[0]) }

    // Collect Gemini Suggestion States
    val aiSuggestion by viewModel.aiSuggestion.collectAsState()
    val aiLoading by viewModel.aiLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Publish Viral Dose 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF07000B)),
                actions = {
                    // Check button
                    IconButton(
                        onClick = {
                            val contentToSend = if (selectedType == "IMAGE") selectedTemplateUrl else captionText
                            viewModel.publishNewPost(
                                caption = captionText,
                                tags = hashtagText,
                                category = selectedCategory,
                                postType = selectedType
                            )
                            Toast.makeText(context, "Dose Published Live! 🪙 Recieved 15 Coins!", Toast.LENGTH_LONG).show()
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Publish", tint = Color(0xFF25F4EE), modifier = Modifier.size(28.dp))
                    }
                }
            )
        },
        containerColor = Color(0xFF0F0C20)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // Select Format row tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("TEXT" to "Short Text Joke", "IMAGE" to "Image Meme", "VIDEO" to "Video Reel").forEach { (typeKey, label) ->
                    val isSel = selectedType == typeKey
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color(0xFFFE2C55) else Color.Transparent)
                            .clickable { selectedType = typeKey }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (isSel) Color.White else Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Body inputs fields
            Text(text = "Dose Caption ideas & Funny description", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = captionText,
                onValueChange = { captionText = it },
                placeholder = { Text("Write something funny, or tap Gemini assistant to write it...", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFE2C55),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                minLines = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("caption_input_studio")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Categories list selection row
            Text(text = "Choose Tag Category", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.W600)
            Spacer(modifier = Modifier.height(6.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.height(84.dp)
            ) {
                items(categories) { cat ->
                    val isSel = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSel) Color(0xFF7209B7) else Color.White.copy(alpha = 0.05f))
                            .clickable { selectedCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = cat, color = if (isSel) Color.White else Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Hashtags input field
            OutlinedTextField(
                value = hashtagText,
                onValueChange = { hashtagText = it },
                placeholder = { Text("viral, cricket, btown (comma separated)", color = Color.Gray) },
                label = { Text("Hashtags", color = Color.LightGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFFE2C55),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Render conditional IMAGE formats picker
            AnimatedVisibility(visible = selectedType == "IMAGE") {
                Column {
                    Text(text = "Select Meme Format Template", color = Color.LightGray, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        memeTemplates.forEach { url ->
                            val isSel = selectedTemplateUrl == url
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                                    .clickable { selectedTemplateUrl = url }
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSel) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFFE2C55).copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // --- Server-Side Gemini Content Generation Assistant Card ---
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF7209B7).copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF25F4EE), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini Caption Suggester",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Sparkle prompt generator button
                        Button(
                            onClick = {
                                val searchTopic = if (captionText.isNotEmpty()) captionText else selectedCategory
                                viewModel.generateAIPostSuggestions(searchTopic)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFE2C55)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(28.dp).testTag("gemini_generate_btn")
                        ) {
                            if (aiLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                            } else {
                                Text("Ask Gemini ✨", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (aiSuggestion.isNotEmpty()) {
                        Text(
                            text = aiSuggestion,
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                                .fillMaxWidth()
                                .clickable {
                                    // Auto copy suggestions to draft caption
                                    val parted = aiSuggestion.split("#")
                                    captionText = parted.firstOrNull()?.trim() ?: aiSuggestion
                                    if (parted.size > 1) {
                                        hashtagText = parted.drop(1).joinToString(",") { it.trim() }
                                    }
                                    Toast.makeText(context, "Suggestion copied to editor draft!", Toast.LENGTH_SHORT).show()
                                }
                        )
                    } else {
                        Text(
                            text = "Draft a topic above, type what your joke is about, or choose a category and click Ask Gemini to generate trending hashtags & captions!",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
