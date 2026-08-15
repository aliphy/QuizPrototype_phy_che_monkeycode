package com.example.quizprototype_phy_che_deepseek

//package com.yourapp.physicschem.network

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.classroom.Classroom
import com.google.api.services.classroom.ClassroomScopes
import com.google.api.services.classroom.model.*
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.example.quizprototype_phy_che_deepseek.CourseTopic
import com.example.quizprototype_phy_che_deepseek.MaterialType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class ClassroomService(private val context: Context) {

    private fun getCredential(account: GoogleSignInAccount): GoogleAccountCredential {
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(
                ClassroomScopes.CLASSROOM_COURSES_READONLY,
                ClassroomScopes.CLASSROOM_COURSEWORK_ME,
                ClassroomScopes.CLASSROOM_COURSEWORK_STUDENTS,
                "https://www.googleapis.com/auth/classroom.courseworkmaterials.readonly",
                "https://www.googleapis.com/auth/classroom.topics.readonly",
                "https://www.googleapis.com/auth/classroom.profile.emails",
                DriveScopes.DRIVE_READONLY,
            ),
        ).apply {
            selectedAccount = account.account
        }
    }

    // جلب قائمة الفصول المشترك بها المستخدم بصفته تلميذاً فقط
    suspend fun getEnrolledCourses(account: GoogleSignInAccount): Result<List<Course>> {
        return withContext(Dispatchers.IO) {
            try {
                val service = getClassroomService(account)
                
                // 1. جلب بيانات الملف الشخصي للمستخدم لمعرفة معرّفه الرقمي
                val myId = try {
                    val userProfile = service.userProfiles().get("me").execute()
                    userProfile.id
                } catch (e: Exception) {
                    android.util.Log.e("CLASSROOM", "Failed to get my profile ID", e)
                    null // إذا فشل جلب المعرف، قد نفشل في الفلترة بدقة
                }

                // 2. جلب قائمة الفصول
                val response = service.courses().list()
                    .setCourseStates(listOf("ACTIVE"))
                    .execute()
                
                val allCourses = response.courses ?: emptyList()
                
                // 3. تصفية الفصول: إذا كان لدينا معرفنا الشخصي، نستبعد الفصول التي نملكها
                val studentCourses = if (myId != null) {
                    allCourses.filter { it.ownerId != myId }
                } else {
                    allCourses // إذا فشلنا في جلب المعرف، نعرض الكل بدلاً من إظهار شاشة بيضاء
                }
                
                android.util.Log.d("CLASSROOM", "User ID: $myId, Total: ${allCourses.size}, Student: ${studentCourses.size}")
                Result.success(studentCourses)
            } catch (e: Exception) {
                e.printStackTrace()
                val errorMsg = when {
                    e.message?.contains("403") == true -> "تحتاج للموافقة على جميع صلاحيات Classroom عند تسجيل الدخول. اضغط على زر التبديل وحاول مجدداً."
                    e.message?.contains("401") == true -> "انتهت صلاحية الجلسة. أعد تسجيل الدخول."
                    else -> e.localizedMessage ?: "فشل جلب البيانات من جوجل"
                }
                Result.failure(Exception(errorMsg))
            }
        }
    }

    private fun getClassroomService(account: GoogleSignInAccount): Classroom {
        val credential = getCredential(account)
        return Classroom.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("PhysicsChemApp")
            .build()
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = getCredential(account)
        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName("PhysicsChemApp")
            .build()
    }

    // جلب مواد الفصل
