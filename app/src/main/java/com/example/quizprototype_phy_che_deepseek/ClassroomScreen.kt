package com.example.quizprototype_phy_che_deepseek

import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.services.classroom.model.Course
import kotlinx.coroutines.launch

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomScreen(
    googleAccount: GoogleSignInAccount,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val classroomService = remember { ClassroomService(context) }
    val materialRepository = remember { MaterialRepository(context) }

    // الآن نقرأ المعرف مباشرة من إعدادات النسخة (BuildConfig)
    val targetTeacherId = BuildConfig.TEACHER_ID

    var enrolledCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var courseTopics by remember { mutableStateOf<List<CourseTopic>>(emptyList()) }
    
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var downloadStates by remember { mutableStateOf<Map<String, DownloadState>>(emptyMap()) }

    // دالة جلب محتوى الفصل المختار
    fun loadCourseContent(course: Course) {
        selectedCourse = course
        isLoading = true
        scope.launch {
            classroomService.fetchClassroomData(googleAccount, course.id)
                .onSuccess { topics ->
                    courseTopics = topics
                    isLoading = false
                }
                .onFailure { error ->
                    errorMessage = error.message
                    isLoading = false
                }
        }
    }

    // فتح موقع Classroom الرسمي
    val openClassroomSite = { course: Course ->
        val baseUrl = course.alternateLink ?: "https://classroom.google.com/c/${course.id}"
        val email = googleAccount.email ?: ""
        val finalUrl = if (baseUrl.contains("?")) "$baseUrl&authuser=$email" else "$baseUrl?authuser=$email"
        try {
            val colorParams = CustomTabColorSchemeParams.Builder().setToolbarColor("#1565C0".toColorInt()).build()
            val intent = CustomTabsIntent.Builder().setDefaultColorSchemeParams(colorParams).setShowTitle(true).build()
            intent.launchUrl(context, finalUrl.toUri())
        } catch (_: Exception) {
            context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, finalUrl.toUri()))
        }
    }

    fun fetchCourses() {
        isLoading = true
        errorMessage = null
        scope.launch {
            classroomService.getEnrolledCourses(googleAccount).onSuccess { courses ->
                // 1. تحديد المعرف المستهدف (من النسخة الحالية)
                val targetId = targetTeacherId.trim()
                
                // 2. الفلترة مع تنظيف البيانات لضمان التطابق
                var filtered = if (targetId.isNotEmpty()) {
                    courses.filter { it.ownerId?.trim() == targetId }
                } else {
                    courses 
                }
                
                // --- نظام الحماية الاحتياطي (Fail-Safe) ---
                if (courses.isNotEmpty() && filtered.isEmpty()) {
                    filtered = courses // عرض الفصول المتاحة كحل احتياطي
                }

                enrolledCourses = filtered
                
                // 3. ميزة الدخول التلقائي الذكية
                if (enrolledCourses.size == 1) {
                    loadCourseContent(enrolledCourses[0])
                } else {
                    isLoading = false
                }
            }.onFailure { error ->
                errorMessage = error.localizedMessage
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchCourses()
    }

    Scaffold(
        topBar = {
            LogoTopAppBar(
                title =  selectedCourse?.name ?: "فصولي الدراسية"
                ,
                navigationIcon = {
                    // نظهر سهم الرجوع فقط إذا كان هناك أكثر من فصل مشترك فيه التلميذ
                    if (selectedCourse != null && enrolledCourses.size > 1) {
                        AppIconButton(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            onClick = {
                                selectedCourse = null
                                courseTopics = emptyList()
                            }
                        )
                    }
                }
                ,
                actions = {
                    if (selectedCourse != null) {
                        AppIconButton(
                            imageVector  = Icons.Default.School,
                            contentDescription = "رجوع",
                            tint = Color.White,
                            onClick = {
                                openClassroomSite(selectedCourse!!)
                            }
                        )


                    }
                    AppIconButton(
                        imageVector  = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "تبديل الحساب",
                        tint = Color.White,
                        onClick = onLogout
                    )
                },
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    ErrorView(message = errorMessage!!, onRetry = { fetchCourses() }, onLogout = onLogout)
                }
                selectedCourse == null -> {
                    // لا نظهر "القائمة فارغة" إلا إذا انتهى التحميل فعلياً ولم نجد فصولاً
                    if (!isLoading && enrolledCourses.isEmpty()) {
                        EmptyContent(message = "أنت غير مشترك في أي فصل فيزياء حالياً")
                    } else if (!isLoading) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(enrolledCourses) { course ->
                                CourseCard(course = course) { loadCourseContent(course) }
                            }
                        }
                    }
                }
                else -> {
                    if (courseTopics.isEmpty()) {
                        EmptyContent(message = "لا توجد مواد دراسية قابلة للتحميل في هذا الفصل")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item {
                                InfoBanner(
                                    message = "اضغط هنا لمتابعة ساحة المشاركات والمناقشات على موقع Classroom الرسمي",
                                    onClick = { openClassroomSite(selectedCourse!!) }
                                )
                            }
                            items(courseTopics) { topic ->
                                TopicSectionContent(
                                    topic = topic,
                                    downloadStates = downloadStates,
                                    onDownloadMaterial = { material ->
                                        scope.launch {
                                            downloadStates = downloadStates + (material.id to DownloadState(material.id, 0f, DownloadStatus.DOWNLOADING))
                                            classroomService.downloadMaterial(googleAccount, material) { progress ->
                                                downloadStates = downloadStates + (material.id to DownloadState(material.id, progress, DownloadStatus.DOWNLOADING))
                                            }.onSuccess { resultData ->
                                                val (localPath, mimeType) = resultData
                                                scope.launch {
                                                    val updatedMaterial = material.copy(mimeType = mimeType)
                                                    val finalPath = if (material.type == MaterialType.QUIZ) {
                                                        val quizRootFolder = context.filesDir.absolutePath + "/quizzes/${material.id}"
                                                        QuizUtils.extractQuiz(localPath, quizRootFolder)
                                                        quizRootFolder
                                                    } else {
                                                        localPath
                                                    }
                                                    materialRepository.saveDownloadedMaterial(updatedMaterial, finalPath)
                                                    downloadStates = downloadStates + (material.id to DownloadState(material.id, 1f, DownloadStatus.COMPLETED))
                                                    Toast.makeText(context, "تم التحميل بنجاح", Toast.LENGTH_SHORT).show()
                                                }
                                            }.onFailure {
                                                downloadStates = downloadStates + (material.id to DownloadState(material.id, 0f, DownloadStatus.ERROR))
                                                Toast.makeText(context, "فشل التحميل", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = MaterialTheme.shapes.medium, color = Color(0xFFE3F2FD)) {
                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.padding(12.dp), tint = Color(0xFF1565C0))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = course.name ?: "فصل بدون اسم", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!course.section.isNullOrEmpty()) {
                    Text(text = course.section, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = Color.LightGray)
        }
    }
}

@Composable
fun TopicSectionContent(topic: CourseTopic, downloadStates: Map<String, DownloadState>, onDownloadMaterial: (ClassroomMaterial) -> Unit) {
    Column {
        Text(text = topic.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0), modifier = Modifier.padding(bottom = 12.dp))
        val groupedMaterials = topic.materials.groupBy { it.type }
        groupedMaterials.forEach { (type, materials) ->
            Text(text = type.arabicNamePlural, style = MaterialTheme.typography.titleSmall, color = Color(0xFF424242), modifier = Modifier.padding(vertical = 8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(materials) { material ->
                    MaterialItemCard(material = material, downloadState = downloadStates[material.id], onDownload = { onDownloadMaterial(material) })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MaterialItemCard(material: ClassroomMaterial, downloadState: DownloadState?, onDownload: () -> Unit) {
    Card(modifier = Modifier.width(160.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = when (material.type) {
                    MaterialType.QUIZ -> Icons.Default.Quiz
                    MaterialType.ASSIGNMENT -> Icons.AutoMirrored.Filled.Assignment
                    MaterialType.EXAM_TASK -> Icons.AutoMirrored.Filled.Grading
                    MaterialType.QUESTION -> Icons.Default.QuestionAnswer
                    MaterialType.MATERIAL -> Icons.AutoMirrored.Filled.MenuBook
                    else -> Icons.Default.AttachFile
                },
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when (material.type) {
                    MaterialType.QUIZ -> Color(0xFF7B1FA2)
                    MaterialType.ASSIGNMENT -> Color(0xFFFF9800)
                    else -> Color(0xFF2196F3)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = material.title, style = MaterialTheme.typography.bodySmall, maxLines = 2, textAlign = TextAlign.Center, modifier = Modifier.heightIn(min = 32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            if (downloadState?.status == DownloadStatus.COMPLETED || material.isDownloaded) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
            } else if (downloadState?.status == DownloadStatus.DOWNLOADING) {
                LinearProgressIndicator(
                    progress = { downloadState.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF1565C0))
                }
            }
        }
    }
}

@Composable
fun InfoBanner(message: String, onClick: () -> Unit) {
    Surface(color = Color(0xFFE3F2FD), shape = MaterialTheme.shapes.small, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp).clickable { onClick() }) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text = message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF1565C0))
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, onLogout: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.ErrorOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.Red)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, textAlign = TextAlign.Center, color = Color.Red)
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) { Text("إعادة المحاولة") }
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text("تسجيل الخروج والتبديل") }
    }
}

@Composable
fun EmptyContent(message: String) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = Color.Gray)
    }
}
