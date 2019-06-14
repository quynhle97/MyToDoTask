package group14.finalproject.mytodotask.otheractivity

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.RadioButton
import android.widget.Toast
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.recyclerview.TaskAdapter
import group14.finalproject.mytodotask.recyclerview.TaskItemClickListener
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.activity_multi_purpose.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class SearchActivity : AppCompatActivity() {
    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    var tasks: ArrayList<Task> = ArrayList()
    var tags: ArrayList<Tag> = ArrayList()
    var relationships: ArrayList<Relationship> = ArrayList()
    lateinit var checkedTags: BooleanArray
    lateinit var listTagsName: ArrayList<String>
    var numReqTags = 0
    lateinit var taskAdapter: TaskAdapter

    private lateinit var username: String

    // Flags
    var indexRadioButton = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_purpose)

        (application as MyApplication).getAppComponent().inject(this)
        tasks = repositoryHelper.getAllTasks() as ArrayList<Task>
        tags = repositoryHelper.getAllTags() as ArrayList<Tag>
        relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>

        listTagsName = ArrayList(tags.size)
        for (i in tags) listTagsName.add(i.tag)
        val listTags = arrayOfNulls<String>(listTagsName.size)
        listTagsName.toArray(listTags)
        checkedTags = BooleanArray(tags.size)

        username = SharedPreferencesHelper.readString(USERNAME_KEY)

        setupRecyclerView()

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }

        tv_select_tags.setOnClickListener {
            showDialogSelectTagsForEditTask(listTags, checkedTags)
        }

        btn_search.setOnClickListener {
            val titleSearch = edt_Title.text.toString()
            val filteredTasks = ArrayList<Task>()
            var matched: Boolean

            for (i in 0 until tasks.size) {
                matched = true
                if (!tasks[i].title.contains(titleSearch)) {
                    matched = false
                }
                if (matched) {
                    if (indexRadioButton == 0 && !tasks[i].checked) matched = false
                    if (indexRadioButton == 1 && tasks[i].checked) matched = false
                }
                if (matched) {
                    var curRels = relationships.filter{rel -> rel.note == tasks[i].id}
                    var count = 0
                    for (t in 0 until checkedTags.size) {
                        if (checkedTags[t]) {
                            for (rel in curRels)
                                if (tags[t].id == rel.tag) {
                                    count++
                                    break
                                }
                        }
                    }
                    if (count != numReqTags) matched = false
                }
                if (matched) filteredTasks.add(tasks[i])
            }
            Toast.makeText(this, "$checkedTags", Toast.LENGTH_SHORT).show()
            setTasksAdapter(filteredTasks)
        }
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
            this.checkedTags = checkedTags // Save to adapter

            var tmp = ""
            numReqTags = 0
            for (i in 0 until this.checkedTags.size) {
                if (this.checkedTags[i]) {
                    tmp += "\uD83C\uDFF7 ${listTagsName[i]} "
                    numReqTags++
                }
            }
            tv_select_tags.text = tmp
        }

        builder.setNegativeButton("Cancel") { dialog, which -> }

        val dialog = builder.create()
        dialog.show()
    }

    private fun getTagName(idInt: Int): String {
        return repositoryHelper.findByIdTag(idInt).tag
    }

    override fun onBackPressed() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setupRecyclerView() {
        rcv_items_search.layoutManager = LinearLayoutManager(this) as RecyclerView.LayoutManager
        taskAdapter = TaskAdapter(tasks, this)
        rcv_items_search.adapter = taskAdapter
        taskAdapter.setListener(taskItemClickListener)
    }

    private fun setTasksAdapter(allTasks: ArrayList<Task>) {
        Timer(false).schedule(500) {
            runOnUiThread {
                taskAdapter.setArrayListTask(allTasks)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

    private val taskItemClickListener = object : TaskItemClickListener {
        override fun onItemClicked(position: Int) {
//            val intent = Intent(this@MainActivity, DetailsTaskActivity::class.java)
            val intent = Intent(this@SearchActivity, TaskActivity::class.java)
            intent.putExtra(INDEX_NEW_DETAIL, 1)
            intent.putExtra(EDIT_TASK, tasks[position])
            intent.putExtra(EDIT_TASK_POSITION, position)
            startActivityForResult(intent, CODE_EDIT_TASK)
        }

        override fun onItemLongClicked(position: Int) {
            showDialogDeleteTask(position)
        }

        override fun onCheckBoxClicked(position: Int, state: Boolean) {
            handleOnCheckBoxClicked(position, state)
        }
    }

    private fun showDialogDeleteTask(position: Int) {
        val builder = AlertDialog.Builder(this@SearchActivity)
        builder.setTitle("Delete Confirmation")
            .setMessage("Are you sure to remove task title ${tasks[position].title}?")
            .setPositiveButton("OK") { _, _ ->
                repositoryHelper.deleteTask(taskAdapter.items[position]) // Local database
                repositoryHelper.removeTaskFirebaseDatabase(taskAdapter.items[position], username)
                removeTaskFromAdapter(position)
            }
            .setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog?.dismiss() }
        val myDialog = builder.create()
        myDialog.show()
    }

    private fun handleOnCheckBoxClicked(position: Int, state: Boolean) {
        taskAdapter.items[position].checked = state
        repositoryHelper.updateTask(tasks[position])
        repositoryHelper.writeTaskFirebaseDatabase(tasks[position], username)
    }

    private fun removeTaskFromAdapter(position: Int) {
        val tmp = taskAdapter.items.get(position)
        tasks.remove(tmp)
        taskAdapter.items.remove(tmp)

        taskAdapter.notifyItemRemoved(position)
        Timer(false).schedule(500) {
            runOnUiThread {
                taskAdapter.setArrayListTask(tasks)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CODE_EDIT_TASK && resultCode == Activity.RESULT_OK) {
            val codeEditTask = data?.extras?.getInt(EDIT_TASK_POSITION_KEY) as Int
            val codeDelTask = data?.extras?.getInt(DELETE_TASK_POSITION_KEY) as Int

            if (codeEditTask != -1 && codeDelTask == -1) {
                val editTask = data?.extras?.getParcelable(EDIT_TASK_KEY) as Task
                taskAdapter.setTask(editTask, codeEditTask)
                repositoryHelper.updateTask(editTask)                                           // Local Database
                if (username != USERNAME_DEFAULT)
                    repositoryHelper.writeTaskFirebaseDatabase(editTask, username)              // Firebase Database
            }

            if (codeEditTask == -1 && codeDelTask != -1) {
                repositoryHelper.deleteTask(tasks[codeDelTask])                                 // Local database
                if (username != USERNAME_DEFAULT)
                    repositoryHelper.removeTaskFirebaseDatabase(tasks[codeDelTask], username)   // Firebase Database
                removeTaskFromAdapter(codeDelTask)
            }
        }
    }
}
