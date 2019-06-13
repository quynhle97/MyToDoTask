package group14.finalproject.mytodotask.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

@Database(entities = arrayOf(Relationship::class), version = 3)
abstract class RelationshipDatabase : RoomDatabase(){
    abstract fun relationshipDAO(): RelationshipDAO

    companion object {
        @Volatile
        private var instance: RelationshipDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context, databaseName: String) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context, databaseName).also { instance = it }
        }

        private fun buildDatabase(context: Context, databaseName: String) = Room.databaseBuilder(
            context, RelationshipDatabase::class.java, databaseName).allowMainThreadQueries().build()
    }
}