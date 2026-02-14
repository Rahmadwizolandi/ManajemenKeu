package com.example.myapplication

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {
    @Query("SELECT * FROM saving_goal ORDER BY id DESC")
    fun getAllGoals(): Flow<List<SavingGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingGoalEntity)
}
