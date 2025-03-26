package com.example.userauthentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.userauthentication.components.CButton
import com.example.userauthentication.components.CTextField
import com.example.userauthentication.components.DontHaveAccount
import com.example.userauthentication.ui.theme.AlegreyaSansFontFamily
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    context: Context,
    modifier: Modifier = Modifier
) {
    val dbHelper = DatabaseHelper(context)
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }
    val rememberMe = remember { mutableStateOf(false) } // State for Remember Me checkbox
    val userPreferences = UserPreferences(context) // Pass context to UserPreferences

    // Load saved credentials
    LaunchedEffect(Unit) {
        val savedCredentials = userPreferences.getUser_Credentials()
        email.value = savedCredentials.first ?: ""
        password.value = savedCredentials.second ?: ""
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.login2),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = modifier.fillMaxSize()
        )

        // Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.logo3),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 54.dp)
                    .height(100.dp)
                    .align(Alignment.Start)
                    .offset(x = (-20).dp)
            )
            Text(
                "Welcome to Indhan Mitra!",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = AlegreyaSansFontFamily,
                    fontWeight = FontWeight(500),
                    color = Color.White,
                ),
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                "Sign In",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = AlegreyaSansFontFamily,
                    fontWeight = FontWeight(500),
                    color = Color.LightGray,
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            // TextField Email
            CTextField(
                hint = "Email Address",
                value = email.value,
                onValueChange = { email.value = it }
            )

            // TextField Password
            CTextField(
                hint = "Password",
                value = password.value,
                onValueChange = { password.value = it }
            )

            // Remember Me Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = rememberMe.value,
                    onCheckedChange = { rememberMe.value = it }
                )
                Text("Remember Me", modifier = Modifier.padding(start = 8.dp),
                    style = TextStyle(color = Color.White)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Button
            CButton(text = "Sign In", onClick = {
                if (dbHelper.readUser (password.value, email.value)) {
                    if (rememberMe.value) {
                        userPreferences.saveUser_Credentials(email.value, password.value) // Save credentials if remembered
                    } else {
                        userPreferences.clearUser_Credentials() // Clear credentials if not remembered
                    }
                    navController.navigate("home") // Navigate to the home screen upon successful login
                } else {
                    showDialog.value = true // Show alert dialog if login fails
                }
            })

            // AlertDialog for wrong email or password
            if (showDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDialog.value = false },
                    title = { Text("Error") },
                    text = { Text("Wrong email or password. Please try again.") },
                    confirmButton = {
                        Button(onClick = { showDialog.value = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            DontHaveAccount(
                onSignUpTap = { navController.navigate("signup") }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun LoginScreenPreview() {
    LoginScreen(rememberNavController(), context = LocalContext.current) // Pass context here
}