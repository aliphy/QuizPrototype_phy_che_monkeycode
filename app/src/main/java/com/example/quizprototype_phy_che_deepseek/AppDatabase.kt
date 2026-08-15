package com.example.quizprototype_phy_che_deepseek

//package com.yourapp.physicschem.data.local

import android.content.Context
import androidx.room.*

@Entity(tableName = "downloaded_materials")
data class MaterialEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val localFilePath: String,
    val topicId: String,
    val topicName: String,
    val downloadDate: Long = System.currentTimeMillis(),
    val fileSize: Long = 0,
    val mimeType: String? = null,
)

@Dao
interface MaterialDao {
    @Query("SELECT * FROM downloaded_materials ORDER BY downloadDate DESC")
    fun getAllMaterialsFlow(): kotlinx.coroutines.flow.Flow<List<MaterialEntity>>

    @Query("SELECT * FROM downloaded_materials WHERE type = 'QUIZ' ORDER BY downloadDate DESC")
    fun getAllQuizzesFlow(): kotlinx.coroutines.flow.Flow<List<MaterialEntity>>

    @Query("SELECT * FROM downloaded_materials ORDER BY downloadDate DESC")
    suspend fun getAllMaterials(): List<MaterialEntity>

    @Query("SELECT * FROM downloaded_materials WHERE type = :type ORDER BY downloadDate DESC")
    suspend fun getMaterialsByType(type: String): List<MaterialEntity>

    @Query("SELECT * FROM downloaded_materials WHERE type = 'QUIZ' ORDER BY downloadDate DESC")
    suspend fun getAllQuizzes(): List<MaterialEntity>

    @Query("SELECT * FROM downloaded_materials WHERE topicId = :topicId")
    suspend fun getMaterialsByTopic(topicId: String): List<MaterialEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: MaterialEntity)

    @Delete
    suspend fun deleteMaterial(material: MaterialEntity)

    @Query("DELETE FROM downloaded_materials WHERE id = :materialId")
    suspend fun deleteById(materialId: String)

    @Query("DELETE FROM downloaded_materials")
    suspend fun deleteAll()

    @Query("SELECT * FROM downloaded_materials WHERE id = :materialId")
    suspend fun getMaterialById(materialId: String): MaterialEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM downloaded_materials WHERE id = :materialId)")
    suspend fun isDownloaded(materialId: String): Boolean
}

@Database(entities = [MaterialEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun materialDao(): MaterialDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physics_chem_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
