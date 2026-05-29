package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PostEntity
import com.example.viewmodel.FeedTab
import com.example.viewmodel.FunnyDoseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(viewModel: FunnyDoseViewModel) {
    val currentTab by viewModel.currentFeedTab.collectAsState()
    val posts by viewModel.allPosts.collectAsState()
    val activeCategory by viewModel.activeCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val userCoins by viewModel.userCoins.collectAsState()
    val dailyStreak by viewModel.dailyStreak.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Interactive Dialogs
    var showShareSheet by remember { mutableStateOf<PostEntity?>(null) }
    val selectedPostForComments by viewModel.activePostForComments.collectAsState()

    // Categories List
    val categories = listOf("Trending", "Bollywood", "Cricket", "Desi", "Office", "School", "Gaming", "Love", "Anime")

    // Filter Posts
    val filteredPosts = remember(posts, currentTab, activeCategory, searchQuery) {
        posts.filter { post ->
            // Search filter
            val matchesSearch = if (searchQuery.isNotEmpty()) {
                post.caption.contains(searchQuery, ignoreCase = true) ||
                        post.hashtags.contains(searchQuery, ignoreCase = true) ||
                        post.authorHandle.contains(searchQuery, ignoreCase = true)
            } else {
                true
            }

            // Tab filter & Category filter
            val matchesTab = when (currentTab) {
                FeedTab.FOLLOWING -> post.authorHandle == "@desi_guru" || post.authorHandle == "@office_comedy"
                FeedTab.EXPLORE -> {
                    if (activeCategory == "Trending") true else post.category.equals(activeCategory, ignoreCase = true)
                }
                FeedTab.REELS -> post.type == "VIDEO"
            }

            matchesSearch && matchesTab
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .background(Color(0xFF07000B))
                    .padding(top = 8.dp)
            ) {
                // Header Brand Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FunnyDose 😂",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Streak coins count
                    Row(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                            .clickable {
                                viewModel.addDailyCoinsReward()
                                Toast.makeText(context, "Streak Rewarded! +50 Coins 🌟", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Daily Streak reward",
                            tint = Color(0xFFFFB703),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${dailyStreak}D Streak",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🪙 $userCoins",
                            color = Color(0xFF25F4EE), // NeonCyan
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Main feed/explore/reels tab row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    val activeBrush = Brush.horizontalGradient(listOf(Color(0xFFFE2C55), Color(0xFF7209B7)))

                    listOf(
                        FeedTab.FOLLOWING to "Following",
                        FeedTab.EXPLORE to "Explore",
                        FeedTab.REELS to "Reels 🎬"
                    ).forEach { (tab, label) ->
                        val isSelected = currentTab == tab
                        Box(
                            modifier = Modifier
                                .clickable { viewModel.selectFeedTab(tab) }
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.Gray
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .offset(y = 10.dp)
                                        .width(36.dp)
                                        .height(3.dp)
                                        .background(activeBrush, RoundedCornerShape(2.dp))
                                )
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                // Optional Horizontal category chips if Explore is selected
                if (currentTab == FeedTab.EXPLORE) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { category ->
                            val isSelected = activeCategory == category
                            val textCol = if (isSelected) Color.White else Color.LightGray
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFE2C55) else Color.White.copy(
                                            alpha = 0.08f
                                        )
                                    )
                                    .clickable { viewModel.selectCategory(category) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = category,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W600,
                                    color = textCol
                                )
                            }
                        }
                    }
                }

                // Global search bar input panel
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search topics, tags, creators...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.04f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedBorderColor = Color(0xFF25F4EE).copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .height(48.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))
            }
        },
        containerColor = Color(0xFF0F0C20)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (filteredPosts.isEmpty()) {
                // Empty state page
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.SentimentVeryDissatisfied,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No viral doses here!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Try switching tags or write a funny joke to publish code details yourself!",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            } else if (currentTab == FeedTab.REELS) {
                // Special TikTok-like Fullscreen Reels Feed (Swipes / Scrolling)
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberLazyListState()
                ) {
                    items(filteredPosts) { post ->
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .background(Color.Black)
                        ) {
                            ReelsCardItem(
                                postValue = post,
                                viewModel = viewModel,
                                onShareClicked = { showShareSheet = it },
                                onCommentsToggle = { viewModel.openCommentsForPost(it) }
                            )
                        }
                    }
                }
            } else {
                // Classic Instagram/Reels Grid / List Home Feed
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    items(filteredPosts) { post ->
                        StandardMemePostCard(
                            post = post,
                            viewModel = viewModel,
                            onShareClicked = { showShareSheet = it },
                            onCommentsToggle = { viewModel.openCommentsForPost(it) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }

            // --- Realtime Comments Bottom Sheet Dialog ---
            if (selectedPostForComments != null) {
                CommentsDrawerSheet(
                    activePost = selectedPostForComments!!,
                    viewModel = viewModel,
                    onClose = { viewModel.closeComments() }
                )
            }

            // --- Share Drawer Sheet Dialog ---
            if (showShareSheet != null) {
                ShareActionsSheet(
                    post = showShareSheet!!,
                    onDismiss = { showShareSheet = null }
                )
            }
        }
    }
}

