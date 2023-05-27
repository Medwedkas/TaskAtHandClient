package com.example.myapplication

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val number: String, val password: String)

@Serializable
data class LoginResponse(val auth: Boolean, val uid: Int, val name: String, val role: Int)

@Composable
fun LoginPage(navController: NavController) {
    val number = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFC7F1E8)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 1.dp)
        ) {
            Image(
                painter = painterResource(R.mipmap.logo), // Путь к ресурсу с картинкой
                contentDescription = "Logo",
                modifier = Modifier
                    .size(260.dp) // Размер квадратной картинки
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Номер телефона:",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .background(color = Color(0xFF71AEB2))
                .padding(start = 50.dp),
            fontSize = 25.sp,
            color = Color.White,
            style = TextStyle(fontStyle = FontStyle.Normal)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(75.dp)
                .width(350.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .background(color = Color.White)
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
        ) {
            BasicTextField(
                value = number.value,
                onValueChange = { number.value = it },
                textStyle = TextStyle.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Пароль:",
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Start)
                .background(color = Color(0xFF71AEB2))
                .padding(start = 50.dp),
            fontSize = 25.sp,
            color = Color.White,
            style = TextStyle(fontStyle = FontStyle.Normal)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(75.dp)
                .width(350.dp)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .background(color = Color.White)
                .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
        ) {
            BasicTextField(
                value = password.value,
                onValueChange = { password.value = it },
                textStyle = TextStyle.Default,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                singleLine = true
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        performLogin(number.value, password.value, navController)
                    } catch (e: Exception) {
                        Log.e("XD", "Error: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .width(300.dp)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                Color(0xFF71AEB2) // Цвет фона кнопки
            ),
            contentPadding = PaddingValues(16.dp)
        ) {
            Text(
                text = "Войти",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

private suspend fun performLogin(number: String, password: String, navController: NavController) {
    val httpClient = OkHttpClient()
    val loginRequest = LoginRequest(number, password)
    val mediaType = "application/json".toMediaType()
    val requestBody = Json.encodeToString(loginRequest)
    val request = Request.Builder()
        .url("http://192.168.1.47:8080/login")
        .post(requestBody.toRequestBody(mediaType))
        .build()

    try {
        val response = withContext(Dispatchers.IO) {
            httpClient.newCall(request).execute()
        }

        val responseBody = response.body?.string()

        if (response.isSuccessful) {
            val json = Json { ignoreUnknownKeys = true }
            val loginResponse = responseBody?.let { json.decodeFromString<LoginResponse>(it) }
            if (loginResponse?.auth != false) {
                val user = User(
                    uid = loginResponse?.uid ?: 0,
                    name = loginResponse?.name ?: "",
                    role = loginResponse?.role ?: 0
                )
                UserManager.user = user // Сохраняем пользователя в UserManager
                Toast.makeText(
                    navController.context,
                    "Success",
                    Toast.LENGTH_SHORT
                ).show()
                withContext(Dispatchers.Main) {
                    if (UserManager.user!!.role == 1 || UserManager.user!!.role == 2) {
                        navController.navigate("admin_tasks")
                    } else {
                        navController.navigate("task")
                    }

                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        navController.context,
                        "Invalid password",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    navController.context,
                    "Login error",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    } catch (e: Exception) {
        withContext(Dispatchers.Main) {
            Toast.makeText(
                navController.context,
                "Error: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}