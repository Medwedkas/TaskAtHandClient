package com.example.myapplication.feature_messanger

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import com.example.myapplication.feature_tasks.NavigationBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class Chat(
    val receiverId: Int,
    val name: String,
    val post: String,
    val timestamp: String,
    val message: String
)

val chats = mutableStateListOf<Chat>()
private fun navigateToTasks(navController: NavController) {
    navController.navigate("task")
}

private fun parseChatListFromJson(json: String?): List<Chat> {
    val chatList = mutableListOf<Chat>()
    try {
        val jsonArray = JSONArray(json)
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val receiverId = jsonObject.getInt("receiver_id")
            val name = jsonObject.getString("name")
            val post = jsonObject.getString("post")
            val timestamp = jsonObject.getString("timestamp")
            val message = jsonObject.getString("message")
            val chat = Chat(receiverId, name, post, timestamp, message)
            chatList.add(chat)
        }
    } catch (e: JSONException) {
        e.printStackTrace()
    }
    return chatList
}

@Composable
fun ChatCard(chat: Chat, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { navController.navigate("allMessages/${chat.receiverId}") },
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = chat.name, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Должность: " + chat.post, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Последние сообщение было: " + chat.timestamp, style = MaterialTheme.typography.titleSmall)
        }
    }
}
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController) {
    val chatList: MutableState<List<Chat>> = remember { mutableStateOf(emptyList()) }

    LaunchedEffect(Unit) {
        chatList.value = fetchChats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Сообщения") },
                actions = {
                    IconButton(onClick = { /* Действие при нажатии на кнопку + */ }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Добавить")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = Color(0xFF3C3A3F),
            ) {
                NavigationBar(
                    navController = navController,
                    onButton1Click = {},
                    onButton2Click = { navigateToTasks(navController) },
                    onButton3Click = {}
                )
            }
        },
        content = {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFF323034)),
                contentPadding = PaddingValues(top = 55.dp)
            ) {
                items(chatList.value) { chat ->
                    ChatCard(chat = chat, navController = navController)
                }
            }
        }
    )
}

@Composable
fun ChatList(chats: List<Chat>, navController: NavController) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats) { chat ->
            ChatCard(chat = chat, navController = navController)
        }
    }
}



private suspend fun fetchChats(): List<Chat> = withContext(Dispatchers.IO) {
    val userId = UserManager.user?.uid ?: ""
    val url = MainActivity.ApiConfig.BASE_URL + "message/getChats"

    val requestBody = JSONObject().apply {
        put("uid", userId)
    }.toString().toRequestBody("application/json".toMediaTypeOrNull())

    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    suspendCoroutine { continuation ->
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                val chatList = parseChatListFromJson(json)
                continuation.resume(chatList)
            }
        })
    }
}