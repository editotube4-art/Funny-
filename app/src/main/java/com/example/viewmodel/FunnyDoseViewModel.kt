package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

// --- Active Screens Enum ---
enum class AppScreen {
    FEED,
    CREATE,
    NOTIFICATIONS,
    PROFILE,
    ADMIN,
    AUTH
}

// --- Active Feed Tab Enum ---
enum class FeedTab {
    FOLLOWING,
    EXPLORE,
    REELS
}

class FunnyDoseViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val userDao = db.userDao()
    private val postDao = db.postDao()
    private val commentDao = db.commentDao()
    private val notificationDao = db.notificationDao()
    private val adminSettingDao = db.adminSettingDao()

    private val sharedPrefs = application.getSharedPreferences("funnydose_prefs", Context.MODE_PRIVATE)

    // --- State Declarations ---

    private val _currentScreen = MutableStateFlow(AppScreen.FEED)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentFeedTab = MutableStateFlow(FeedTab.EXPLORE)
    val currentFeedTab: StateFlow<FeedTab> = _currentFeedTab.asStateFlow()

    private val _activeCategory = MutableStateFlow("Trending")
    val activeCategory: StateFlow<String> = _activeCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // --- Auth State ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    // --- Suggestion State ---
    private val _aiSuggestion = MutableStateFlow("")
    val aiSuggestion: StateFlow<String> = _aiSuggestion.asStateFlow()

    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // --- Dynamic Feed Lists ---
    val allPosts: StateFlow<List<PostEntity>> = postDao.getAllPostsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationEntity>> = notificationDao.getAllNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Comments for Dialog ---
    private val _activePostForComments = MutableStateFlow<PostEntity?>(null)
    val activePostForComments: StateFlow<PostEntity?> = _activePostForComments.asStateFlow()

    private val _currentComments = MutableStateFlow<List<CommentEntity>>(emptyList())
    val currentComments: StateFlow<List<CommentEntity>> = _currentComments.asStateFlow()

    // --- Monetization State & Admin Overrides ---
    private val _adminSettings = MutableStateFlow<Map<String, String>>(emptyMap())
    val adminSettings: StateFlow<Map<String, String>> = _adminSettings.asStateFlow()

    private val _adRevenue = MutableStateFlow(24.50f)
    val adRevenue: StateFlow<Float> = _adRevenue.asStateFlow()

    private val _viewCounter = MutableStateFlow(0)
    val viewCounter: StateFlow<Int> = _viewCounter.asStateFlow()

    // Streaks & Rewards
    private val _userCoins = MutableStateFlow(120)
    val userCoins: StateFlow<Int> = _userCoins.asStateFlow()

    private val _dailyStreak = MutableStateFlow(3)
    val dailyStreak: StateFlow<Int> = _dailyStreak.asStateFlow()

    init {
        // Load settings and user details
        loadActiveSession()
        loadAdminSettings()
        // Populate starting posts if empty
        viewModelScope.launch(Dispatchers.IO) {
            checkAndPrepopulateData()
        }
    }

    // --- Navigation ---
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun selectFeedTab(tab: FeedTab) {
        _currentFeedTab.value = tab
    }

    fun selectCategory(category: String) {
        _activeCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // --- Session & Authentication with Room & Supabase ---

    private fun loadActiveSession() {
        viewModelScope.launch(Dispatchers.IO) {
            val savedHandle = sharedPrefs.getString("logged_in_handle", "")
            if (!savedHandle.isNullOrEmpty()) {
                val dbUser = userDao.getUserByHandleSync(savedHandle)
                if (dbUser != null) {
                    _currentUser.value = dbUser
                } else {
                    // Create default active profile
                    val defaultUser = UserEntity(
                        handle = savedHandle,
                        username = savedHandle.replace("@", "").capitalize(),
                        avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100&auto=format&fit=crop",
                        bio = "FunnyDose Enthusiast ⚡ Making viral posts daily!",
                        followersCount = 4200,
                        followingCount = 180,
                        totalLikes = 28900,
                        isVerified = true
                    )
                    userDao.insertUser(defaultUser)
                    _currentUser.value = defaultUser
                }
            } else {
                // Pre-populate with a cool default profile on first launch to skip friction
                val starterHandle = "@desi_comedian"
                val starterUser = UserEntity(
                    handle = starterHandle,
                    username = "Desi Comedian",
                    avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=120&auto=format&fit=crop",
                    bio = "Desi memes, office logic, cricket reactions. verified creator! 🏏😂",
                    followersCount = 8900,
                    followingCount = 312,
                    totalLikes = 145200,
                    isVerified = true
                )
                userDao.insertUser(starterUser)
                sharedPrefs.edit().putString("logged_in_handle", starterHandle).apply()
                _currentUser.value = starterUser
            }
        }
    }

    fun signUpUser(email: String, username: String, handle: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val formattedHandle = if (handle.startsWith("@")) handle else "@$handle"
            try {
                // 1. Attempt Supabase real Sign-up
                val apiRequest = SupabaseAuthRequest(
                    email = email,
                    password = "TempPassword123!",
                    data = mapOf("username" to username, "handle" to formattedHandle)
                )
                try {
                    SupabaseClient.service.signUp(SupabaseClient.getApiKey(), apiRequest)
                } catch (netErr: Exception) {
                    // Log net variance, proceed gracefully
                }

                // 2. Persist local user profile
                val newUser = UserEntity(
                    handle = formattedHandle,
                    username = username,
                    avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=100&auto=format&fit=crop",
                    bio = "Newly joined FunnyDose creator! 🚀",
                    followersCount = 1,
                    followingCount = 10,
                    totalLikes = 0,
                    isVerified = false
                )
                userDao.insertUser(newUser)
                sharedPrefs.edit().putString("logged_in_handle", formattedHandle).apply()
                _currentUser.value = newUser
                _currentScreen.value = AppScreen.PROFILE
            } catch (e: Exception) {
                _authError.value = e.localizedMessage ?: "Failed to sign up User"
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun logInUser(handle: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val formattedHandle = if (handle.startsWith("@")) handle else "@$handle"
            try {
                var user = userDao.getUserByHandleSync(formattedHandle)
                if (user == null) {
                    // Generate one on demand
                    user = UserEntity(
                        handle = formattedHandle,
                        username = formattedHandle.replace("@", "").capitalize(),
                        avatarUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100&auto=format&fit=crop",
                        bio = "Viral memes are my dose | Back in action! 😎",
                        followersCount = 1500,
                        followingCount = 200,
                        totalLikes = 4500,
                        isVerified = true
                    )
                    userDao.insertUser(user)
                }
                sharedPrefs.edit().putString("logged_in_handle", formattedHandle).apply()
                _currentUser.value = user
                _currentScreen.value = AppScreen.FEED
            } catch (e: Exception) {
                _authError.value = "Username failed: ${e.message}"
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun logOutCurrent() {
        viewModelScope.launch {
            sharedPrefs.edit().remove("logged_in_handle").apply()
            _currentUser.value = null
            _currentScreen.value = AppScreen.AUTH
        }
    }

    // --- Profile System Updates ---

    fun updateProfile(username: String, bio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = _currentUser.value ?: return@launch
            val updatedUser = user.copy(username = username, bio = bio)
            userDao.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun followCreatorToggle(targetHandle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val creatorUser = userDao.getUserByHandleSync(targetHandle) ?: return@launch
            val current = _currentUser.value ?: return@launch

            val isCurrentlyFollower = creatorUser.followersCount > 1000 && (creatorUser.followersCount % 2 == 1) // simulated
            val newFollowerCount = if (isCurrentlyFollower) creatorUser.followersCount - 1 else creatorUser.followersCount + 1

            val updatedCreator = creatorUser.copy(followersCount = newFollowerCount)
            userDao.insertUser(updatedCreator)

            // Notify Target
            if (!isCurrentlyFollower) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        type = "FOLLOW",
                        actorName = current.username,
                        actorHandle = current.handle,
                        actorAvatar = current.avatarUrl,
                        message = "started following you. Check out their latest posts!"
                    )
                )
            }
        }
    }

    // --- Post System Actions ---

    fun triggerDoubleTapLike(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = postDao.getPostById(postId) ?: return@launch
            if (!post.isLiked) {
                likePostToggle(postId)
            }
        }
    }

    fun likePostToggle(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = postDao.getPostById(postId) ?: return@launch
            val newLikedState = !post.isLiked
            val newLikesCount = if (newLikedState) post.likesCount + 1 else post.likesCount - 1

            postDao.updatePostLikeStatus(postId, newLikesCount, newLikedState)

            // Trigger real-time notification simulation and ad revenue increment
            val current = _currentUser.value
            if (newLikedState && current != null && post.authorHandle != current.handle) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        type = "LIKE",
                        postId = postId,
                        actorName = current.username,
                        actorHandle = current.handle,
                        actorAvatar = current.avatarUrl,
                        message = "liked your post: \"${post.caption.take(24)}...\""
                    )
                )
                // Add revenue with action
                _adRevenue.value += 0.05f
                _userCoins.value += 5
            }
        }
    }

    fun bookmarkPostToggle(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = postDao.getPostById(postId) ?: return@launch
            val updated = post.copy(isBookmarked = !post.isBookmarked)
            postDao.insertPost(updated)
        }
    }

    fun checkAndAddAdRevenueOnView() {
        _viewCounter.value += 1
        // Every 3 views triggers remote config frequency logic and earns $0.02
        if (_viewCounter.value % 3 == 0) {
            _adRevenue.value += 0.02f
        }
    }

    fun addDailyCoinsReward() {
        _userCoins.value += 50
        _dailyStreak.value += 1
    }

    // --- Comments Screen States ---

    fun openCommentsForPost(post: PostEntity) {
        _activePostForComments.value = post
        viewModelScope.launch(Dispatchers.IO) {
            commentDao.getCommentsForPost(post.id).collectLatest { list ->
                _currentComments.value = list
            }
        }
    }

    fun closeComments() {
        _activePostForComments.value = null
        _currentComments.value = emptyList()
    }

    fun submitPostComment(commentText: String) {
        val activePost = _activePostForComments.value ?: return
        val current = _currentUser.value ?: return
        if (commentText.trim().isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val commentId = UUID.randomUUID().toString()
            val comment = CommentEntity(
                id = commentId,
                postId = activePost.id,
                authorName = current.username,
                authorHandle = current.handle,
                authorAvatar = current.avatarUrl,
                text = commentText,
                likesCount = 0
            )
            // Save to local Room SQL DB
            commentDao.insertComment(comment)
            // Increment UI post count
            postDao.incrementCommentsCount(activePost.id)

            // Trigger notification
            if (activePost.authorHandle != current.handle) {
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        type = "COMMENT",
                        postId = activePost.id,
                        actorName = current.username,
                        actorHandle = current.handle,
                        actorAvatar = current.avatarUrl,
                        message = "commented: \"$commentText\" on your meme"
                    )
                )
            }
        }
    }

    // --- Post Creation View logic with Gemini ---

    fun generateAIPostSuggestions(memeTopic: String) {
        if (memeTopic.trim().isEmpty()) return
        viewModelScope.launch {
            _aiLoading.value = true
            _aiSuggestion.value = ""
            try {
                val suggestion = GeminiClient.getMemeAICaption(memeTopic)
                _aiSuggestion.value = suggestion
            } catch (e: Exception) {
                _aiSuggestion.value = "AI Suggestion Error: When compiling goes perfect but the output screen is blank 🥶 #justcoderthings"
            } finally {
                _aiLoading.value = false
            }
        }
    }

    fun publishNewPost(caption: String, tags: String, category: String, postType: String) {
        val current = _currentUser.value ?: return
        val finalCaption = caption.ifEmpty { "Viral entry from ${current.username} 😂" }
        viewModelScope.launch(Dispatchers.IO) {
            // Generate synthetic visuals/colors matching gradient coordinates
            val mediaGradientCode = when(category) {
                "Cricket" -> "LGR_01" // Sports Green Accent
                "Bollywood" -> "LGR_02" // Filmy Neon Pink
                "Office" -> "LGR_03" // Midnight Blue work vibe
                "Gaming" -> "LGR_04" // Cyberpunk Purple
                else -> "LGR_DEFAULT" // Rainbow gradient
            }

            val newPost = PostEntity(
                id = UUID.randomUUID().toString(),
                authorHandle = current.handle,
                authorName = current.username,
                authorAvatar = current.avatarUrl,
                isVerifiedAuthor = current.isVerified,
                type = postType, // "TEXT" | "IMAGE" | "VIDEO"
                contentUrl = mediaGradientCode,
                caption = finalCaption,
                category = category,
                hashtags = tags.replace(" ", ""),
                likesCount = 1,
                commentsCount = 0,
                sharesCount = 5,
                timestamp = System.currentTimeMillis()
            )

            // Insert into local cache
            postDao.insertPost(newPost)

            // Sync with Supabase asynchronously if credentials configured
            try {
                val supabasePost = SupabasePostModel(
                    id = newPost.id,
                    caption = newPost.caption,
                    type = newPost.type,
                    contentUrl = newPost.contentUrl,
                    category = newPost.category,
                    hashtags = newPost.hashtags,
                    likesCount = newPost.likesCount,
                    commentsCount = newPost.commentsCount,
                    authorHandle = newPost.authorHandle
                )
                SupabaseClient.service.upsertPost(
                    apiKey = SupabaseClient.getApiKey(),
                    bearer = "Bearer ${SupabaseClient.getApiKey()}",
                    post = supabasePost
                )
            } catch (e: Exception) {
                // Ignore silent syncing drops (resilio offline)
            }

            // Reward creator
            _userCoins.value += 15
            _currentScreen.value = AppScreen.FEED
        }
    }

    // --- Admin Settings Panel Adjustments ---

    private fun loadAdminSettings() {
        viewModelScope.launch {
            adminSettingDao.getAllSettingsFlow().collectLatest { list ->
                val map = list.associate { it.key to it.value }
                _adminSettings.value = map
            }
        }
    }

    fun updateAdminToggles(key: String, value: String) {
        viewModelScope.launch(Dispatchers.IO) {
            adminSettingDao.insertSetting(AdminSettingEntity(key, value))
            // Reload
            val updatedMap = _adminSettings.value.toMutableMap()
            updatedMap[key] = value
            _adminSettings.value = updatedMap
        }
    }

    fun executeAdminBan(userHandle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = userDao.getUserByHandleSync(userHandle)
            if (user != null) {
                val updated = user.copy(isBlocked = true)
                userDao.insertUser(updated)

                // Inject notification warning
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = UUID.randomUUID().toString(),
                        type = "SYSTEM",
                        actorName = "Admin Moderation Tools",
                        actorHandle = "@admin",
                        actorAvatar = "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=100&auto=format&fit=crop",
                        message = "Flagged actions detected. Creator accounts violating NSFW policies risk shadow-bans."
                    )
                )
            }
        }
    }

    fun executeAdminPostDelete(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            postDao.deletePostById(postId)
            // Add alert message
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    type = "SYSTEM",
                    actorName = "Content Moderator",
                    actorHandle = "@mod",
                    actorAvatar = "",
                    message = "A reported post violating NSFW filtering was securely removed from Explore feed."
                )
            )
        }
    }

    fun sendAdminBroadCastAlert(title: String, alertText: String) {
        viewModelScope.launch(Dispatchers.IO) {
            notificationDao.insertNotification(
                NotificationEntity(
                    id = UUID.randomUUID().toString(),
                    type = "SYSTEM",
                    actorName = title,
                    actorHandle = "@system",
                    actorAvatar = "",
                    message = alertText
                )
            )
        }
    }

    // --- Database Prepopulation with memes, jokes, reel-grades ---

    private suspend fun checkAndPrepopulateData() {
        val existing = postDao.getAllPostsFlow().first()
        if (existing.isNotEmpty()) return

        // 1. Prepopulate users
        val starCreators = listOf(
            UserEntity("@desi_guru", "Desi Guru", "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&auto=format&fit=crop", "Professional Overthinker | Meme distributor 🍿", 41200, 312, 192800, true),
            UserEntity("@bollywood_gossip", "Bollywood Gossip", "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=100&auto=format&fit=crop", "Spilling tea on B-Town stars ☕💃", 98200, 48, 892400, true),
            UserEntity("@cricket_hub", "Cricket Hub India", "https://images.unsplash.com/photo-1624555130581-1d9cca783bc0?w=100&auto=format&fit=crop", "CRICKET IS LIFE 🏏 Boundary updates & funny reactions!", 145000, 110, 1205300, true),
            UserEntity("@office_comedy", "Corporate Majdoor", "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=100&auto=format&fit=crop", "Crying in Excel sheets since 2021. 😭📊", 52100, 600, 451900, false)
        )
        starCreators.forEach { userDao.insertUser(it) }

        // 2. Prepopulate starting posts
        val starterPosts = listOf(
            PostEntity(
                id = "start_post_1",
                authorHandle = "@desi_guru",
                authorName = "Desi Guru",
                authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&auto=format&fit=crop",
                isVerifiedAuthor = true,
                type = "TEXT",
                contentUrl = "LGR_02", // Gradient Code
                caption = "Interviewer: 'Where do you see yourself in 5 years?'\n\nMe: 'Probably in the mirror while brushing my teeth, hopefully in a bigger bathroom.' 😂",
                category = "Desi",
                hashtags = "desi,interview,joblife,funny",
                likesCount = 8452,
                commentsCount = 2,
                sharesCount = 1420
            ),
            PostEntity(
                id = "start_post_2",
                authorHandle = "@office_comedy",
                authorName = "Corporate Majdoor",
                authorAvatar = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=100&auto=format&fit=crop",
                isVerifiedAuthor = false,
                type = "IMAGE",
                contentUrl = "https://images.unsplash.com/photo-1531538606174-0f90ff5dce83?w=500&auto=format&fit=crop", // Team meeting image placeholder
                caption = "When the manager says 'Let me share my screen' and you hurriedly close all your job search browser tabs on the extended monitor... 📉🤐",
                category = "Office",
                hashtags = "excel,officelife,corporatelove",
                likesCount = 12450,
                commentsCount = 1,
                sharesCount = 2390
            ),
            PostEntity(
                id = "start_post_3",
                authorHandle = "@cricket_hub",
                authorName = "Cricket Hub India",
                authorAvatar = "https://images.unsplash.com/photo-1624555130581-1d9cca783bc0?w=100&auto=format&fit=crop",
                isVerifiedAuthor = true,
                type = "VIDEO",
                contentUrl = "LGR_01", // Green pulsating gradient simulating reels
                caption = "Desi fans when Kohli runs a single vs when his wife applauds from the VIP stands! 😂 Over-the-top stadium drama is unmatched!",
                category = "Cricket",
                hashtags = "kohli,cricket,indVsPak,iplviral",
                likesCount = 42890,
                commentsCount = 3,
                sharesCount = 8900
            ),
            PostEntity(
                id = "start_post_4",
                authorHandle = "@bollywood_gossip",
                authorName = "Bollywood Gossip",
                authorAvatar = "https://images.unsplash.com/photo-1580489944761-15a19d654956?w=100&auto=format&fit=crop",
                isVerifiedAuthor = true,
                type = "TEXT",
                contentUrl = "LGR_04", // Cyan purple gradient
                caption = "Bolly star kids explaining their 'extreme struggle': 'The AC in the vanity van was not set to 21 degrees, the water bottles were standard mineral, NOT Himalayan sparkling.' 😭🤡",
                category = "Bollywood",
                hashtags = "bollywood,starkids,struggles,nepo",
                likesCount = 19412,
                commentsCount = 0,
                sharesCount = 4200
            ),
            PostEntity(
                id = "start_post_5",
                authorHandle = "@desi_guru",
                authorName = "Desi Guru",
                authorAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100&auto=format&fit=crop",
                isVerifiedAuthor = true,
                type = "IMAGE",
                contentUrl = "https://images.unsplash.com/photo-1511512578047-dfb367046420?w=500&auto=format&fit=crop", // Gaming placeholder
                caption = "Me staying up until 4 AM playing RPG matches but taking a sick leave from work because of 'mild physical exhaustion'... 🥱🎮",
                category = "Gaming",
                hashtags = "gaming,memes,gamerz,sickday",
                likesCount = 6820,
                commentsCount = 0,
                sharesCount = 890
            )
        )
        starterPosts.forEach { postDao.insertPost(it) }

        // Populate Starter Static Comments
        val starterComments = listOf(
            CommentEntity("comment_1", "start_post_1", "Gamer Pro", "@game_pro", "", "Bruh this is too accurate! 🤣 Literally me every morning.", 45),
            CommentEntity("comment_2", "start_post_1", "Sneha Roy", "@sneha", "", "Bathroom sizes are direct proportional to salary hike!", 12),
            CommentEntity("comment_3", "start_post_2", "John Hr", "@johnhr", "", "As an HR, I am keeping an eye on this comment section 👀", 280),
            CommentEntity("comment_4", "start_post_3", "Rajiv Sharma", "@rajiv", "", "Over-the-top cricket reactions are the best thing on earth!", 150),
            CommentEntity("comment_5", "start_post_3", "Simran Gill", "@simi", "", "Kohli's expressions are ready-made reels content 🍿🏆", 92)
        )
        starterComments.forEach { commentDao.insertComment(it) }

        // Populate starter Notifications
        val starterNotifications = listOf(
            NotificationEntity(UUID.randomUUID().toString(), "SYSTEM", null, "FunnyDose Admin", "@admin", "https://images.unsplash.com/photo-1543269865-cbf427effbad?w=100&auto=format&fit=crop", "Welcome to FunnyDose! Post viral memes, double tap to like, check your ad analytics on the Admin page, and use Gemini AI suggestions to create caption ideas! Happy dosing! 😉🔥"),
            NotificationEntity(UUID.randomUUID().toString(), "COMMENT", "start_post_1", "Sneha Roy", "@sneha", "", "commented: 'Bathroom sizes are direct proportional to salary hike!' on your post"),
            NotificationEntity(UUID.randomUUID().toString(), "LIKE", "start_post_1", "Gamer Pro", "@game_pro", "", "liked your joke post"),
            NotificationEntity(UUID.randomUUID().toString(), "FOLLOW", null, "Anjali Sen", "@anjali", "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=100&auto=format&fit=crop", "started following you. Check out their profile!")
        )
        starterNotifications.forEach { notificationDao.insertNotification(it) }

        // Populate starter Admin Configuration settings
        val starterAdminSettings = listOf(
            AdminSettingEntity("nsfw_filter_enabled", "true"),
            AdminSettingEntity("ads_networks_active", "AdMob, Unity Ads"),
            AdminSettingEntity("frequency_ad_intervals", "3"),
            AdminSettingEntity("uploads_enabled", "true"),
            AdminSettingEntity("monetization_joined", "true"),
            AdminSettingEntity("gemini_suggestions_enabled", "true")
        )
        starterAdminSettings.forEach { adminSettingDao.insertSetting(it) }
    }
}
