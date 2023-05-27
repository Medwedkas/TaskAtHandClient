package com.example.myapplication

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
import com.example.myapplication.feature_tasks.AdminTasksPage
import com.example.myapplication.feature_tasks.TaskDescriptionScreen
import com.example.myapplication.feature_tasks.TaskForm
import com.example.myapplication.feature_tasks.TasksPage
import com.example.myapplication.ui.theme.MyApplicationTheme


class MainActivity : ComponentActivity() {
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
        composable("create_tasks") {
            TaskForm(navController)
        }
        composable(
            "taskDescription/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.IntType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getInt("taskId")
            taskId?.let { TaskDescriptionScreen(it, navController) }
        }
    }
}