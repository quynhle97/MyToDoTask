package group14.finalproject.mytodotask.repo

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.room.*
import javax.inject.Inject

class RepositoryManager @Inject constructor (taskDao: TaskDAO, tagDAO: TagDAO, relationshipDAO: RelationshipDAO, firebaseDatabase: FirebaseDatabase) : RepositoryHelper {
    private val taskDao: TaskDAO = taskDao
    private val tagDao: TagDAO = tagDAO
    private val relationshipDao: RelationshipDAO = relationshipDAO
    private val firebaseReference: DatabaseReference = firebaseDatabase.reference

    override fun writeTaskFirebaseDatabase(task: Task, username: String) {
        firebaseReference.child(TASK_FIREBASE_DATABASE).child(username).child(task.id.toString()).setValue(task)
    }

    override fun writeTagFirebaseDatabase(tag: Tag, username: String) {
        firebaseReference.child(TAG_FIREBASE_DATABASE).child(username).child(tag.id.toString()).setValue(tag)
    }

    override fun writeRelationshipFirebaseDatabase(relationship: Relationship, username: String) {
        firebaseReference.child(RELATIONSHIP_FIREBASE_DATABASE).child(username).child(relationship.id.toString()).setValue(relationship)
    }

    override fun getAllTasks(): List<Task> {
        return taskDao.getAll()
    }

    override fun getAllTags(): List<Tag> {
        return tagDao.getAll()
    }

    override fun getAllRelationships(): List<Relationship> {
        return relationshipDao.getAll()
    }

    override fun insertAllTasks(vararg todo: Task): List<Long> {
        return taskDao.insertAll(*todo)
    }

    override fun insertAllTags(vararg todo: Tag): List<Long> {
        return tagDao.insertAll(*todo)
    }

    override fun insertAllRelationships(vararg todo: Relationship): List<Long> {
        return relationshipDao.insertAll(*todo)
    }

    override fun deleteAllTasks() {
        taskDao.deleteAllTask()
    }

    override fun deleteAllTags() {
        tagDao.deleteAllTags()
    }

    override fun deleteAllRelationship() {
        relationshipDao.deleteAllRelations()
    }

    override fun deleteAll() {
        deleteAllRelationship()
        deleteAllTags()
        deleteAllTasks()
    }

    override fun updateTask(task: Task) {
        taskDao.update(task)
    }

    override fun updateTag(tag: Tag) {
        tagDao.update(tag)
    }

    override fun updateRelationship(relationship: Relationship) {
        relationshipDao.update(relationship)
    }

    override fun insertTask(task: Task): Long {
        return taskDao.insert(task)
    }

    override fun insertTag(tag: Tag): Long {
        return tagDao.insert(tag)
    }

    override fun insertRelationship(relationship: Relationship): Long {
        return relationshipDao.insert(relationship)
    }

    override fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    override fun deleteTag(tag: Tag) {
        tagDao.delete(tag)
    }

    override fun deleteRelationship(relationship: Relationship) {
        relationshipDao.delete(relationship)
    }

    override fun findByIdTask(id: Int): Task {
        return taskDao.findById(id)
    }

    override fun findByTitleTask(title: String): Task {
        return taskDao.findByTitle(title)
    }

    override fun findByCheckedIdTask(isChecked: Boolean): Task {
        return taskDao.findByChecked(isChecked)
    }

    override fun findByPriorityTask(priority: Int): Task {
        return taskDao.findByPriority(priority)
    }

    override fun findAllTasksByStateChecked(isChecked: Boolean): List<Task> {
        return taskDao.findAllTasksWithState(isChecked)
    }

    override fun findByIdTag(id: Int): Tag {
        return tagDao.findById(id)
    }

    override fun findByTagName(tag: String): Tag {
        return tagDao.findByTag(tag)
    }

    override fun findByIdRelationship(id: Int): Relationship {
        return relationshipDao.findById(id)
    }

    override fun findByTagIdInRelationship(idTag: Int): Relationship {
        return relationshipDao.findByTagId(idTag)
    }

    override fun findByTaskIdInRelationship(idTask: Int): Relationship {
        return relationshipDao.findByNoteId(idTask)
    }

    override fun findAllRelationshipsByIdTag(idTag: Int): List<Relationship> {
        return relationshipDao.findAllRelationshipsWithTag(idTag)
    }
}