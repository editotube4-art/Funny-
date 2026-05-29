package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.FunnyDoseViewModel

class MainActivity : ComponentActivity() {
  
  private val viewModel: FunnyDoseViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val currentScreen by viewModel.currentScreen.collectAsState()
        val currentUser by viewModel.currentUser.collectAsState()

        // Force redirect to Auth if no active creator session exists
        LaunchedEffect(currentUser) {
          if (currentUser == null) {
            viewModel.navigateTo(AppScreen.AUTH)
          } else if (currentScreen == AppScreen.AUTH) {
            viewModel.navigateTo(AppScreen.FEED)
          }
        }

        Scaffold(
          modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.background),
          bottomBar = {
            if (currentUser != null && currentScreen != AppScreen.AUTH) {
              BottomNavBar(viewModel = viewModel)
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
          ) {
            when (currentScreen) {
              AppScreen.FEED -> FeedScreen(viewModel = viewModel)
              AppScreen.CREATE -> CreatePostScreen(viewModel = viewModel)
              AppScreen.NOTIFICATIONS -> NotificationScreen(viewModel = viewModel)
              AppScreen.PROFILE -> ProfileScreen(viewModel = viewModel)
              AppScreen.ADMIN -> AdminScreen(viewModel = viewModel)
              AppScreen.AUTH -> AuthScreen(viewModel = viewModel)
            }
          }
        }
      }
    }
  }
}
