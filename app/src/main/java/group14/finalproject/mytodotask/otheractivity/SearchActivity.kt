package group14.finalproject.mytodotask.otheractivity

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.RadioButton
import android.widget.Toast
import group14.finalproject.mytodotask.*
import group14.finalproject.mytodotask.recyclerview.TaskAdapter
import group14.finalproject.mytodotask.recyclerview.TaskItemClickListener
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
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
    var checkedTags: ArrayList<Boolean> = ArrayList()
    lateinit var reqTags: ArrayList<Tag>
    lateinit var taskAdapter: TaskAdapter

    // Flags
    var indexRadioButton = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_purpose)

        (application as MyApplication).getAppComponent().inject(this)
        tasks = repositoryHelper.getAllTasks() as ArrayList<Task>
        tags = repositoryHelper.getAllTags() as ArrayList<Tag>
        relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>

        setupRecyclerView()

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }

        tv_select_tags.setOnClickListener {

        }

        btn_search.setOnClickListener {
            val titleSearch = edt_Title.text.toString()
            val arrListTasks = repositoryHelper.getAllTasks() as ArrayList<Task>
            var checkedSearch: ArrayList<Boolean> = ArrayList()
            for (i in 0 until arrListTasks.size)
                checkedSearch.add(true)
            for (i in 0 until arrListTasks.size) {
                checkedSearch[i] = arrListTasks[i].title.contains(titleSearch)
            }
            Toast.makeText(this, "$checkedTags", Toast.LENGTH_SHORT).show()
            var completed: Boolean = false
            if (indexRadioButton == 0) completed = true
            if (indexRadioButton != 2) {
                for (i in 0 until checkedSearch.size) {
                    if (checkedSearch[i]) {
                        checkedSearch[i] = (arrListTasks[i].checked == completed)
                    } else  {
                        checkedSearch[i] = false
                    }
                }
            }
            val finalTasks = ArrayList<Task>()
            for (i in 0 until checkedSearch.size)
                if (checkedSearch[i])
                    finalTasks.add(arrListTasks[i])
            setTasksAdapter(finalTasks)
        }
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
        }

        override fun onItemLongClicked(position: Int) {
        }

        override fun onCheckBoxClicked(position: Int, state: Boolean) {
        }
    }
}
