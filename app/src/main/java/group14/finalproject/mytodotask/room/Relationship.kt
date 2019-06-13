package group14.finalproject.mytodotask.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Relationship (
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var tag: Int,
    var note: Int
) {
    constructor() : this(null, -1, -1)
}