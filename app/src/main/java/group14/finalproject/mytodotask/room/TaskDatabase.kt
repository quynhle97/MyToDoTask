package group14.finalproject.mytodotask.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(Task::class), version = 3)
abstract class TaskDatabase : RoomDatabase() {
    abstract fun taskDAO(): TaskDAO

    companion object {
        @Volatile
        private var instance: TaskDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context, databaseName: String) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context, databaseName).also { instance = it }
        }

        private fun buildDatabase(context: Context, databaseName: String) = Room.databaseBuilder(
            context,
            TaskDatabase::class.java, databaseName//DATABASE_TASK_NAME
        ).allowMainThreadQueries()
            .build()
    }
}