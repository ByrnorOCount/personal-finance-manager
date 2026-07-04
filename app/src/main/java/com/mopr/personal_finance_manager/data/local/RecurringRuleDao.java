package com.mopr.personal_finance_manager.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface RecurringRuleDao {

    @Insert
    void insert(RecurringRule rule);

    @Update
    void update(RecurringRule rule);

    @Delete
    void delete(RecurringRule rule);

    @Query("SELECT * FROM recurring_rules ORDER BY name ASC")
    LiveData<List<RecurringRule>> getAllRules();

    @Query("SELECT * FROM recurring_rules WHERE isActive = 1")
    List<RecurringRule> getActiveRulesSync();
}
