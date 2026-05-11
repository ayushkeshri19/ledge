package com.ayush.sms.data.local.classifier

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface ClassifierRuleDao {
    @Query("SELECT * FROM classifier_rules_cache ORDER BY priority DESC")
    suspend fun getAll(): List<ClassifierRuleEntity>

    @Insert
    suspend fun insertAll(rules: List<ClassifierRuleEntity>)

    @Query("DELETE FROM classifier_rules_cache")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(rules: List<ClassifierRuleEntity>) {
        clear()
        insertAll(rules)
    }
}