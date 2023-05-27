@file:OptIn(DelicateCoroutinesApi::class)

package com.example.myapplication.feature_tasks

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

data class TaskData(
    val uid: Int,
    val creator: String,
    val creatorRole: String,
    val executor: String,
    val executorRole: String,
    val description: String,
    val deadlines: String,
    val status: String,
    val header: String,
    val priority: String
)

@Composable
fun priorityColor(priority: String): Color {
    return when (priority) {
        "Высокий" -> Color.Red
        "Средний" -> Color.Yellow
        "Низкий" -> Color.Green
        else -> Color.Black
    }
}

// Измените функцию TaskDescriptionScreen
@Composable
fun TaskDescriptionScreen(uid: Int, navController: NavController) {
    var taskList by remember { mutableStateOf<List<Task>>(emptyList()) }
    val context = LocalContext.current
    LaunchedEffect(uid) {
        val json = JSONObject().apply {
            put("uid", uid)
        }
        val requestBody =
            json.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url("http://192.168.1.47:8080/tasks/getForTaskUid")
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonArray = JSONArray(responseBody) // Получить JSONArray вместо JSONObject
            val tasks = mutableListOf<Task>()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val task = Task(
                    uid = jsonObject.getInt("uid"),
                    creator = jsonObject.getString("creator"),
                    creatorRole = jsonObject.getString("creatorRole"),
                    executor = jsonObject.getString("executor"),
                    executorRole = jsonObject.getString("executorRole"),
                    description = jsonObject.getString("description"),
                    deadlines = jsonObject.getString("deadlines"),
                    status = jsonObject.getString("status"),
                    header = jsonObject.getString("header"),
                    priority = jsonObject.getString("priority")
                )
                tasks.add(task)
            }

            taskList = tasks
        } else {
            val errorMessage = response.message
            showToast(context, "Error: $errorMessage")
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxHeight()
            .fillMaxWidth()
            .background(Color(0xFFC7F1E8))
            .verticalScroll(rememberScrollState()),
        color = Color(0xFFC7F1E8),
        shape = RoundedCornerShape(5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .background(Color(0xFF71AEB2)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Описание задачи",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            for (task in taskList) {
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Заголовок: ${task.header}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "От кого: ${task.creator}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "Должность: ${task.creatorRole}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "Исполнитель: ${task.executor}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "Должность: ${task.executorRole}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "Описание задачи: ${task.description}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "Срок выполнения(включительно): ${task.deadlines}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        "Статус задачи: ${task.status}", modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        color = Color.White,
                        style = TextStyle(fontStyle = FontStyle.Normal)
                    )
                    Text(
                        text = "Приоритет задачи: ${task.priority}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Start)
                            .background(color = Color(0xFF71AEB2)),
                        fontSize = 20.sp,
                        style = TextStyle(fontStyle = FontStyle.Normal),
                        color = priorityColor(task.priority)
                    )
                    Button(
                        onClick = {
                            updateCompleteDate(task.uid, context)
                            navController.popBackStack()
                        },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(300.dp)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            Color(0xFFC7F1E8) // Цвет фона кнопки
                        ),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text(text = "Выполнить", color = Color.Black)
                    }
                }
            }
        }
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

private fun updateCompleteDate(uid: Int, context: Context) {
    val json = JSONObject().apply {
        put("uid", uid)
    }
    val requestBody =
        json.toString().toRequestBody("application/json".toMediaTypeOrNull())
    val request = Request.Builder()
        .url("http://192.168.1.47:8080/tasks/updateCompleteDate")
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    GlobalScope.launch(Dispatchers.IO) {
        val response = withContext(Dispatchers.IO) {
            client.newCall(request).execute()
        }

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody)
            val requestStatus = jsonObject.getBoolean("request")
            val message = if (requestStatus) "Success" else "Update failed"

            println(responseBody)
        } else {
            val errorMessage = response.message
            println(response.body.toString())
        }
    }
}