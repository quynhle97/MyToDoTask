package group14.finalproject.mytodotask.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity
data class Tag (
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var tag: String
    ) {
    constructor() : this(null, "")
}