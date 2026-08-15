package com.example.quizprototype_phy_che_deepseek

//// Models.kt
//data class CourseTopic(
//    val id: String,
//    val name: String,           // اسم الموضوع
//    val materials: List<ClassroomMaterial>
//)
//
//data class ClassroomMaterial(
//    val id: String,
//    val title: String,
//    val type: MaterialType,
//    val driveFileId: String?,
//    val driveUrl: String?,
//    val isDownloaded: Boolean = false,
//    val localFilePath: String? = null,
//    val fileSize: Long = 0
//)
//
//enum class MaterialType(val arabicName: String) {
//    ASSIGNMENT("واجب"),
//    EXAM_TASK("مهمة للاختبار"),
//    QUESTION("توجيه الأسئلة"),
//    MATERIAL("المواد"),
//    QUIZ("كويز"),
//    OTHER("أخرى")
//}
//
//// حالة التحميل
//data class DownloadState(
//    val materialId: String,
//    val progress: Float = 0f,
//    val status: DownloadStatus = DownloadStatus.NOT_STARTED
//)
//
//enum class DownloadStatus {
//    NOT_STARTED, DOWNLOADING, COMPLETED, ERROR
//}
//package com.yourapp.physicschem.data.model

import com.google.api.services.classroom.model.Material as GoogleMaterial

// أنواع المواد
enum class MaterialType(val arabicName: String, val arabicNamePlural: String) {
    ASSIGNMENT("واجب", "واجبات"),
    EXAM_TASK("مهمة للاختبار", "مهام الاختبار"),
    QUESTION("توجيه الأسئلة", "توجيهات الأسئلة"),
    MATERIAL("المواد", "المواد الدراسية"),
    QUIZ("كويز", "كويزات"),
    OTHER("أخرى", "أخرى")
}

// الموضوع الدراسي
data class CourseTopic(
    val id: String,
    val name: String,
    val materials: List<ClassroomMaterial>
)

// المادة التعليمية
data class ClassroomMaterial(
    val id: String,
    val title: String,
    val type: MaterialType,
    val driveFileId: String? = null,
    val driveUrl: String? = null,
    val isDownloaded: Boolean = false,
    val localFilePath: String? = null,
    val fileSize: Long = 0,
    val mimeType: String? = null,
    val topicId: String = "",
    val topicName: String = ""
)

// حالة التحميل
data class DownloadState(
    val materialId: String,
    val progress: Float = 0f,
    val status: DownloadStatus = DownloadStatus.NOT_STARTED
)

enum class DownloadStatus {
    NOT_STARTED,
    DOWNLOADING,
    COMPLETED,
    ERROR
}

// حالة الكويز
data class QuizInfo(
    val id: String,
    val localUrl: String,
    val folderPath: String
)