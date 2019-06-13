package group14.finalproject.mytodotask.otheractivity

import android.app.Activity
import android.content.Intent
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
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
import javax.inject.Inject

class DetailsTaskActivity : AppCompatActivity() {
    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    private lateinit var username: String

    var indexRadioButton: Int = 1
    var indexItemClicked: Int = -1
    var idItemClicked: Int = -1

    lateinit var editTask: Task
    lateinit var tags: ArrayList<Tag>
    lateinit var relationships: ArrayList<Relationship>

    lateinit var listTagsName: ArrayList<String>
    lateinit var listCheckedTags: BooleanArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_task)

        (application as MyApplication).getAppComponent().inject(this)
        username = SharedPreferencesHelper.readString(USERNAME_KEY)
        setInitialView()

        radio_priority_choice.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }

        tv_add_tags.setOnClickListener {
            relationships = ArrayList()
            tags = ArrayList()
            getInitalDatabaseTagsAndRelationships()

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
                handleSaveRelationship()
            }

            builder.setNegativeButton("Cancel") { dialog, which -> }

            builder.setNeutralButton("Create") { dialog, which ->
                showDialogCreateNewTag()
            }

            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_save-> {
                handleSaveTask()
                return true
            }
            R.id.action_delete-> {
                handleDeleteTask()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setInitialView() {
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

            // Create a new instance of TimePickerDialog and return it
            val timePickerDialog =  TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { timePicker: TimePicker, hourOfDay: Int, minute: Int ->
                tv_add_reminder.setText("Hour: $hourOfDay, minute: $minute")
            }, hour, minute, DateFormat.is24HourFormat(this))
            timePickerDialog.setTitle("Select Time")
            timePickerDialog.show()
        }

    }

    private fun handleSaveTask() {
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

    private fun handleDeleteTask() {
        val builder = AlertDialog.Builder(this@DetailsTaskActivity)
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

    private fun getInitalDatabaseTagsAndRelationships() {
        tags = repositoryHelper.getAllTags() as ArrayList<Tag>
        relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>
    }

    private fun getTagName(idInt: Int): String {
        return repositoryHelper.findByIdTag(idInt).tag
    }

    private fun handleSaveRelationship() {
        for (i in 0 until tags.size) {
            if (listCheckedTags[i]) {
                val tag = repositoryHelper.findByTagName(listTagsName[i])
                if (tag.id != null) {
                    val rel = Relationship(null, tag.id!!, editTask.id!!)
                    repositoryHelper.insertRelationship(rel)                                // Local Database
                    if (username != USERNAME_DEFAULT)
                        repositoryHelper.writeRelationshipFirebaseDatabase(rel, username)   // Firebase Database
                    Toast.makeText(applicationContext, "Save Tag-Task Relationship added: ${getTagName(tag.id!!)} - ${editTask.title}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun showDialogCreateNewTag() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_tag, null)
        val mBuilder = android.app.AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Create new tag")
        val  mAlertDialog = mBuilder.show()
        mAlertDialog.btnCreateTagDialog.setOnClickListener {
            mAlertDialog.dismiss()
            val tagName = mDialogView.edt_tag_name.text.toString()
            val newTag = Tag()
            newTag.tag = tagName

            val id = repositoryHelper.insertTag(newTag)                 // Local Database
            newTag.id = id.toInt()
            if (username != USERNAME_DEFAULT)
                repositoryHelper.writeTagFirebaseDatabase(newTag, username)   // Firebase Database
            tags.add(newTag)                                                  // Add list tags adapter (VIew)
            Toast.makeText(applicationContext,"New tag added: $tagName", Toast.LENGTH_SHORT).show()
        }
        mAlertDialog.btnCancelDialog.setOnClickListener{
            mAlertDialog.dismiss()
        }
    }
}
