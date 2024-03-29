package group14.finalproject.mytodotask.repo

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import group14.finalproject.mytodotask.room.*

interface RepositoryHelper {
    // Firebase Database Reference
    fun getFirebaseReference(): DatabaseReference

    fun getTasksFirebaseDatabase(username: String): ArrayList<Task>
    fun getTagsFirebaseDatabase(username: String): ArrayList<Tag>
    fun getRelationshipsFirebaseDatabase(username: String): ArrayList<Relationship>

    fun writeTaskFirebaseDatabase(task: Task, username: String)
    fun removeTaskFirebaseDatabase(task: Task, username: String)
    fun removeAllTasksFirebaseDatabase(username: String)

    fun writeTagFirebaseDatabase(tag: Tag, username: String)
    fun removeTagFirebaseDatabase(tag: Tag, username: String)
    fun removeAllTagsFirebaseDatabase(username: String)

    fun writeRelationshipFirebaseDatabase(relationship: Relationship, username: String)
    fun removeRelationshipFirebaseDatabase(relationship: Relationship, username: String)
    fun removeAllRelationshipsFirebaseDatabase(username: String)

    // Get/Add/Update/Delete
    fun getAllTasks(): List<Task>
    fun getAllTags(): List<Tag>
    fun getAllRelationships(): List<Relationship>

    fun insertAllTasks(vararg todo: Task): List<Long>
    fun insertAllTags(vararg todo: Tag): List<Long>
    fun insertAllRelationships(vararg todo: Relationship): List<Long>

    fun deleteAllTasks()
    fun deleteAllTags()
    fun deleteAllRelationship()
    fun deleteAll()

    fun updateTask(task: Task)
    fun updateTag(tag: Tag)
    fun updateRelationship(relationship: Relationship)

    fun insertTask(task: Task): Long
    fun insertTag(tag: Tag): Long
    fun insertRelationship(relationship: Relationship): Long

    fun deleteTask(task: Task)
    fun deleteTag(tag: Tag)
    fun deleteRelationship(relationship: Relationship)

    // Find in TaskDatabase
    fun findByIdTask(id: Int): Task
    fun findByTitleTask(title: String): Task
    fun findByCheckedIdTask(isChecked: Boolean): Task
    fun findByPriorityTask(priority: Int): Task
    fun findAllTasksByStateChecked(isChecked: Boolean): List<Task>

    // Find in TagDatabase
    fun findByIdTag(id: Int): Tag
    fun findByTagName(tag: String): Tag

    // Find in RelationshipDatabase
    fun findByIdRelationship(id: Int): Relationship
    fun findByTagIdInRelationship(idTag: Int): Relationship
    fun findByTaskIdInRelationship(idTask: Int): Relationship
    fun findAllRelationshipsByIdTag(idTag: Int): List<Relationship>
}