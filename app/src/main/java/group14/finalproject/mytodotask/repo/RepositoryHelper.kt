package group14.finalproject.mytodotask.repo

import group14.finalproject.mytodotask.room.*

interface RepositoryHelper {
    // Firebase Database Reference
    fun writeTaskFirebaseDatabase(task: Task)
    fun writeTagFirebaseDatabase(tag: Tag)
    fun writeRelationshipFirebaseDatabase(relationship: Relationship)

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