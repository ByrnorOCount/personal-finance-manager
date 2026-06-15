package com.mopr.personal_finance_manager.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.mopr.personal_finance_manager.data.local.entity.SavingsGoal;

import java.util.List;

@Dao
public interface SavingsGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SavingsGoal goal);

    @Update
    void update(SavingsGoal goal);

    @Query("DELETE FROM savings_goals WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT * FROM savings_goals ORDER BY deadline ASC")
    LiveData<List<SavingsGoal>> getAll();

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 0 ORDER BY deadline ASC")
    LiveData<List<SavingsGoal>> getActive();

    @Query("SELECT * FROM savings_goals WHERE isCompleted = 1 ORDER BY deadline DESC")
    LiveData<List<SavingsGoal>> getCompleted();

    @Query("DELETE FROM savings_goals")
    void deleteAll();
}
