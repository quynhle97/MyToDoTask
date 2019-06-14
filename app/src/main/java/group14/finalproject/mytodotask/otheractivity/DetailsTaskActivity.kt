package group14.finalproject.mytodotask.otheractivity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.activity_details_task.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.view.*
import javax.inject.Inject
import android.widget.Toast
import com.appeaser.sublimepickerlibrary.helpers.SublimeListenerAdapter
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker
import com.appeaser.sublimepickerlibrary.SublimePicker
import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions
import com.appeaser.sublimepickerlibrary.recurrencepicker.EventRecurrence
import com.google.ical.compat.javautil.DateIteratorFactory
import group14.finalproject.mytodotask.fragments.SublimePickerFragment


class DetailsTaskActivity : AppCompatActivity() {
    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    private lateinit var username: String

    var indexRadioButton: Int = 1
    var indexItemClicked: Int = -1
    var idItemClicked: Int = -1

    var reminderTime: Date? = null
    var alarmTime: Date? = null
    var repeat: String = ""

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

        // Sublime Picker
        val mListener = object : SublimeListenerAdapter() {
            override fun onDateTimeRecurrenceSet(
                sublimeMaterialPicker: SublimePicker?,
                selectedDate: SelectedDate?,
                hourOfDay: Int,
                minute: Int,
                recurrenceOption: SublimeRecurrencePicker.RecurrenceOption?,
                recurrenceRule: String?
            ) {
                Toast.makeText(applicationContext, recurrenceRule, Toast.LENGTH_LONG).show()
            }

            override fun onCancelled() {
                // Handle click on `Cancel` button
            }
        }

        var sublimePicker = SublimePicker(this)
        var sublimeOptions = SublimeOptions() // This is optional
        sublimeOptions.setPickerToShow(SublimeOptions.Picker.REPEAT_OPTION_PICKER) // I want the recurrence picker to show.
        sublimeOptions.setDisplayOptions(SublimeOptions.ACTIVATE_RECURRENCE_PICKER) // I only want the recurrence picker, not the date/time pickers.
        sublimePicker.initializePicker(sublimeOptions,mListener)

        val mFragmentCallback = object : SublimePickerFragment.Callback {
            override fun onCancelled() {
            }

            override fun onDateTimeRecurrenceSet(
                selectedDate: SelectedDate?,
                hourOfDay: Int,
                minute: Int,
                recurrenceOption: SublimeRecurrencePicker.RecurrenceOption?,
                recurrenceRule: String?
            ) {
                when (recurrenceOption) {
                    SublimeRecurrencePicker.RecurrenceOption.DOES_NOT_REPEAT -> {
                        repeat = ""
                        tv_repeat.text = ""
                    }
                    SublimeRecurrencePicker.RecurrenceOption.DAILY -> {
                        repeat = "FREQ=DAILY"
                        tv_repeat.text = "Daily"
                    }
                    SublimeRecurrencePicker.RecurrenceOption.WEEKLY -> {
                        repeat = "FREQ=WEEKLY"
                        tv_repeat.text = "Weekly"
                    }
                    SublimeRecurrencePicker.RecurrenceOption.MONTHLY -> {
                        repeat = "FREQ=MONTHLY"
                        tv_repeat.text = "Monthly"
                    }
                    SublimeRecurrencePicker.RecurrenceOption.YEARLY -> {
                        repeat = "FREQ=YEARLY"
                        tv_repeat.text = "Yearly"
                    }
                    SublimeRecurrencePicker.RecurrenceOption.CUSTOM -> {
                        repeat = "$recurrenceRule"
                        tv_repeat.text = "Custom"
                    }
                }
                var res = ""
                if (repeat != "") {
                    val tmp = "RRULE:${repeat};COUNT=1"
                    var di = DateIteratorFactory.createDateIterable(
                        tmp,
                        reminderTime,
                        TimeZone.getDefault(),
                        true
                    )
                    res = di.first().toString()
                }
                Toast.makeText(applicationContext, res, Toast.LENGTH_LONG).show()
            }
        }

        tv_repeat.setOnClickListener {
            if (reminderTime == null) {
                Toast.makeText(applicationContext, "Please choose reminder time first!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            val pickerFrag = SublimePickerFragment();
            pickerFrag.setCallback(mFragmentCallback)
            // Options
            val options = SublimeOptions();
            options.pickerToShow = SublimeOptions.Picker.REPEAT_OPTION_PICKER
            options.setDisplayOptions(SublimeOptions.ACTIVATE_RECURRENCE_PICKER)

            // Valid options
            val bundle = Bundle();
            bundle.putParcelable("SUBLIME_OPTIONS", options);
            pickerFrag.arguments = bundle;

            pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            pickerFrag.show(supportFragmentManager, "SUBLIME_PICKER");
        }

        tv_add_tags.setOnClickListener {
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

//        tv_repeat.setOnClickListener()

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
                    val simpleDateTime = SimpleDateFormat("hh:mm dd/mm/yy", Locale.ENGLISH)
                    tv_add_reminder.text = simpleDateTime.format(reminderTime)
                }, year, month, day)
                datePickerDialog.setTitle("Select date")
                datePickerDialog.show()
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

    private fun getInitialDatabaseTagsAndRelationships() {
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
