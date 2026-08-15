package com.example.quizprototype_phy_che_deepseek

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.api.client.json.gson.GsonFactory
import java.io.File
import java.io.FileOutputStream

// DownloadManager.kt
class DownloadManager(private val context: Context) {

    // تحميل مادة من Google Drive
    suspend fun downloadMaterial(
        material: ClassroomMaterial,
        credential: GoogleAccountCredential,
        onProgress: (Float) -> Unit
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val driveService = Drive.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    GsonFactory.getDefaultInstance(),
                    credential
                ).setApplicationName("PhysicsChemApp")
                    .build()

                // تحديد مسار الحفظ
                val materialDir = getMaterialDirectory(material.type)
                val file = File(materialDir, material.title)

                // تحميل الملف
                driveService.files()
                    .get(material.driveFileId!!)
                    .executeMediaAndDownloadTo(FileOutputStream(file))

                // حفظ معلومات المادة في قاعدة البيانات المحلية
                val repository = MaterialRepository(context)
                repository.saveDownloadedMaterial(material, file.absolutePath)

                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    private fun getMaterialDirectory(type: MaterialType): File {
        val baseDir = File(context.filesDir, "downloaded_materials")
        val typeDir = File(baseDir, type.name.lowercase())
        typeDir.mkdirs()
        return typeDir
    }

    // تحميل جميع المواد المحملة
    suspend fun loadDownloadedMaterials(): List<CourseTopic> {
        return MaterialRepository(context).getMaterialsGroupedByTopic()
    }

    // تحميل الكويزات فقط
    suspend fun loadDownloadedQuizzes(): List<ClassroomMaterial> {
        return MaterialRepository(context).getAllQuizzes()
    }
}