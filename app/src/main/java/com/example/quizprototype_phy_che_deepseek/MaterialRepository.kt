package com.example.quizprototype_phy_che_deepseek

//package com.yourapp.physicschem.data.repository

import android.content.Context
import com.example.quizprototype_phy_che_deepseek.MaterialEntity
import com.example.quizprototype_phy_che_deepseek.ClassroomMaterial
import com.example.quizprototype_phy_che_deepseek.CourseTopic
import com.example.quizprototype_phy_che_deepseek.MaterialType
import kotlinx.coroutines.flow.map

class MaterialRepository(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val materialDao = database.materialDao()

    // حفظ مادة محملة بشكل آمن
    suspend fun saveDownloadedMaterial(
        material: ClassroomMaterial,
        localFilePath: String,
    ) {
        val entity = MaterialEntity(
            id = material.id,
            title = material.title,
            type = material.type.name,
            localFilePath = localFilePath,
            topicId = material.topicId,
            topicName = material.topicName,
            fileSize = material.fileSize,
            mimeType = material.mimeType ?: getMimeTypeFromPath(localFilePath)
        )
        materialDao.insertMaterial(entity)
    }

    private fun getMimeTypeFromPath(path: String): String? {
        val extension = path.substringAfterLast('.', "").lowercase()
        return android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    // التحقق من تحميل مادة
    suspend fun isMaterialDownloaded(materialId: String): Boolean {
        return materialDao.isDownloaded(materialId)
    }

    // جلب جميع المواد المحملة
    suspend fun getAllDownloadedMaterials(): List<ClassroomMaterial> {
        return materialDao.getAllMaterials().map { it.toClassroomMaterial() }
    }

    // جلب المواد حسب النوع
    suspend fun getMaterialsByType(type: MaterialType): List<ClassroomMaterial> {
        return materialDao.getMaterialsByType(type.name).map { it.toClassroomMaterial() }
    }

    // جلب الكويزات فقط
    suspend fun getAllQuizzes(): List<ClassroomMaterial> {
        return materialDao.getAllQuizzes().map { it.toClassroomMaterial() }
    }

    // جلب المواد المنظمة حسب الموضوعات
    suspend fun getMaterialsGroupedByTopic(): List<CourseTopic> {
        val allMaterials = getAllDownloadedMaterials()
        return allMaterials
            .groupBy { it.topicId }
            .map { (topicId, materials) ->
                CourseTopic(
                    id = topicId,
                    name = materials.firstOrNull()?.topicName ?: "غير مصنف",
                    materials = materials
                )
            }
    }

    // جلب الكويزات المنظمة حسب الموضوعات
    suspend fun getQuizzesGroupedByTopic(): List<CourseTopic> {
        val allQuizzes = getAllQuizzes()
        return allQuizzes
            .groupBy { it.topicId }
            .map { (topicId, quizzes) ->
                CourseTopic(
                    id = topicId,
                    name = quizzes.firstOrNull()?.topicName ?: "غير مصنف",
                    materials = quizzes
                )
            }
    }

    // جلب جميع المواد المنظمة حسب الموضوعات بشكل حي (Flow)
    fun getMaterialsGroupedByTopicFlow(): kotlinx.coroutines.flow.Flow<List<CourseTopic>> {
        return materialDao.getAllMaterialsFlow().map { allMaterials ->
            allMaterials.map { it.toClassroomMaterial() }
                .groupBy { it.topicId }
                .map { (topicId, materials) ->
                    CourseTopic(
                        id = topicId,
                        name = materials.firstOrNull()?.topicName ?: "غير مصنف",
                        materials = materials
                    )
                }
        }
    }

    // جلب الكويزات المنظمة حسب الموضوعات بشكل حي (Flow)
    fun getQuizzesGroupedByTopicFlow(): kotlinx.coroutines.flow.Flow<List<CourseTopic>> {
        return materialDao.getAllQuizzesFlow().map { allQuizzes ->
            allQuizzes.map { it.toClassroomMaterial() }
                .groupBy { it.topicId }
                .map { (topicId, quizzes) ->
                    CourseTopic(
                        id = topicId,
                        name = quizzes.firstOrNull()?.topicName ?: "غير مصنف",
                        materials = quizzes
                    )
                }
        }
    }

    // حذف مادة
    suspend fun deleteMaterial(materialId: String) {
        val material = materialDao.getMaterialById(materialId)
        material?.let {
            val file = java.io.File(it.localFilePath)
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
        materialDao.deleteById(materialId)
    }

    // حذف الكويزات فقط
    suspend fun deleteAllQuizzes() {
        val quizzes = materialDao.getAllQuizzes()
        quizzes.forEach {
            val file = java.io.File(it.localFilePath)
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
        // سنحتاج لإضافة Query في DAO لحذف الكويزات فقط أو استخدام loop
        quizzes.forEach { materialDao.deleteById(it.id) }
    }

    // حذف كل المواد عدا الكويزات
    suspend fun deleteAllMaterialsExceptQuizzes() {
        val materials = materialDao.getAllMaterials().filter { it.type != MaterialType.QUIZ.name }
        materials.forEach {
            val file = java.io.File(it.localFilePath)
            if (file.exists()) {
                file.deleteRecursively()
            }
            materialDao.deleteById(it.id)
        }
    }

    // حذف الكل
    suspend fun deleteAllMaterials() {
        val materials = materialDao.getAllMaterials()
        materials.forEach {
            val file = java.io.File(it.localFilePath)
            if (file.exists()) {
                file.deleteRecursively()
            }
        }
        materialDao.deleteAll()
    }
}

// دالة تحويل من Entity إلى ClassroomMaterial
fun MaterialEntity.toClassroomMaterial(): ClassroomMaterial {
    return ClassroomMaterial(
        id = id,
        title = title,
        type = MaterialType.valueOf(type),
        isDownloaded = true,
        localFilePath = localFilePath,
        topicId = topicId,
        topicName = topicName,
        fileSize = fileSize,
        mimeType = mimeType
    )
}
