package com.example.quizprototype_phy_che_deepseek

import net.lingala.zip4j.ZipFile
import java.io.File

object QuizUtils {
    fun extractQuiz(zipPath: String, targetFolder: String): String? {
        return try {
            val destination = File(targetFolder)
            if (destination.exists()) {
                destination.deleteRecursively()
            }
            destination.mkdirs()
            
            val zipFile = ZipFile(zipPath)
            zipFile.extractAll(targetFolder)
            
            // محاولة جلب المجلد الصحيح الذي يحتوي على index.html
            val resultFolder = findIndexFolder(destination)
            android.util.Log.d("QUIZ_UTILS", "Extracted to: ${resultFolder?.absolutePath}")
            return resultFolder?.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // دالة للبحث عن المجلد الذي يحتوي على index.html بعمق
    private fun findIndexFolder(root: File): File? {
        if (File(root, "index.html").exists()) return root
        
        val files = root.listFiles()
        if (files != null) {
            for (file in files) {
                if (file.isDirectory) {
                    val found = findIndexFolder(file)
                    if (found != null) return found
                }
            }
        }
        return null
    }
}
