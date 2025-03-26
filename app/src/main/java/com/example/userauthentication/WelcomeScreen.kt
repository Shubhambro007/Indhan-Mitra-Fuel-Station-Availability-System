package com.example.userauthentication

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.userauthentication.components.CButton
import com.example.userauthentication.components.DontHaveAccount
import com.example.userauthentication.ui.theme.AlegreyaFontFamily
import com.example.userauthentication.ui.theme.AlegreyaSansFontFamily
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.graphicsLayer


@Composable
fun WelcomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val infiniteTransition = rememberInfiniteTransition(label = "logo-rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ){
        //BackgroundImage
        Image(painter = painterResource(id = R.drawable.navy_blue_1), contentDescription = null ,
            contentScale = ContentScale.FillBounds,
            modifier = modifier.fillMaxSize()
            )

        //Content
        Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
        ) {

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painter = painterResource(id = R.drawable.logo3),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 54.dp)
                    .height(240.dp)
                    .width(320.dp)
                    .align(Alignment.Start)
                    .graphicsLayer {
                        rotationY = rotation // Apply the rotation
                    }
            )

            Text("WELCOME" , fontSize = 32.sp ,
                fontFamily = AlegreyaFontFamily,
                fontWeight = FontWeight(700),
                color = Color.White
                )

            Text("Ensuring peak efficiency and reliability.",
                    textAlign = TextAlign.Center,
                    fontFamily = AlegreyaSansFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )

            Spacer(modifier = Modifier.weight(1f))

            CButton(text = "Sign In With Email",
                onClick = {
                    navController.navigate("login")
                }
                )

            DontHaveAccount(
                onSignUpTap = {
                    navController.navigate("signup")
                }
            )



        }

    }

}

@Preview(showBackground = true, widthDp = 320, heightDp = 640)
@Composable
fun WelcomeScreenPreview(){
    WelcomeScreen(rememberNavController())
}
