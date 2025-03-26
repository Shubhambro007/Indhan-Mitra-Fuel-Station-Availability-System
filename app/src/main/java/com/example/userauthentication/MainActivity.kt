package com.example.userauthentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.userauthentication.ui.theme.UserAuthenticationTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserAuthenticationTheme {
                NavigationView()
            }
        }
    }
}

@Composable
fun NavigationView() {

    val context = LocalContext.current
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination ="welcome" ){
        composable("welcome"){ WelcomeScreen(navController)}
        composable("login"){ LoginScreen(navController,context)}
        composable("signup"){SignUpScreen(navController, context)}
        composable("home"){ HomePage(navController) }

    }
}