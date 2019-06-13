package group14.finalproject.mytodotask.otheractivity

import android.app.Activity
import android.content.Intent
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Menu
import android.view.MenuItem
import android.widget.RadioButton
import android.widget.TimePicker
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.room.*
import kotlinx.android.synthetic.main.activity_details_task.*

class DetailsTaskActivity : AppCompatActivity() {
    var indexRadioButton: Int = 1
    var indexItemClicked: Int = -1
    var idItemClicked: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_task)

        setInitialView()

        radio_priority_choice.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details_task, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_save-> {
                val editTask = Task()

                editTask.id = idItemClicked
                editTask.title = edt_title.text.toString()
                editTask.description = edt_description_note.text.toString()
                editTask.checked = cb_completed.isChecked
                editTask.priority = indexRadioButton
                editTask.categorize = tv_uncategorized.text.toString()

                val intent = Intent()
                intent.putExtra(EDIT_TASK_KEY, editTask)
                intent.putExtra(EDIT_TASK_POSITION_KET, indexItemClicked)
                setResult(Activity.RESULT_OK, intent)
                finish()
                return true
            }
            R.id.action_delete-> {
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setInitialView() {
        val data = intent.extras
        val editTask = data.getParcelable(EDIT_TASK) as Task
        indexItemClicked = data.getInt(EDIT_TASK_POSITION)

        idItemClicked = editTask.id!!

        edt_title.setText(editTask.title)
        edt_description_note.setText(editTask.description)
        cb_completed.isChecked = editTask.checked
        indexRadioButton = editTask.priority

        tv_uncategorized.text = editTask.categorize

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
}
