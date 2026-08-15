package com.example.quizprototype_phy_che_deepseek

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.services.classroom.ClassroomScopes
import com.google.api.services.drive.DriveScopes

@Composable
fun PhysicsChemApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    // 1. إعدادات تسجيل الدخول والصلاحيات
    val requiredScopes = listOf(
        Scope(ClassroomScopes.CLASSROOM_COURSES_READONLY),
        Scope(ClassroomScopes.CLASSROOM_COURSEWORK_ME),
        Scope(ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS),
        Scope(ClassroomScopes.CLASSROOM_ANNOUNCEMENTS_READONLY),
        Scope("https://www.googleapis.com/auth/classroom.courseworkmaterials.readonly"),
        Scope("https://www.googleapis.com/auth/classroom.topics.readonly"),
        Scope("https://www.googleapis.com/auth/classroom.profile.emails"),
        Scope(DriveScopes.DRIVE_READONLY)
    )

    val googleSignInClient: GoogleSignInClient = remember {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .apply { requiredScopes.forEach { requestScopes(it) } }
            .build()
        GoogleSignIn.getClient(context, signInOptions)
    }

    var isLoggedIn by remember { mutableStateOf(false) }
    var googleAccount by remember { mutableStateOf<GoogleSignInAccount?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    // 2. محاولة تسجيل الدخول التلقائي
    LaunchedEffect(Unit) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null && account.grantedScopes.containsAll(requiredScopes)) {
                googleAccount = account
                isLoggedIn = true
            }
        } catch (_: Exception) { }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    val missing = requiredScopes.filter { !account.grantedScopes.contains(it) }
                    if (missing.isNotEmpty()) {
                        signInError = "يجب الموافقة على جميع الصلاحيات ليعمل التطبيق."
                        googleSignInClient.signOut()
                    } else {
                        googleAccount = account
                        isLoggedIn = true
                    }
                }
                isSigningIn = false
            } catch (e: ApiException) {
                isSigningIn = false
                signInError = "خطأ في تسجيل الدخول. تأكد من إعدادات حسابك."
            }
        } else {
            isSigningIn = false
        }
    }

    // 3. منطق عرض الواجهات بناءً على الحالة
    if (!isLoggedIn) {
        LoginScreen(
            isLoading = isSigningIn,
            errorMessage = signInError,
            onLoginClick = {
                isSigningIn = true
                signInError = null
                launcher.launch(googleSignInClient.signInIntent)
            }
        )
    } else {
        val isTeacher = BuildConfig.APP_TYPE == "TEACHER"
        var isActivated by remember { mutableStateOf(SecurityManager.isActivated(context)) }
        var isPrivacyAccepted by remember { mutableStateOf(SecurityManager.isPrivacyAccepted(context)) }
        var showTeacherTools by remember { mutableStateOf(isTeacher) }

        if (!isPrivacyAccepted && !isTeacher) {
            PrivacyPolicyScreen(onAccept = {
                SecurityManager.setPrivacyAccepted(context)
                isPrivacyAccepted = true
            })
        } else if (showTeacherTools) {
            TeacherToolsScreen(
                googleAccount = googleAccount,
                onLogout = {
                    scope.launch {
                        googleSignInClient.signOut().addOnCompleteListener {
                            // مسح شامل لكل الحالات للبدء من جديد
                            isLoggedIn = false
                            googleAccount = null
                            showTeacherTools = false
                            signInError = null
                        }
                    }
                },
                onEnterApp = { showTeacherTools = false }
            )
        } else if (!isActivated && !isTeacher) {
            ActivationScreen(
                onActivated = { isActivated = true },
                onLogout = {
                    scope.launch {
                        googleSignInClient.signOut().addOnCompleteListener {
                            isLoggedIn = false
                            googleAccount = null
                        }
                    }
                }
            )
        } else {
            // واجهة التطبيق الرئيسية (صفي، دروسي، كويزاتي)
            MainContentArea(
                googleAccount = googleAccount!!,
                onLogout = {
                    googleSignInClient.signOut().addOnCompleteListener {
                        isLoggedIn = false
                        googleAccount = null
                    }
                }
            )
        }
    }
}

@Composable
fun MainContentArea(googleAccount: GoogleSignInAccount, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    label = { Text("صفي") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Book, contentDescription = null) },
                    label = { Text("دروسي") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                    label = { Text("كويزاتي") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> ClassroomScreen(googleAccount = googleAccount, onLogout = onLogout)
                1 -> MyMaterialsScreen()
                2 -> MyQuizzesScreen()
            }
        }
    }
}
