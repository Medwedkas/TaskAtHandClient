package com.example.myapplication.feature_tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.UserManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class CreateTaskData(
    val creator: Int,
    val creatorRole: Int,
    val executor: String,
    val description: String,
    val deadlines: String,
    val status: String,
    val header: String,
    val priority: String
)

private fun sendTaskData(taskData: CreateTaskData) {
    GlobalScope.launch(Dispatchers.IO) {
        val url = URL("http://192.168.1.47:8080/tasks/createTask")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("Accept", "application/json")
        connection.doOutput = true

        val requestBody = Json.encodeToString(taskData) // Сериализация в JSON
        println(requestBody)

        val outputStream = OutputStreamWriter(connection.outputStream)
        outputStream.write(requestBody)
        outputStream.flush()

        val responseCode = connection.responseCode
        outputStream.close()
        connection.disconnect()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskForm(navController: NavController) {
    var creator by remember { mutableStateOf("") }
    var creatorRole by remember { mutableStateOf("") }
    var executor by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var deadlines by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var header by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .background(color = Color(0xFFC7F1E8))
            .fillMaxHeight()
            .fillMaxWidth()
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = executor,
            onValueChange = { executor = it },
            label = { Text("Исполнитель") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = header,
            onValueChange = { header = it },
            label = { Text("Заголовок") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Описание задачи") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = deadlines,
            onValueChange = { deadlines = it },
            label = { Text("Срок выполнения(включительно)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = status,
            onValueChange = { status = it },
            label = { Text("Статус задачи") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = priority,
            onValueChange = { priority = it },
            label = { Text("Приоритет") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val taskData = CreateTaskData(
                    creator = UserManager.user!!.uid,
                    creatorRole = UserManager.user!!.role,
                    executor = executor,
                    description = description,
                    deadlines = deadlines,
                    status = status,
                    header = header,
                    priority = priority
                )

                sendTaskData(taskData)
                navController.popBackStack()
            }
        ) {
            Text("Создать задачу")
        }
    }
}