//    suspend fun fetchClassroomData(
//        account: GoogleSignInAccount,
//        courseId: String
//    ): Result<List<CourseTopic>> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val classroomService = getClassroomService(account)
//
//                // جلب المواد الدراسية
//                val materials = classroomService.courses()
//                    .courseWorkMaterials()
//                    .list(courseId)
//                    .execute()
//
//                // جلب الواجبات
//                val coursework = classroomService.courses()
//                    .courseWork()
//                    .list(courseId)
//                    .execute()
//
//                val allMaterials = mutableListOf<ClassroomMaterial>()
//
//                // معالجة المواد الدراسية
//                materials.courseWorkMaterial?.forEach { material ->
//                    material.materials?.forEach { googleMaterial ->
//                        val classroomMaterial = convertToClassroomMaterial(
//                            googleMaterial = googleMaterial,
//                            materialId = material.id ?: "",
//                            title = material.title ?: "بدون عنوان"
//                        )
//                        allMaterials.add(classroomMaterial)
//                    }
//                }
//
//                // معالجة الواجبات
//                coursework.courseWork?.forEach { work ->
//                    work.materials?.forEach { googleMaterial ->
//                        val classroomMaterial = convertToClassroomMaterial(
//                            googleMaterial = googleMaterial,
//                            materialId = work.id ?: "",
//                            title = work.title ?: "بدون عنوان",
//                            isAssignment = true
//                        )
//                        allMaterials.add(classroomMaterial)
//                    }
//                }
//
//                // تنظيم المواد في موضوعات (حسب معرف المادة)
//                val topics = allMaterials
//                    .groupBy { it.topicId }
//                    .map { (topicId, materials) ->
//                        CourseTopic(
//                            id = topicId,
//                            name = materials.first().topicName,
//                            materials = materials
//                        )
//                    }
//
//                Result.success(topics)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Result.failure(Exception("فشل في جلب بيانات الفصل: ${e.message}"))
//            }
//        }
//    }
//    @Suppress("DEPRECATION")
//    suspend fun fetchClassroomData(
//        account: GoogleSignInAccount,
//        courseIdOrCode: String
//    ): Result<List<CourseTopic>> {
//        return withContext(Dispatchers.IO) {
//            try {
//                // إذا كان المدخل قصيراً (مثل y2ad6v6s) نحوله إلى Course ID
//                val courseId = if (courseIdOrCode.length < 15) {
//                    getCourseIdByCode(account, courseIdOrCode)
//                        ?: throw Exception("لم يتم العثور على الفصل بهذا الرمز")
//                } else {
//                    courseIdOrCode
//                }
//
//                val classroomService = getClassroomService(account)
//
//                // جلب المواد الدراسية
//                val materials = classroomService.courses()
//                    .courseWorkMaterials()
//                    .list(courseId)
//                    .execute()
//
//                // جلب الواجبات
//                val coursework = classroomService.courses()
//                    .courseWork()
//                    .list(courseId)
//                    .execute()
//
//                val allMaterials = mutableListOf<ClassroomMaterial>()
//
//                // معالجة المواد الدراسية
//                materials.courseWorkMaterial?.forEach { material ->
//                    material.materials?.forEach { googleMaterial ->
//                        val classroomMaterial = convertToClassroomMaterial(
//                            googleMaterial = googleMaterial,
//                            materialId = material.id ?: "",
//                            title = material.title ?: "بدون عنوان"
//                        )
//                        allMaterials.add(classroomMaterial)
//                    }
//                }
//
//                // معالجة الواجبات
//                coursework.courseWork?.forEach { work ->
//                    work.materials?.forEach { googleMaterial ->
//                        val classroomMaterial = convertToClassroomMaterial(
//                            googleMaterial = googleMaterial,
//                            materialId = work.id ?: "",
//                            title = work.title ?: "بدون عنوان",
//                            isAssignment = true
//                        )
//                        allMaterials.add(classroomMaterial)
//                    }
//                }
//
//                // تنظيم المواد في موضوعات
//                val topics = allMaterials
//                    .groupBy { it.topicId }
//                    .map { (topicId, materials) ->
//                        CourseTopic(
//                            id = topicId,
//                            name = materials.first().topicName,
//                            materials = materials
//                        )
//                    }
//
//                Result.success(topics)
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Result.failure(Exception("فشل في جلب بيانات الفصل: ${e.message}"))
//            }
//        }
//    }
//    @Suppress("DEPRECATION")
//    suspend fun fetchClassroomData(
//        account: GoogleSignInAccount,
//        courseIdOrCode: String
//    ): Result<List<CourseTopic>> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val classroomService = getClassroomService(account)
//
//                // جلب المواد الدراسية
//                val materials = classroomService.courses()
//                    .courseWorkMaterials()
//                    .list(courseIdOrCode)
//                    .execute()
//
//                // جلب الواجبات
//                val coursework = classroomService.courses()
//                    .courseWork()
//                    .list(courseIdOrCode)
//                    .execute()
//
//                val allMaterials = mutableListOf<ClassroomMaterial>()
//
//                // معالجة المواد الدراسية
//                materials.courseWorkMaterial?.forEach { material ->
//                    material.materials?.forEach { googleMaterial ->
//                        val classroomMaterial = convertToClassroomMaterial(
//                            googleMaterial = googleMaterial,
//                            materialId = material.id ?: "",
//                            title = material.title ?: "بدون عنوان"
//                        )
//                        allMaterials.add(classroomMaterial)
//                    }
//                }
//
//                // معالجة الواجبات
//                coursework.courseWork?.forEach { work ->
//                    work.materials?.forEach { googleMaterial ->
//                        val classroomMaterial = convertToClassroomMaterial(
//                            googleMaterial = googleMaterial,
//                            materialId = work.id ?: "",
//                            title = work.title ?: "بدون عنوان",
//                            isAssignment = true
//                        )
//                        allMaterials.add(classroomMaterial)
//                    }
//                }
//
//                // تنظيم المواد في موضوعات
//                val topics = allMaterials
//                    .groupBy { it.topicId }
//                    .map { (topicId, materials) ->
//                        CourseTopic(
//                            id = topicId,
//                            name = materials.first().topicName,
//                            materials = materials
//                        )
//                    }
//
//                if (topics.isEmpty()) {
//                    Result.failure(Exception("لا توجد مواد في هذا الفصل"))
//                } else {
//                    Result.success(topics)
//                }
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Result.failure(Exception("فشل في جلب بيانات الفصل: ${e.message}"))
//            }
//        }
//    }
    @Suppress("DEPRECATION")
    suspend fun fetchClassroomData(
        account: GoogleSignInAccount,
        courseIdOrCode: String
    ): Result<List<CourseTopic>> {
        return withContext(Dispatchers.IO) {
            try {
                val classroomService = getClassroomService(account)
                
                // 1. محاولة معرفة ما إذا كان المدخل هو ID أم رمز (الرموز قصيرة عادة)
                val courseId = if (courseIdOrCode.length < 15) {
                    getCourseIdByCode(account, courseIdOrCode) ?: courseIdOrCode
                } else {
                    courseIdOrCode
                }

                android.util.Log.d("CLASSROOM", "Fetching data for Course ID: $courseId")

                // جلب المواضيع الأصلية للفصل (مع حماية ضد خطأ 403)
                val topicMap = try {
                    val topicsResponse = classroomService.courses().topics().list(courseId).execute()
                    topicsResponse.topic?.associate { it.topicId to it.name } ?: emptyMap()
                } catch (e: Exception) {
                    android.util.Log.e("CLASSROOM", "Failed to fetch topics, using default names", e)
                    emptyMap<String, String>()
                }

                val allMaterials = mutableListOf<ClassroomMaterial>()

                // محاولة جلب المواد الدراسية
                try {
                    val materials = classroomService.courses()
                        .courseWorkMaterials()
                        .list(courseId)
                        .execute()

                    materials.courseWorkMaterial?.forEach { material ->
                        material.materials?.forEach { googleMaterial ->
                            val classroomMaterial = convertToClassroomMaterial(
                                googleMaterial = googleMaterial,
                                materialId = material.id ?: "",
                                title = material.title ?: "بدون عنوان",
                                topicId = material.topicId,
                                topicName = topicMap[material.topicId] ?: "مواد عامة"
                            )
                            allMaterials.add(classroomMaterial)
                        }
                    }
                } catch (e: Exception) {
                    // تجاهل خطأ المواد الدراسية
                    android.util.Log.d("FETCH_DEBUG", "لا توجد مواد دراسية: ${e.message}")
                }

                // محاولة جلب الواجبات
                try {
                    val coursework = classroomService.courses()
                        .courseWork()
                        .list(courseId)
                        .execute()

                    coursework.courseWork?.forEach { work ->
                        work.materials?.forEach { googleMaterial ->
                            val classroomMaterial = convertToClassroomMaterial(
                                googleMaterial = googleMaterial,
                                materialId = work.id ?: "",
                                title = work.title ?: "بدون عنوان",
                                isAssignment = true,
                                topicId = work.topicId,
                                topicName = topicMap[work.topicId] ?: "واجبات عامة"
                            )
                            allMaterials.add(classroomMaterial)
                        }
                    }
                } catch (e: Exception) {
                    // تجاهل خطأ الواجبات
                    android.util.Log.d("FETCH_DEBUG", "لا توجد واجبات: ${e.message}")
                }

                if (allMaterials.isEmpty()) {
                    Result.failure(Exception("الفصل موجود لكن لا يحتوي على مواد قابلة للتحميل حالياً."))
                } else {
                    val topics = allMaterials
                        .groupBy { it.topicId }
                        .map { (topicId, materials) ->
                            CourseTopic(
                                id = topicId ?: "unassigned",
                                name = materials.first().topicName,
                                materials = materials
                            )
                        }
                    Result.success(topics)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("فشل في جلب بيانات الفصل: ${e.message}"))
            }
        }
    }
    // تحويل مادة Google إلى مادة التطبيق
    private fun convertToClassroomMaterial(
        googleMaterial: Material,
        materialId: String,
        title: String,
        isAssignment: Boolean = false,
        topicId: String? = null,
        topicName: String = "غير مصنف"
    ): ClassroomMaterial {
        val type = when {
            googleMaterial.driveFile?.driveFile != null -> {
                val fileName = googleMaterial.driveFile.driveFile.title ?: ""
                when {
                    fileName.contains("كويز", ignoreCase = true) ||
                            fileName.contains("quiz", ignoreCase = true) -> MaterialType.QUIZ
                    fileName.contains("واجب", ignoreCase = true) || isAssignment -> MaterialType.ASSIGNMENT
                    fileName.contains("اختبار", ignoreCase = true) -> MaterialType.EXAM_TASK
                    fileName.contains("سؤال", ignoreCase = true) ||
                            fileName.contains("question", ignoreCase = true) -> MaterialType.QUESTION
                    else -> MaterialType.MATERIAL
                }
            }
            else -> MaterialType.OTHER
        }

        return ClassroomMaterial(
            id = "${materialId}_${System.currentTimeMillis()}",
            title = title,
            type = type,
            driveFileId = googleMaterial.driveFile?.driveFile?.id,
            driveUrl = googleMaterial.driveFile?.driveFile?.alternateLink,
            topicId = topicId ?: "default",
            topicName = topicName,
            fileSize = (googleMaterial.driveFile?.driveFile?.size ?: 0).toLong(),
            mimeType = null // سيتم اكتشافه عند الفتح أو استخدامه من الاسم
        )
    }

    suspend fun getCourseAnnouncements(googleAccount: GoogleSignInAccount, courseId: String): Result<List<com.google.api.services.classroom.model.Announcement>> {
        return withContext(Dispatchers.IO) {
            try {
                val service = getClassroomService(googleAccount)
                val response = service.courses().announcements().list(courseId)
                    .setOrderBy("updateTime desc")
                    .execute()
                Result.success(response.announcements ?: emptyList())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // تحميل ملف مع كشف النوع الحقيقي من جوجل
    suspend fun downloadMaterial(
        account: GoogleSignInAccount,
        material: ClassroomMaterial,
        onProgress: (Float) -> Unit
    ): Result<Pair<String, String?>> { // نرجع المسار ونوع الملف
        return withContext(Dispatchers.IO) {
            try {
                val driveService = getDriveService(account)
                val fileId = material.driveFileId ?: return@withContext Result.failure(Exception("معرف الملف مفقود"))

                // 1. جلب معلومات الملف الحقيقية من Drive API (الاسم والنوع)
                val driveFile = driveService.files().get(fileId)
                    .setFields("name, mimeType")
                    .execute()
                
                val realMimeType = driveFile.mimeType
                val realName = driveFile.name

                // 2. إنشاء مجلد للمادة
                val downloadDir = File(context.filesDir, "downloads/${material.type.name}")
                downloadDir.mkdirs()

                // التأكد من وجود الامتداد في الاسم المحفوظ
                val finalFileName = if (realName.contains(".")) realName else {
                    val ext = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(realMimeType)
                    if (ext != null) "$realName.$ext" else realName
                }

                val file = File(downloadDir, "${material.id}_$finalFileName")

                // 3. تحميل الملف
                onProgress(0.1f)
                driveService.files().get(fileId)
                    .executeMediaAndDownloadTo(FileOutputStream(file))
                onProgress(1.0f)

                Result.success(Pair(file.absolutePath, realMimeType))

            } catch (e: Exception) {
                e.printStackTrace()
                Result.failure(Exception("فشل في تحميل الملف: ${e.message}"))
            }
        }
    }
    // تحويل رمز الفصل إلى Course ID
//    suspend fun getCourseIdByCode(
//        account: GoogleSignInAccount,
//        classCode: String
//    ): String? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val classroomService = getClassroomService(account)
//
//                // البحث عن الفصل برمز معين (ليس مدعوم مباشرة)
//                // لذلك نجلب كل الفصول ونبحث عن المطابق
//                val courses = classroomService.courses()
//                    .list()
//                    .setCourseStates(listOf("ACTIVE"))
//                    .execute()
//
//                courses.courses?.find { it.enrollmentCode == classCode }?.id
//            } catch (e: Exception) {
//                null
//            }
//        }
//    }
//    suspend fun getCourseIdByCode(
//        account: GoogleSignInAccount,
//        classCode: String
//    ): String? {
//
//        return withContext(Dispatchers.IO) {
//            try {
//                val classroomService = getClassroomService(account)
//                val courses = classroomService.courses()
//                    .list()
//                    .setCourseStates(listOf("ACTIVE"))
//                    .execute()
//
//                courses.courses?.find {
//                    it.enrollmentCode.equals(classCode, ignoreCase = true)
//                }?.id
//            } catch (e: Exception) {
//                e.printStackTrace()
//
//
//                null
//            }
//        }
//    }
//    suspend fun getCourseIdByCode(
//        account: GoogleSignInAccount,
//        classCode: String
//    ): String? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val classroomService = getClassroomService(account)
//
//                // نجلب كل الفصول النشطة
//                val courses = classroomService.courses()
//                    .list()
//                    .setCourseStates(listOf("ACTIVE"))
//                    .execute()
//                android.util.Log.d("DEBUG_CLASS", "الفصل: ${course.name}, الرمز: ${course.enrollmentCode}")
//                // نبحث عن الفصل الذي رمزه مطابق
//                for (course in courses.courses ?: emptyList()) {
//                    if (course.enrollmentCode != null &&
//                        course.enrollmentCode.equals(classCode, ignoreCase = true)) {
//                        return@withContext course.id
//                    }
//                }
//
//                null
//            } catch (e: Exception) {
//                e.printStackTrace()
//                null
//            }
//        }
//    }
//    suspend fun getCourseIdByCode(
//        account: GoogleSignInAccount,
//        classCode: String
//    ): String? {
//        return withContext(Dispatchers.IO) {
//            try {
//                val classroomService = getClassroomService(account)
//
//                val courses = classroomService.courses()
//                    .list()
//                    .setCourseStates(listOf("ACTIVE"))
//                    .execute()
//
//                android.util.Log.d("DEBUG_CLASS", "عدد الفصول: ${courses.courses?.size ?: 0}")
//
//                for (course in courses.courses ?: emptyList()) {
//                    android.util.Log.d("DEBUG_CLASS", "الفصل: ${course.name}, الرمز: ${course.enrollmentCode}")
//
//                    if (course.enrollmentCode != null &&
//                        course.enrollmentCode.equals(classCode, ignoreCase = true)) {
//                        android.util.Log.d("DEBUG_CLASS", "تم العثور على الفصل: ${course.id}")
//                        return@withContext course.id
//                    }
//                }
//
//                android.util.Log.d("DEBUG_CLASS", "لم يتم العثور على فصل بالرمز: $classCode")
//                null
//            } catch (e: Exception) {
//                e.printStackTrace()
//                android.util.Log.d("DEBUG_CLASS", "خطأ: ${e.message}")
//                null
//            }
//        }
//    }
    suspend fun getCourseIdByCode(
        account: GoogleSignInAccount,
        classCode: String
    ): String? {
        return withContext(Dispatchers.IO) {
            try {
                val classroomService = getClassroomService(account)
                
                // جلب قائمة الفصول
                val response = classroomService.courses()
                    .list()
                    .setCourseStates(listOf("ACTIVE"))
                    .execute()

                val courses = response.courses ?: emptyList()
                
                android.util.Log.d("CLASSROOM", "Found ${courses.size} active courses")

                // 1. البحث المطابق للمعرف (إذا أدخل المستخدم المعرف الطويل مباشرة)
                courses.find { it.id == classCode }?.let { return@withContext it.id }

                // 2. البحث عن الرمز (يعمل غالباً للمعلمين فقط لأن الطلاب قد لا يرون الرمز في الـ API)
                for (course in courses) {
                    val code = course.enrollmentCode
                    if (code != null && code.equals(classCode, ignoreCase = true)) {
                        android.util.Log.d("CLASSROOM", "Matched by enrollment code: ${course.id}")
                        return@withContext course.id
                    }
                }

                // 3. إذا لم نجد شيئاً وكان المستخدم لديه فصل واحد فقط، ربما هذا هو المطلوب؟
                // (اختياري: يمكن تفعيل هذا الخيار إذا كنت متأكداً أن الطالب لن يكون في أكثر من فصل)
                // if (courses.size == 1) return@withContext courses[0].id

                null
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}