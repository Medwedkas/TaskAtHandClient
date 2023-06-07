package com.example.myapplication.feature_messanger

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.MainActivity
import com.example.myapplication.UserManager
import com.example.myapplication.feature_tasks.NavigationBar
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

data class Chat(
    val receiverId: Int,
    val name: String,
    val post: String,
    val timestamp: String,
    val message: String
)

val chats = mutableStateListOf<Chat>()

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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(navController: NavController) {
    var chatList by remember { mutableStateOf(emptyList<Chat>()) }

    LaunchedEffect(Unit) {
        fetchChats { fetchedChatList ->
            chatList = fetchedChatList
        }
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
            NavigationBar(
                navController = navController,
                onButton1Click = {},
                onButton2Click = {},
                onButton3Click = {}
            )
        },
        content = {
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF29272B))) {
                LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 72.dp)) {
                    items(chatList) { chat ->
                        ChatCard(chat = chat)
                    }
                }
            }
        }
    )
}


private suspend fun fetchChats(callback: (List<Chat>) -> Unit) {
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

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // Handle the failure
        }

        override fun onResponse(call: Call, response: Response) {
            val json = response.body?.string()
            val chatList = parseChatListFromJson(json)
            callback(chatList)
        }
    })
}

@Composable
fun ChatList(chats: List<Chat>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(chats) { chat ->
            ChatCard(chat = chat)
        }
    }
}

@Composable
fun ChatCard(chat: Chat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium, // Укажите форму карточки
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = chat.name,
                color = Color(0xFF717379),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = chat.post,
                color = Color(0xFF717379),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
            Text(
                text = chat.timestamp,
                color = Color(0xFF717379),
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}