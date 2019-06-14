package group14.finalproject.mytodotask.room

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity
data class Task(
    @PrimaryKey(autoGenerate = true) var id: Int? = null,
    var title: String,
    var checked: Boolean,
    var date: String,
    var time: String,
    var priority: Int,
    var alarmtime: String,
    var remindertime: String,
    var categorize: String,
    var repeat: String,
    var attach: String,
    var description: String
) : Parcelable  {
    constructor() : this(null, "", false,"","", 0, "", "", "", "", "", "")
}