package com.example.myapplication.feature_stat

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URL


@Serializable
data class User(
    val uid: Int,
    val number: String,
    val password: String,
    val name: String,
    val status: String,
    val role: Int
)

suspend fun fetchUser(uid: Int): User? = withContext(Dispatchers.IO) {
    try {
        val client = OkHttpClient()
        val url = URL(MainActivity.ApiConfig.BASE_URL + "getUser")
        val requestBodyJson = "{\"uid\": $uid}"
        val mediaType = "application/json".toMediaType()
        val requestBody = requestBodyJson.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseJson = response.body?.string()
            val users = Json.decodeFromString<List<User>>(responseJson ?: "")
            return@withContext users.firstOrNull()
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

suspend fun fetchAttendanceStats(uid: Int, startDate: String, endDate: String): AttendanceStats? =
    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = URL(MainActivity.ApiConfig.BASE_URL + "stats/attendance")
            val requestBodyJson =
                "{\"user_id\": $uid, \"start_date\": \"$startDate\", \"end_date\": \"$endDate\"}"
            val mediaType = "application/json".toMediaType()
            val requestBody = requestBodyJson.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseJson = response.body?.string()
                return@withContext Json.decodeFromString<AttendanceStats>(responseJson ?: "")
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

// Fetch task statistics
suspend fun fetchTaskStats(uid: Int, startDate: String, endDate: String): TaskStats? =
    withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient()
            val url = URL(MainActivity.ApiConfig.BASE_URL + "stats/taskStats")
            val requestBodyJson =
                "{\"user_id\": $uid, \"start_date\": \"$startDate\", \"end_date\": \"$endDate\"}"
            val mediaType = "application/json".toMediaType()
            val requestBody = requestBodyJson.toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseJson = response.body?.string()
                val json = Json { ignoreUnknownKeys = true } // Используем ignoreUnknownKeys = true
                return@withContext json.decodeFromString<TaskStats>(responseJson ?: "")
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

@Serializable
data class AttendanceStats(
    val total_worked_hours: Double,
    val shortfall_hours: Double,
    val overtime_hours: Double
)

@Serializable
data class TaskStats(
    val overdue_tasks_count: Int,
    val on_time_tasks_countval: Int,
    val assigned_tasks_count: Int
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatScreen(uid: Int, navController: NavHostController) {
    var user: User? by remember { mutableStateOf(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isCardVisible by remember { mutableStateOf(false) }
    var stats: AttendanceStats? by remember { mutableStateOf(null) }
    var taskStats: TaskStats? by remember { mutableStateOf(null) }

    LaunchedEffect(uid) {
        user = fetchUser(uid)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxSize()
            .fillMaxWidth()
            .background(Color(0xFF3C3A3F)),
        topBar = {
            TopAppBar(
                title = { Text(text = "Статистика") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .fillMaxSize()
                    .background(Color(0xFF323034)),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF3C3A3F),
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    if (user != null) {
                        // Display user information
                        Text(
                            text = "Имя: ${user!!.name}",
                            color = Color(0xFFfafaf9),
                            style = TextStyle(fontWeight = FontWeight.Bold),
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Номер: ${user!!.number}",
                            color = Color(0xFFfafaf9),
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Статус: ${user!!.status}",
                            color = Color(0xFFfafaf9),
                            fontSize = 16.sp
                        )

                        val startDateState = remember { mutableStateOf("") }
                        val endDateState = remember { mutableStateOf("") }

                        TextField(
                            value = TextFieldValue(startDateState.value),
                            onValueChange = { startDateState.value = it.text },
                            label = {
                                Text(
                                    text = "Начальная дата",
                                    color = Color(0xFF717379),
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color(0xFF717379),
                                focusedLabelColor = Color(0xFF717379),
                                cursorColor = Color(0xFF717379),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                        )

                        TextField(
                            value = TextFieldValue(endDateState.value),
                            onValueChange = { endDateState.value = it.text },
                            label = {
                                Text(
                                    text = "Конечная дата",
                                    color = Color(0xFF717379),
                                    fontSize = 14.sp
                                )
                            },
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = Color(0xFF717379),
                                focusedLabelColor = Color(0xFF717379),
                                cursorColor = Color(0xFF717379),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                        )

                        Button(

                            onClick = {

                                CoroutineScope(Dispatchers.Main).launch {
                                    isLoading = true
                                    val attendanceStats =
                                        fetchAttendanceStats(
                                            uid,
                                            startDateState.value,
                                            endDateState.value
                                        )
                                    val fetchedTaskStats =
                                        fetchTaskStats(
                                            uid,
                                            startDateState.value,
                                            endDateState.value
                                        )
                                    isLoading = false
                                    isCardVisible = true
                                    stats = attendanceStats
                                    taskStats = fetchedTaskStats
                                }
                            },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .align(CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(
                                Color(0xFF3C3A3F),
                                contentColor = Color(0xFFfafaf9)
                            )
                        ) {
                            Text(text = "Получить статистику", fontSize = 16.sp)
                        }

                        if (isCardVisible) {
                            Card(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .background(Color(0xFF323034)),
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Column(
                                    Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    if (stats != null) {
                                        Text(
                                            "Общее количество отработанных часов: ${stats!!.total_worked_hours}",
                                            color = Color(0xFF717379),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Недостающие часы: ${stats!!.shortfall_hours}",
                                            color = Color(0xFF717379),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Переработанные часы: ${stats!!.overtime_hours}",
                                            color = Color(0xFF717379),
                                            fontSize = 16.sp
                                        )
                                    }

                                    if (taskStats != null) {
                                        Text(
                                            "Количество просроченных задач: ${taskStats!!.overdue_tasks_count}",
                                            color = Color(0xFF717379),
                                            fontSize = 16.sp
                                        )
                                        Text(
                                            "Количество задач выполненных вовремя: ${taskStats!!.on_time_tasks_countval}",
                                            color = Color(0xFF717379),
                                            fontSize = 16.sp
                                        )
                                        //Text("Общее количество назначенных задач: ${taskStats!!.assigned_tasks_count}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}



