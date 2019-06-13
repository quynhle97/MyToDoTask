package group14.finalproject.mytodotask.otheractivity

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.RadioButton
import android.widget.Toast
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.activity_details_task.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.view.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class NewTaskActivity : AppCompatActivity() {
    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    // Initial Variables
    var title = "Title"
    var indexRadioButton = 1

    var tags: ArrayList<Tag> = ArrayList()
    var tasks: ArrayList<Task> = ArrayList()
    var checkedTags: ArrayList<Boolean> = ArrayList()
    var relationships: ArrayList<Relationship> = ArrayList()
    lateinit var listCheckedTags: BooleanArray
    lateinit var listTagsName: ArrayList<String>
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_task)

        (application as MyApplication).getAppComponent().inject(this)

        getInitalDatabaseTagsAndRelationships()
        username = SharedPreferencesHelper.readString(USERNAME_KEY)

        tv_add_tags.setOnClickListener {
            listCheckedTags = BooleanArray(tags.size)
            listTagsName = ArrayList(tags.size)
            for (i in tags) listTagsName.add(i.tag)
            val listTags = arrayOfNulls<String>(listTagsName.size)
            listTagsName.toArray(listTags)

            val checkedTags = BooleanArray(tags.size)
            for (i in 0 until tags.size) checkedTags[i] = false

            showDialogSelectTags(listTags, checkedTags)
        }

        radio_priority_choice.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_save-> {
                handleSaveNewTask()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        handleSaveRelationship(newTask.id!!)

        intent.putExtra(NEW_TASK_KEY, newTask)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun getInitalDatabaseTagsAndRelationships() {
        tags = repositoryHelper.getAllTags() as ArrayList<Tag>
        relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>
    }

    private fun showDialogCreateNewTag() {
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_new_tag, null)
        val mBuilder = android.app.AlertDialog.Builder(this)
            .setView(mDialogView)
            .setTitle("Create new tag")
        val  mAlertDialog = mBuilder.create()
        mAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        mAlertDialog.show()
        mAlertDialog.btnCreateTagDialog.setOnClickListener {
            mAlertDialog.dismiss()
            val tagName = mDialogView.edt_tag_name.text.toString()
            val newTag = Tag()
            newTag.tag = tagName
            repositoryHelper.insertTag(newTag)                                      // Add Local Database
            if (username != USERNAME_DEFAULT)
                repositoryHelper.writeTagFirebaseDatabase(newTag, username)         // Add Firebase Database
            tags.add(newTag)                                                        // Add dialog view
            Toast.makeText(applicationContext,"New tag added: $tagName", Toast.LENGTH_SHORT).show()
        }
        mAlertDialog.btnCancelDialog.setOnClickListener{
            mAlertDialog.dismiss()
        }
    }

    private fun showDialogSelectTags(listTags: Array<String?>, checkedTags: BooleanArray) {
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

    private fun handleSaveRelationship(idTask: Int) {
        for (i in 0 until tags.size) {
            if (listCheckedTags[i]) {
                val tag = repositoryHelper.findByTagName(listTagsName[i])
                if (tag.id != null) {
                    val rel = Relationship(null, tag.id!!, idTask)
                    repositoryHelper.insertRelationship(rel)                                // Local Database
                    if (username != USERNAME_DEFAULT)
                        repositoryHelper.writeRelationshipFirebaseDatabase(rel, username)   // Firebase Database
                    Toast.makeText(applicationContext, "Save Tag added: ${tag.id} - $idTask", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}
