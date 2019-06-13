package group14.finalproject.mytodotask.otheractivity

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
    var indexRadioButton = -1

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
                val task = handleSaveEditTask()

                return true
            }
            R.id.action_delete-> {
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleSaveEditTask(): Task {
        val editTask = Task()

        editTask.title = edt_title.text.toString()
        editTask.description = edt_description_note.text.toString()
        editTask.checked = cb_completed.isChecked
        editTask.priority = indexRadioButton
        editTask.categorize = tv_uncategorized.text.toString()

        return editTask
    }

    private fun setInitialView() {
        val data = intent.extras
        val editTask = data.getParcelable(CODE_EDIT_TASK_POSITION) as Task

        edt_title.setText(editTask.title)
        edt_description_note.setText(editTask.description)
        cb_completed.isChecked = editTask.checked
        tv_uncategorized.text = editTask.categorize

        val btnLow = radio_priority_choice.findViewById<RadioButton>(R.id.btnLow)
        val btnNormal = radio_priority_choice.findViewById<RadioButton>(R.id.btnNormal)
        val btnHigh = radio_priority_choice.findViewById<RadioButton>(R.id.btnHigh)

        when (editTask.priority) {
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
