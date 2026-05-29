package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val handle: String, // e.g. "@johndoe"
    val username: String,
    val avatarUrl: String,
    val bio: String,
    val followersCount: Int,
    val followingCount: Int,
    val totalLikes: Int,
    val isVerified: Boolean = false,
    val isPrivate: Boolean = false,
    val isBlocked: Boolean = false
) : java.io.Serializable

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey val id: String,
    val authorHandle: String,
    val authorName: String,
    val authorAvatar: String,
    val isVerifiedAuthor: Boolean = false,
    val type: String, // "TEXT" | "IMAGE" | "VIDEO"
    val contentUrl: String, // Meme image link, text caption or gradient color description
    val caption: String,
    val category: String, // Bollywood, Desi, Gaming, Life, Office etc
    val hashtags: String, // comma-separated e.g. "funny,desi,viral"
    val likesCount: Int,
    val commentsCount: Int,
    val sharesCount: Int,
    val viewCount: Int = 120, // default view counter for realism
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : java.io.Serializable

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey val id: String,
    val postId: String,
    val authorName: String,
    val authorHandle: String,
    val authorAvatar: String,
    val text: String,
    val likesCount: Int,
    val timestamp: Long = System.currentTimeMillis()
) : java.io.Serializable

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val type: String, // "LIKE", "COMMENT", "FOLLOW", "MENTION", "SYSTEM"
    val postId: String? = null,
    val actorName: String,
    val actorHandle: String,
    val actorAvatar: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : java.io.Serializable

@Entity(tableName = "admin_settings")
data class AdminSettingEntity(
    @PrimaryKey val key: String,
    val value: String
) : java.io.Serializable
