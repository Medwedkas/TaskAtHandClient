package com.example.myapplication

import QRScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.feature_messanger.ChatsScreen
import com.example.myapplication.feature_messanger.MessagesScreen
import com.example.myapplication.feature_stat.StatScreen
import com.example.myapplication.feature_stat.WorkersListPage
import com.example.myapplication.feature_tasks.AdminTasksPage
import com.example.myapplication.feature_tasks.TaskDescriptionScreen
import com.example.myapplication.feature_tasks.TaskForm
import com.example.myapplication.feature_tasks.TasksInspectorScreen
import com.example.myapplication.feature_tasks.TasksPage
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
    object ApiConfig {
        const val BASE_URL = "http://192.168.1.49:8080/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val startPage: String = if (UserManager.user?.uid != null) "task" else "login"
    NavHost(navController = navController, startDestination = startPage) {
        composable("login") {
            LoginPage(navController)
        }
        composable("task") {
            TasksPage(navController)
        }
        composable("admin_tasks") {
            AdminTasksPage(navController)
        }
        composable("inspector_tasks") {
            TasksInspectorScreen(navController)
        }
        composable("create_tasks") {
            TaskForm(navController)
        }
        composable("chats"){
            ChatsScreen(navController)
        }
        composable("workers_list"){
            WorkersListPage(navController)
        }
        composable(route = "qr"){
            QRScreen(navController)
        }
        composable(
            "taskDescription/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            taskId?.let { TaskDescriptionScreen(it, navController) }
        }
        composable(
            "allMessages/{receiver_Id}",
            arguments = listOf(navArgument("receiver_Id") { type = NavType.IntType })
        ) { backStackEntry ->
            val receiver_Id = backStackEntry.arguments?.getInt("receiver_Id")
            receiver_Id?.let { MessagesScreen(it, navController) }
        }
        composable(
            "user_stat/{user_id}",
            arguments = listOf(navArgument("user_id") { type = NavType.IntType })
        ) { backStackEntry ->
            val user_id = backStackEntry.arguments?.getInt("user_id")
            user_id?.let { StatScreen(it, navController) }
        }
    }
}

