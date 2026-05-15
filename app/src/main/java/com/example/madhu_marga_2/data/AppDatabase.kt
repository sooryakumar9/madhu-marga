package com.example.madhu_marga_2.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HiveDao {
    @Insert
    suspend fun insertHive(hive: Hive)

    @Query("SELECT * FROM hives")
    fun getAllHives(): Flow<List<Hive>>

    @Insert
    suspend fun insertInspection(inspection: Inspection)

    @Query("SELECT * FROM inspections WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getInspectionsForHive(hiveId: Long): Flow<List<Inspection>>

    @Insert
    suspend fun insertHarvest(harvest: Harvest)

    @Query("SELECT * FROM harvests WHERE hiveId = :hiveId ORDER BY date DESC")
    fun getHarvestsForHive(hiveId: Long): Flow<List<Harvest>>

    @Query("SELECT * FROM harvests")
    fun getAllHarvests(): Flow<List<Harvest>>
}

@Database(entities = [Hive::class, Inspection::class, Harvest::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hiveDao(): HiveDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "madhu_marga_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
