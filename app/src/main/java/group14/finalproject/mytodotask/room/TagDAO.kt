package group14.finalproject.mytodotask.room

import android.arch.persistence.room.*

@Dao
interface TagDAO {
    @Query("SELECT * FROM tag")
    fun getAll(): List<Tag>

    @Query("SELECT * FROM tag WHERE id=:id")
    fun findById(id: Int): Tag

    @Query("SELECT * FROM tag WHERE tag=:tag")
    fun findByTag(tag: String): Tag

    @Insert
    fun insertAll(vararg todo: Tag): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(obj: Tag): Long

    @Delete
    fun delete(todo: Tag)

    @Update
    fun update(tag: Tag)

    @Query("DELETE FROM tag")
    fun deleteAllTags()
}