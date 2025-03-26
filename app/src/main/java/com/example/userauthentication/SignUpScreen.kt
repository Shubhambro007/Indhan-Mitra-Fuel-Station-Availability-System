package com.example.userauthentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.userauthentication.components.CButton
import com.example.userauthentication.components.CTextField
import com.example.userauthentication.ui.theme.AlegreyaSansFontFamily
import android.content.Context
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext

@Composable
fun SignUpScreen(
    navController: NavController,
    context: Context, // Added context
    modifier: Modifier = Modifier
) {
    val dbHelper = DatabaseHelper(context)
    val name = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val showDialog = remember { mutableStateOf(false) }

    // Background Image
    Image(
        painter = painterResource(id = R.drawable.login2), contentDescription = null,
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
        Image(painter = painterResource(id = R.drawable.logo3), contentDescription = null,
            modifier = Modifier
                .padding(top = 30.dp)
                .height(100.dp)
                .align(Alignment.Start)
                .offset(x = (-20).dp)
        )

        Text("Welcome to Indhan Mitra!",
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = AlegreyaSansFontFamily,
                fontWeight = FontWeight(500),
                color = Color.White,
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        Text("Sign Up",
            style = TextStyle(
                fontSize = 28.sp,
                fontFamily = AlegreyaSansFontFamily,
                fontWeight = FontWeight(500),
                color = Color.LightGray,
            ),
            modifier = Modifier.align(Alignment.Start)
        )

        // TextField Name
        CTextField(hint = "Name", value = name.value, onValueChange = { name.value = it })

        // TextField Email
        CTextField(hint = "Email Address", value = email.value, onValueChange = { email.value = it })

        // TextField Password
        CTextField(hint = "Password", value = password.value, onValueChange = { password.value = it })

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Button
        CButton(text = "Sign Up", onClick = {
            val userId = dbHelper.insertUser(name.value, password.value, email.value)
            if (userId != -1L) {
                navController.navigate("login")
            } else {
                showDialog.value = true
            }
        })

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text("Error") },
                text = { Text("Please Fill Blank Fields") },
                confirmButton = {
                    Button(
                        onClick = { showDialog.value = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        // Have An Account Screen
        Row(modifier = Modifier.padding(top = 12.dp, bottom = 52.dp)) {
            Text("Already have an account ?",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = AlegreyaSansFontFamily,
                    color = Color.White,
                )
            )

            Text("Sign In",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = AlegreyaSansFontFamily,
                    color = Color.White,
                ),
                modifier = Modifier.clickable {
                    navController.navigate("login")
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(rememberNavController(), context = LocalContext.current)
}
