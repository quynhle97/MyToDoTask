package group14.finalproject.mytodotask.di

import android.app.Application
import android.arch.persistence.room.Room
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper

import dagger.Module
import dagger.Provides
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.repo.RepositoryManager
import javax.inject.Singleton

@Module
class TaskModule (mApplication: Application) {
    private val taskDatabase: TaskDatabase
    private val tagDatabase: TagDatabase
    private val relationshipDatabase: RelationshipDatabase
    private val firebaseDatabase: FirebaseDatabase

    private val databaseTaskName = SharedPreferencesHelper.readString(USERNAME_KEY) + DATABASE_TASK_SUFFIX_NAME
    private val databaseTagName = SharedPreferencesHelper.readString(USERNAME_KEY) + DATABASE_TAG_SUFFIX_NAME
    private val databaseRelationshipName = SharedPreferencesHelper.readString(USERNAME_KEY) + DATABASE_RELATIONSHIP_SUFFIX_NAME

    init {
        taskDatabase = Room.databaseBuilder(mApplication,
            TaskDatabase::class.java, databaseTaskName).allowMainThreadQueries().build()
        tagDatabase = Room.databaseBuilder(mApplication,
            TagDatabase::class.java, databaseTagName).allowMainThreadQueries().build()
        relationshipDatabase = Room.databaseBuilder(mApplication,
            RelationshipDatabase:: class.java, databaseRelationshipName).allowMainThreadQueries().build()
        firebaseDatabase = FirebaseDatabase.getInstance()
    }

    @Singleton
    @Provides
    fun getFirebaseDatabase(): FirebaseDatabase {
        return firebaseDatabase
    }

    @Singleton
    @Provides
    fun getTaskDatabase(): TaskDatabase {
        return taskDatabase
    }

    @Singleton
    @Provides
    fun getTagDatabase(): TagDatabase {
        return tagDatabase
    }

    @Singleton
    @Provides
    fun getRelationshipDatabase(): RelationshipDatabase {
        return relationshipDatabase
    }

    @Singleton
    @Provides
    fun getReferenceFirebaseDatabase(): DatabaseReference {
        return firebaseDatabase.reference
    }

    @Singleton
    @Provides
    fun getTaskDao(): TaskDAO {
        return taskDatabase.taskDAO()
    }

    @Singleton
    @Provides
    fun getTagDao(): TagDAO {
        return tagDatabase.tagDAO()
    }

    @Singleton
    @Provides
    fun getRelationshipDao() : RelationshipDAO {
        return relationshipDatabase.relationshipDAO()
    }

    @Singleton
    @Provides
    fun repositoryHelper(taskDao: TaskDAO, tagDao: TagDAO, relationshipDao: RelationshipDAO, firebaseReference: DatabaseReference): RepositoryHelper {
        return RepositoryManager(taskDao, tagDao, relationshipDao, firebaseReference)
    }
}