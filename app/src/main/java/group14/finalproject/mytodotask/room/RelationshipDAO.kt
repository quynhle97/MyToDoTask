package group14.finalproject.mytodotask.room

import android.arch.persistence.room.*

@Dao
interface RelationshipDAO {
    @Query("SELECT * FROM relationship")
    fun getAll(): List<Relationship>

    @Query("SELECT * FROM relationship WHERE id=:id")
    fun findById(id: Int): Relationship

    @Query("SELECT * FROM relationship WHERE tag=:tag")
    fun findByTagId(tag: Int): Relationship

    @Query("SELECT * FROM relationship WHERE note=:note")
    fun findByNoteId(note: Int): Relationship

    @Query("SELECT * FROM relationship WHERE tag=:tag")
    fun findAllRelationshipsWithTag(tag: Int): List<Relationship>

    @Insert
    fun insertAll(vararg todo: Relationship): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(obj: Relationship): Long

    @Delete
    fun delete(todo: Relationship)

    @Update
    fun update(relationship: Relationship)

    @Query("DELETE FROM relationship")
    fun deleteAllRelations()
}