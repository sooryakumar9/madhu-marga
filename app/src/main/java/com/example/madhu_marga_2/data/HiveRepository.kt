package com.example.madhu_marga_2.data

import kotlinx.coroutines.flow.Flow

class HiveRepository(private val hiveDao: HiveDao) {
    val allHives: Flow<List<Hive>> = hiveDao.getAllHives()
    val allHarvests: Flow<List<Harvest>> = hiveDao.getAllHarvests()

    suspend fun insertHive(hive: Hive) = hiveDao.insertHive(hive)
    suspend fun insertInspection(inspection: Inspection) = hiveDao.insertInspection(inspection)
    suspend fun insertHarvest(harvest: Harvest) = hiveDao.insertHarvest(harvest)
    
    fun getInspectionsForHive(hiveId: Long) = hiveDao.getInspectionsForHive(hiveId)
    fun getHarvestsForHive(hiveId: Long) = hiveDao.getHarvestsForHive(hiveId)
}
