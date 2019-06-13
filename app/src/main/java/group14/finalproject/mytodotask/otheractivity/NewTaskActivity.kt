package group14.finalproject.mytodotask.otheractivity

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.room.*
import kotlinx.android.synthetic.main.activity_task.*
import kotlin.collections.ArrayList

class NewTaskActivity : AppCompatActivity() {
    var tags: ArrayList<Tag> = ArrayList()
    var tasks: ArrayList<Task> = ArrayList()
    var checkedTags: ArrayList<Boolean> = ArrayList()
    var relationships: ArrayList<Relationship> = ArrayList()

    // Initial Variables
    var title = "Title"
    var description = "Description"
    var date = ""
    var time = ""
    var completed = false
    var priority = 0
    var reminder = ""
    var locationX = .0f
    var locationY = .0f
    var categorize = "Uncategorized"
    var indexRadioButton = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task)

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
        intent.putExtra(NEW_TASK_KEY, newTask)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
