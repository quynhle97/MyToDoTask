package group14.finalproject.mytodotask.otheractivity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.DatePicker
import android.widget.RadioButton
import android.widget.TimePicker
import android.widget.Toast
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.activity_details_task.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.view.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TaskActivity : AppCompatActivity() {
    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    lateinit var tags: ArrayList<Tag>

    lateinit var listTagsName: ArrayList<String>
    lateinit var listCheckedTags: BooleanArray
    lateinit var relationships: ArrayList<Relationship>

    var indexNewOrDetail: Int = -1
    var indexRadioButton: Int = 1
    var indexItemClicked: Int = -1
    var idItemClicked: Int = -1

    var reminderTime: Date? = null
    var alarmTime: Date? = null
    lateinit var editTask: Task
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_task)

        (application as MyApplication).getAppComponent().inject(this)
        username = SharedPreferencesHelper.readString(USERNAME_KEY)
        getInitialDatabaseTagsAndRelationships()

        val data = intent.extras
        indexNewOrDetail = data.getInt(INDEX_NEW_DETAIL)
        if (indexNewOrDetail == 1) {
            setInitialForDetailTask()
        }

        listCheckedTags = BooleanArray(tags.size)
        listTagsName = ArrayList(tags.size)

        radio_priority_choice.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }

        tv_add_tags.setOnClickListener {
            if (indexNewOrDetail == 0) {
                listCheckedTags = BooleanArray(tags.size)
                listTagsName = ArrayList(tags.size)
                for (i in tags) listTagsName.add(i.tag)
                val listTags = arrayOfNulls<String>(listTagsName.size)
                listTagsName.toArray(listTags)

                val checkedTags = BooleanArray(tags.size)
                for (i in 0 until tags.size) checkedTags[i] = false
                showDialogSelectTagsForNewTask(listTags, checkedTags)
            }
            else if (indexNewOrDetail == 1) {
                relationships = ArrayList()
                tags = ArrayList()
                getInitialDatabaseTagsAndRelationships()

                listTagsName = ArrayList(tags.size)
                listCheckedTags = BooleanArray(tags.size)

                for (i in tags) listTagsName.add(i.tag)

                val listTags = arrayOfNulls<String>(listTagsName.size)
                listTagsName.toArray(listTags)

                // Initial checkbox value
                val checkedTags = BooleanArray(tags.size)
                val listRelationships = ArrayList<String>()

                for (i in 0 until relationships.size) {
                    if (editTask.id == relationships[i].note) {
                        listRelationships.add(getTagName((relationships[i].tag)))
                    }
                }

                for (i in 0 until tags.size) {
                    for (j in 0 until listRelationships.size) {
                        if (listRelationships[j] == listTags[i]) {
                            checkedTags[i] = true
                            Toast.makeText(applicationContext, "${checkedTags[i]} - ${listTags[i]}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDialogSelectTagsForEditTask(listTags, checkedTags)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_save-> {
                if (indexNewOrDetail == 0) {
                    handleSaveNewTask()
                } else if (indexNewOrDetail == 1) {
                    handleSaveEditTask()
                }
                return true
            }
            R.id.action_delete-> {
                if (indexNewOrDetail == 1) {
                    handleDeleteTaskForEditTask()
                }
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val btnDelete = menu?.findItem(R.id.action_delete)
        val btnSave = menu?.findItem(R.id.action_save)

        btnDelete?.isVisible = indexNewOrDetail != 0
        btnSave?.isVisible = true

        return true
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun setInitialForDetailTask() {
        val data = intent.extras
        editTask = data.getParcelable(EDIT_TASK) as Task
        indexItemClicked = data.getInt(EDIT_TASK_POSITION)

        idItemClicked = editTask.id!!

        edt_title.setText(editTask.title)
        edt_description_note.setText(editTask.description)
        cb_completed.isChecked = editTask.checked
        indexRadioButton = editTask.priority

        tv_add_tags.text = editTask.categorize

        val btnLow = radio_priority_choice.findViewById<RadioButton>(R.id.btnLow)
        val btnNormal = radio_priority_choice.findViewById<RadioButton>(R.id.btnNormal)
        val btnHigh = radio_priority_choice.findViewById<RadioButton>(R.id.btnHigh)

        when (indexRadioButton) {
            0 -> btnLow.isChecked = true
            1 -> btnNormal.isChecked = true
            2 -> btnHigh.isChecked = true
        }

        tv_add_reminder.setOnClickListener {
            // Use the current time as the default values for the picker
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            // Create a new instance of TimePickerDialog and return it
            val timePickerDialog =  TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hourOfDay: Int, minute: Int ->
                c.set(Calendar.HOUR_OF_DAY, hourOfDay)
                c.set(Calendar.MINUTE, minute)
                val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { datePicker: DatePicker, year: Int, month: Int, day: Int ->
                    tv_add_reminder.text = "Hour: $hourOfDay, minute: $minute"
                    c.set(Calendar.YEAR, year)
                    c.set(Calendar.MONTH, month)
                    c.set(Calendar.DAY_OF_MONTH, day)
                    reminderTime = c.time
                    if (alarmTime == null) {
                        alarmTime = reminderTime
                    }
                    val simpleDateTime = SimpleDateFormat("hh:mm dd/mm/yy")
                    tv_add_reminder.text = simpleDateTime.format(reminderTime)
                }, year, month, day)
                datePickerDialog.setTitle("Select date")
                datePickerDialog.show()
            }, hour, minute, DateFormat.is24HourFormat(this))
            timePickerDialog.setTitle("Select Time")
            timePickerDialog.show()
        }
    }

    private fun getInitialDatabaseTagsAndRelationships() {
        tags = repositoryHelper.getAllTags() as java.util.ArrayList<Tag>
        relationships = repositoryHelper.getAllRelationships() as java.util.ArrayList<Relationship>
    }

    private fun handleSaveNewTask() {
        val newTask = Task()
        newTask.title = edt_title.text.toString()
        newTask.checked = cb_completed.isChecked
        newTask.description = edt_description_note.text.toString()
        newTask.categorize = tv_add_tags.text.toString()
        newTask.priority = indexRadioButton

        val intent = Intent()
        val id = repositoryHelper.insertTask(newTask)           // Local Database
        newTask.id = id.toInt()
        repositoryHelper.writeTaskFirebaseDatabase(newTask, username) // Firebase Database

        handleSaveRelationshipForNewTask(newTask.id!!)

        intent.putExtra(NEW_TASK_KEY, newTask)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun handleSaveEditTask() {
        editTask.id = idItemClicked
        editTask.title = edt_title.text.toString()
        editTask.description = edt_description_note.text.toString()
        editTask.checked = cb_completed.isChecked
        editTask.priority = indexRadioButton
        editTask.categorize = tv_add_tags.text.toString()

        val intent = Intent()
        intent.putExtra(EDIT_TASK_KEY, editTask)
        intent.putExtra(EDIT_TASK_POSITION_KEY, indexItemClicked)
        intent.putExtra(DELETE_TASK_POSITION_KEY, -1)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun handleSaveRelationshipForNewTask(idTask: Int) {
        for (i in 0 until listCheckedTags.size) {
            if (listCheckedTags[i]) {
                val tag = repositoryHelper.findByTagName(listTagsName[i])
                if (tag.id != null) {
                    val rel = Relationship(null, tag.id!!, idTask)
                    val id = repositoryHelper.insertRelationship(rel)                             // Local Database
                    rel.id = id.toInt()
                    if (username != USERNAME_DEFAULT)
                        repositoryHelper.writeRelationshipFirebaseDatabase(rel, username)               // Firebase Database
                    Toast.makeText(applicationContext, "Save Tag added: ${tag.id} - $idTask", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun handleSaveRelationshipForEditTask() {
        for (i in 0 until listCheckedTags.size) {
            if (listCheckedTags[i]) {
                val tag = repositoryHelper.findByTagName(listTagsName[i])
                if (tag.id != null) {
                    val rel = Relationship(null, tag.id!!, editTask.id!!)
                    val id = repositoryHelper.insertRelationship(rel)                                // Local Database
                    rel.id = id.toInt()
                    if (username != USERNAME_DEFAULT)
                        repositoryHelper.writeRelationshipFirebaseDatabase(rel, username)   // Firebase Database
                    Toast.makeText(applicationContext, "Save Tag-Task Relationship added: ${getTagName(tag.id!!)} - ${editTask.title}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun getTagName(idInt: Int): String {
        return repositoryHelper.findByIdTag(idInt).tag
    }

    private fun showDialogSelectTagsForNewTask(listTags: Array<String?>, checkedTags: BooleanArray) {
        val builder = AlertDialog.Builder(this)
        builder.setMultiChoiceItems(listTags, checkedTags) { dialog, which, isChecked ->
            Toast.makeText(applicationContext, "$which - $isChecked", Toast.LENGTH_SHORT).show()
            checkedTags[which] = isChecked
        }
        builder.setTitle("List of Tags")
        builder.setCancelable(false)

        builder.setPositiveButton("Save") { dialog, which ->
            listCheckedTags = checkedTags // Save to adapter
            var tmp = ""
            for (i in 0 until tags.size) {
                if (listCheckedTags[i]) {
                    tmp += listTagsName[i] + ", "
                }
            }
            tv_add_tags.text = tmp
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }

        builder.setNeutralButton("Create") { dialog, which ->
            showDialogCreateNewTag()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun showDialogSelectTagsForEditTask(listTags: Array<String?>, checkedTags: BooleanArray) {
        val builder = AlertDialog.Builder(this)
        builder.setMultiChoiceItems(listTags, checkedTags) { dialog, which, isChecked ->
            Toast.makeText(applicationContext, "Tag: $which - State: $isChecked", Toast.LENGTH_SHORT).show()
            checkedTags[which] = isChecked
        }

        builder.setCancelable(false)
        builder.setTitle("List of Tags")

        builder.setPositiveButton("Save") { dialog, which ->
            listCheckedTags = checkedTags // Save to adapter

            // Delete all tag - note
            for (i in relationships) {
                if (i.note == editTask.id) {
                    repositoryHelper.deleteRelationship(i)                                  // Local Database
                    if (username != USERNAME_DEFAULT)
                        repositoryHelper.removeRelationshipFirebaseDatabase(i, username)    // Firebase Database
                }
            }

            var tmp = ""
            for (i in 0 until tags.size) {
                if (listCheckedTags[i]) {
                    tmp += listTagsName[i] + ", "
                }
            }
            tv_add_tags.text = tmp
            handleSaveRelationshipForEditTask()
        }

        builder.setNegativeButton("Cancel") { dialog, which -> }

        builder.setNeutralButton("Create") { dialog, which ->
            showDialogCreateNewTag()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showDialogCreateNewTag() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_tag, null)
        val mBuilder = android.app.AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Create new tag")
        val mAlertDialog = mBuilder.create()
        mAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        mAlertDialog.show()
        mAlertDialog.btnCreateTagDialog.setOnClickListener {
            mAlertDialog.dismiss()
            val tagName = mDialogView.edt_tag_name.text.toString()
            val newTag = Tag()
            newTag.tag = tagName
            val id = repositoryHelper.insertTag(newTag)                                      // Add Local Database
            newTag.id = id.toInt()
            if (username != USERNAME_DEFAULT)
                repositoryHelper.writeTagFirebaseDatabase(newTag, username)         // Add Firebase Database
            tags.add(newTag)                                                        // Add dialog view
            Toast.makeText(applicationContext, "New tag added: $tagName", Toast.LENGTH_SHORT).show()
        }
        mAlertDialog.btnCancelDialog.setOnClickListener {
            mAlertDialog.dismiss()
        }
    }

    private fun handleDeleteTaskForEditTask() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Confirmation")
            .setMessage("Are you sure to remove this task?")
            .setPositiveButton("OK") { _, _ ->
                val intent = Intent()
                intent.putExtra(DELETE_TASK_POSITION_KEY, indexItemClicked)
                intent.putExtra(EDIT_TASK_POSITION_KEY, -1)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog?.dismiss() }

        val myDialog = builder.create()
        myDialog.show()
    }
}
