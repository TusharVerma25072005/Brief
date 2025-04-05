package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.entity.EmailEntity
import com.example.myapplication.ui.screens.EmailDetailScreen
import com.example.myapplication.ui.screens.EmailListScreen
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.viewmodel.AuthViewModel
import com.example.myapplication.ui.viewmodel.EmailViewModel
import com.example.myapplication.worker.WorkManager

class MainActivity : ComponentActivity() {
    private lateinit var authViewModel: AuthViewModel
    private lateinit var emailViewModel: EmailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize ViewModels
        val app = application as BriefyApplication
        emailViewModel = ViewModelProvider(this, EmailViewModel.Factory(app.repository))[EmailViewModel::class.java]
        authViewModel = ViewModelProvider(this, AuthViewModel.Factory(this))[AuthViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    BriefyNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        emailViewModel = emailViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun BriefyNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    emailViewModel: EmailViewModel,
    modifier: Modifier = Modifier
) {
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    val selectedEmail by emailViewModel.selectedEmail.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isAuthenticated) "emailList" else "login",
        modifier = modifier
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    // Schedule email sync with the auth token
                    WorkManager.scheduleEmailSync(navController.context, token)
                    navController.navigate("emailList") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("emailList") {
            EmailListScreen(
                viewModel = emailViewModel,
                onEmailSelected = { email ->
                    emailViewModel.selectEmail(email)
                    navController.navigate("emailDetail")
                }
            )
        }

        composable("emailDetail") {
            selectedEmail?.let { email ->
                EmailDetailScreen(
                    email = email,
                    onBackPressed = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}