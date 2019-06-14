package group14.finalproject.mytodotask.room

import android.arch.persistence.room.*
import android.arch.persistence.room.OnConflictStrategy

@Dao
interface TaskDAO {
    @Query("SELECT * FROM task")
    fun getAll(): List<Task>

    @Query("SELECT * FROM task WHERE id=:id")
    fun findById(id: Int): Task

    @Query("SELECT * FROM task WHERE title=:title")
    fun findByTitle(title: String): Task

    @Query("SELECT * FROM task WHERE checked=:checked")
    fun findByChecked(checked: Boolean): Task

    @Query("SELECT * FROM task WHERE time=:time")
    fun findByTime(time: String): Task

    @Query("SELECT * FROM task WHERE date=:date")
    fun findByDate(date: String): Task

    @Query("SELECT * FROM task WHERE priority=:priority")
    fun findByPriority(priority: Int): Task

    @Query("SELECT * FROM task WHERE alarmtime=:alarmtime")
    fun findByAlarmTime(alarmtime: String): Task

    @Query("SELECT * FROM task WHERE remindertime=:remindertime")
    fun findByReminderTime(remindertime: String): Task

    @Query("SELECT * FROM task WHERE categorize=:categorize")
    fun findByCategorize(categorize: String): Task

    @Query("SELECT * FROM task WHERE repeat=:repeat")
    fun findByRepeat(repeat: String): Task

    @Query("SELECT * FROM task WHERE attach=:attach")
    fun findByAttach(attach: String): Task

    @Query("SELECT * FROM task WHERE description=:description")
    fun findByDescription(description: String): Task

    @Query("SELECT * FROM task WHERE checked=:state")
    fun findAllTasksWithState(state: Boolean): List<Task>

    @Insert
    fun insertAll(vararg todo: Task): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(obj: Task): Long

    @Delete
    fun delete(todo: Task)

    @Update
    fun update(task: Task)

    @Query("DELETE FROM task")
    fun deleteAllTask()
}