// --- Standard post Feed Card ---
@Composable
fun StandardMemePostCard(
    post: PostEntity,
    viewModel: FunnyDoseViewModel,
    onShareClicked: (PostEntity) -> Unit,
    onCommentsToggle: (PostEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var likedPopState by remember { mutableStateOf(false) }

    // Remote visual mapping
    val visualBrush = getGradientBrushByCode(post.contentUrl)

    LaunchedEffect(post.id) {
        viewModel.checkAndAddAdRevenueOnView()
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B2A)),
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        viewModel.triggerDoubleTapLike(post.id)
                        likedPopState = true
                        coroutineScope.launch {
                            delay(600)
                            likedPopState = false
                        }
                    }
                )
            }
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Creator profile details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar image
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    if (post.authorAvatar.isNotEmpty()) {
                        AsyncImage(
                            model = post.authorAvatar,
                            contentDescription = "avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (post.isVerifiedAuthor) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "verified",
                                tint = Color(0xFF25F4EE), // Cyan badge
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(text = post.authorHandle, color = Color.Gray, fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.weight(1f))

                // Time notation
                Text(text = "Just Now", color = Color.DarkGray, fontSize = 10.sp)
            }

            // Meme/Text Joke Body space with active Double tap like floaters
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .then(
                        if (post.type == "IMAGE" && post.contentUrl.startsWith("http")) {
                            Modifier
                        } else {
                            Modifier.background(visualBrush)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (post.type == "IMAGE" && post.contentUrl.startsWith("http")) {
                    AsyncImage(
                        model = post.contentUrl,
                        contentDescription = "meme content",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (post.type == "VIDEO") {
                    // Simulated Interactive Wave Loop using standard Android canvas drawing!
                    // This creates a magnificent high fidelity looping visual representation of a "video reel" without calling Android MediaPlayer codec!
                    SimulatedVideoAudioWaveCanvas()
                } else {
                    // TEXT Post joke
                    Text(
                        text = post.caption,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(20.dp)
                    )
                }

                // Heart Floater on Double Tap
                androidx.compose.animation.AnimatedVisibility(
                    visible = likedPopState,
                    enter = fadeIn(animationSpec = tween(150)) + scaleIn(),
                    exit = fadeOut(animationSpec = tween(400)) + scaleOut()
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFE2C55),
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle Caption & Hashtags mapping for Image/Video
            if (post.type != "TEXT") {
                Text(
                    text = post.caption,
                    color = Color.White,
                    fontSize = 13.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            // Hashtags line layout
            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.split(",").joinToString(" ") { "#$it" },
                    color = Color(0xFF25F4EE), // Cyber Cyan
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Divider(color = Color.White.copy(alpha = 0.05f))

            // Action engagement row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Like Action (Heart pop)
                    IconButton(onClick = { viewModel.likePostToggle(post.id) }) {
                        Icon(
                            imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like Button",
                            tint = if (post.isLiked) Color(0xFFFE2C55) else Color.LightGray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "${post.likesCount}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Comment Action
                    IconButton(onClick = { onCommentsToggle(post) }) {
                        Icon(
                            imageVector = Icons.Outlined.ModeComment,
                            contentDescription = "Comments",
                            tint = Color.LightGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Text(
                        text = "${post.commentsCount}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Share Action
                    IconButton(onClick = { onShareClicked(post) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.LightGray,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Bookmark Icon Accent
                IconButton(onClick = { viewModel.bookmarkPostToggle(post.id) }) {
                    Icon(
                        imageVector = if (post.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save",
                        tint = if (post.isBookmarked) Color(0xFF25F4EE) else Color.LightGray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

// --- TikTok vertical full Reels layout item ---
@Composable
fun ReelsCardItem(
    postValue: PostEntity,
    viewModel: FunnyDoseViewModel,
    onShareClicked: (PostEntity) -> Unit,
    onCommentsToggle: (PostEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isLikedAnim by remember { mutableStateOf(false) }

    LaunchedEffect(postValue.id) {
        viewModel.checkAndAddAdRevenueOnView()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Full background gradients (Reels play simulation)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(getGradientBrushByCode(postValue.contentUrl))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            viewModel.triggerDoubleTapLike(postValue.id)
                            isLikedAnim = true
                            coroutineScope.launch {
                                delay(600)
                                isLikedAnim = false
                            }
                        }
                    )
                }
        ) {
            // Rotating neon canvas
            SimulatedVideoAudioWaveCanvas()

            // Translucent top & bottom gradients for overlay texts
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.TopCenter)
                    .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .align(Alignment.BottomCenter)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
            )

            // Fading Heart animation overlay on double tap
            AnimatedVisibility(
                visible = isLikedAnim,
                modifier = Modifier.align(Alignment.Center),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFFE2C55),
                    modifier = Modifier.size(110.dp)
                )
            }

            // Left Side: Creator Bio metadata panel details
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(18.dp)
                    .fillMaxWidth(0.75f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = postValue.authorHandle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (postValue.isVerifiedAuthor) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "verified",
                            tint = Color(0xFF25F4EE), // Cyan badge
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = postValue.caption,
                    color = Color.White,
                    fontSize = 14.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = postValue.hashtags.split(",").joinToString(" ") { "#$it" },
                    color = Color(0xFF25F4EE),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Original Sound - ${postValue.authorName} ✨",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            // Right Side: Floating Reels control tray icons (Double tap, Likes count, Comments Bubble, Whatsapp share)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Creator Profile Bubble with Plus sign
                Box(contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        AsyncImage(
                            model = postValue.authorAvatar,
                            contentDescription = "Avatar item",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .offset(y = 6.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFE2C55)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Liking bubble icon layout
                IconButton(onClick = { viewModel.likePostToggle(postValue.id) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = if (postValue.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "like reels",
                            tint = if (postValue.isLiked) Color(0xFFFE2C55) else Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
                Text(text = "${postValue.likesCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Comments Bubble icon layout
                IconButton(onClick = { onCommentsToggle(postValue) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ModeComment,
                            contentDescription = "comments count",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Text(text = "${postValue.commentsCount}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Copy Share Bubble icon layout
                IconButton(onClick = { onShareClicked(postValue) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "share to",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Text(text = "Share", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)

                // Spinning Vinyl CD disk simulation visualizer
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Black)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFFFE2C55))
                        )
                    }
                }
            }
        }
    }
}

// --- Dynamic Pulsating Canvas loops for Premium Video experience in Emulator ---
@Composable
fun SimulatedVideoAudioWaveCanvas() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val scaleFactor by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = SineIntensityEasing(0.4f)),
            repeatMode = RepeatMode.Reverse
        )
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val colorBrushes = Brush.linearGradient(
            colors = listOf(Color(0xFFFE2C55), Color(0xFF25F4EE), Color(0xFF7209B7))
        )

        // Draw animated concentric sound rings
        for (i in 1..4) {
            val baseRadius = (size.width.coerceAtMost(size.height) * 0.4f) * (i * 0.25f)
            val dynamicRadius = baseRadius * scaleFactor

            drawCircle(
                brush = colorBrushes,
                radius = dynamicRadius,
                center = centerOffset,
                style = Stroke(width = 3f + i, miter = 1f),
                alpha = 0.7f - (i * 0.15f)
            )
        }

        // Animated neon star vectors rotating inside in a safe layout
        val sizeVal = size * 0.6f * scaleFactor
        val topLeftOffset = Offset(
            (size.width - sizeVal.width) / 2f,
            (size.height - sizeVal.height) / 2f
        )

        drawArc(
            brush = colorBrushes,
            startAngle = rotationAngle,
            sweepAngle = 180f,
            useCenter = false,
            style = Stroke(width = 4f),
            size = sizeVal,
            topLeft = topLeftOffset
        )
    }
}

// Sine Easing class for canvas speed controls
class SineIntensityEasing(private val factor: Float) : Easing {
    override fun transform(fraction: Float): Float {
        return sin(fraction * Math.PI.toFloat()) * factor + fraction * (1 - factor)
    }
}

// Helper to provide nice gradients by index
fun getGradientBrushByCode(code: String): Brush {
    return when (code) {
        "LGR_01" -> Brush.verticalGradient(listOf(Color(0xFF1E350E), Color(0xFF0F1E06))) // Green sports Vibe
        "LGR_02" -> Brush.verticalGradient(listOf(Color(0xFFFE2C55).copy(alpha = 0.6f), Color(0xFF1D032A))) // Hot pink work
        "LGR_03" -> Brush.verticalGradient(listOf(Color(0xFF1B2A4A), Color(0xFF0C0F1A))) // Corporate office
        "LGR_04" -> Brush.verticalGradient(listOf(Color(0xFF7209B7).copy(alpha = 0.8f), Color(0xFF020005))) // Gaming neon
        else -> Brush.verticalGradient(listOf(Color(0xFF07000B), Color(0xFF1D1B2A))) // Slate background
    }
}

// --- Dynamic comment component drawers ---
@Composable
fun CommentsDrawerSheet(
    activePost: PostEntity,
    viewModel: FunnyDoseViewModel,
    onClose: () -> Unit
) {
    val comments by viewModel.currentComments.collectAsState()
    var commentText by remember { mutableStateOf("") }
    val keyboardScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClose() }
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF15141F)),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f)
                .align(Alignment.BottomCenter)
                .clickable(enabled = false, onClick = {}) // block clicking background dismiss
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with counts and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Responses (${comments.size})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { onClose() }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Scroll comments lists
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No comments on this dose yet.", color = Color.Gray, fontSize = 12.sp)
                                Text("Be the first to double tap and leave a response!", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                    items(comments) { comment ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Mini Avatar representation
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (comment.authorAvatar.isNotEmpty()) {
                                    AsyncImage(
                                        model = comment.authorAvatar,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = comment.authorName, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = comment.authorHandle, color = Color.Gray, fontSize = 10.sp)
                                }
                                Text(
                                    text = comment.text,
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 12.dp))

                // Comment input box layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add funny feedback...", color = Color.Gray, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFE2C55),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_field_input")
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            viewModel.submitPostComment(commentText)
                            commentText = ""
                        },
                        modifier = Modifier
                            .background(Color(0xFFFE2C55), CircleShape)
                            .size(44.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send Comment", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

// --- Elegant Share Tray drawer dialog ---
@Composable
fun ShareActionsSheet(
    post: PostEntity,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1C2B)),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = false, onClick = {}) // block dismiss
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Share Viral Dose 😂",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Share icons Row selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ShareOptionItem(
                        icon = Icons.Default.Chat,
                        label = "WhatsApp",
                        color = Color(0xFF25D366),
                        onClick = {
                            Toast.makeText(context, "Meme shared to WhatsApp group! 📲", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    ShareOptionItem(
                        icon = Icons.Default.CameraAlt,
                        label = "Instagram",
                        color = Color(0xFFE1306C),
                        onClick = {
                            Toast.makeText(context, "Added to Reels Story! 📸", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )

                    ShareOptionItem(
                        icon = Icons.Default.Link,
                        label = "Copy Link",
                        color = Color(0xFF25F4EE),
                        onClick = {
                            Toast.makeText(context, "Post link copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                            onDismiss()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun ShareOptionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
