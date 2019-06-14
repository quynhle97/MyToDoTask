package group14.finalproject.mytodotask.di

import com.google.firebase.database.*
import dagger.Component
import group14.finalproject.mytodotask.MainActivity
import group14.finalproject.mytodotask.otheractivity.SearchActivity
import group14.finalproject.mytodotask.otheractivity.TaskActivity
import group14.finalproject.mytodotask.repo.RepositoryManager
import group14.finalproject.mytodotask.room.*
import javax.inject.Singleton

@Singleton
@Component (modules = [AppModule::class, TaskModule::class])
interface AppComponent {

    fun firebaseDatabase(): FirebaseDatabase
    fun taskDatabase(): TaskDatabase
    fun tagDatabase(): TagDatabase
    fun relationshipDatabase(): RelationshipDatabase

    fun taskDao(): TaskDAO
    fun tagDao(): TagDAO
    fun relationshipDao(): RelationshipDAO

    fun repositoryManager(): RepositoryManager

    fun inject (mainActivity: MainActivity)
    fun inject (mSearchActivity: SearchActivity)
    fun inject (mTaskActivity: TaskActivity)
}