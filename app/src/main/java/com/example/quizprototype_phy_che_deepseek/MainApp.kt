//package com.example.quizprototype_phy_che_deepseek
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Book
//import androidx.compose.material.icons.filled.Quiz
//import androidx.compose.material.icons.filled.School
//import androidx.compose.material3.Icon
//import androidx.compose.material3.NavigationBar
//import androidx.compose.material3.NavigationBarItem
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableIntStateOf
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.navigation.compose.rememberNavController
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//
////import com.google.android.gms.auth.api.signin.GoogleSignInAccount
//
//// MainScreen.kt - الهيكل الرئيسي
//@Composable
//fun MainApp() {
////    val context = LocalContext.current
//    val navController = rememberNavController()
//
//    // حالة تسجيل الدخول
//    var isLoggedIn by remember { mutableStateOf(false) }
//    var googleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
//
//    if (!isLoggedIn) {
//        LoginScreen(
//            isLoading = isSigningIn,
//            errorMessage = signInError,
//            onLoginClick = {
//                isSigningIn = true
//                signInError = null
//                val signInIntent = googleSignInClient.signInIntent
//                launcher.launch(signInIntent)
//            }
//        )
//    } else {
//        // التطبيق الرئيسي
//        MainScaffold(
//            googleAccount = googleAccount!!,
//            navController = navController,
//            onLogout = {
//                isLoggedIn = false
//                googleAccount = null
//            }
//        )
//    }
//}
//
//@Composable
//fun MainScaffold(
//    googleAccount: GoogleSignInAccount,
//    navController: androidx.navigation.NavHostController,
//    onLogout: () -> Unit
//) {
//    var selectedTab by remember { mutableIntStateOf(0) }
//
//    Scaffold(
//        bottomBar = {
//            NavigationBar {
//                NavigationBarItem(
//                    selected = selectedTab == 0,
//                    onClick = { selectedTab = 0 },
//                    icon = { Icon(Icons.Default.School, contentDescription = null) },
//                    label = { Text("صفي") }
//                )
//                NavigationBarItem(
//                    selected = selectedTab == 1,
//                    onClick = { selectedTab = 1 },
//                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
//                    label = { Text("دروسي وتماريني") }
//                )
//                NavigationBarItem(
//                    selected = selectedTab == 2,
//                    onClick = { selectedTab = 2 },
//                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
//                    label = { Text("كويزاتي") }
//                )
//            }
//        }
//    ) { padding ->
//        Box(modifier = Modifier.padding(padding)) {
//            when (selectedTab) {
//                0 -> ClassroomScreen(googleAccount = googleAccount)
//                1 -> MyMaterialsScreen()
//                2 -> MyQuizzesScreen(
//                    onStartQuiz = { quiz ->
//                        // الانتقال إلى شاشة الكويز
//                        navController.navigate("quiz_player/${quiz.id}")
//                    }
//                )
//            }
//        }
//    }
//}