package group14.finalproject.mytodotask

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.view.MenuItem
import android.support.v4.widget.DrawerLayout
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.widget.Toast
import group14.finalproject.mytodotask.otheractivity.*
import group14.finalproject.mytodotask.recyclerview.TaskAdapter
import group14.finalproject.mytodotask.recyclerview.TaskItemClickListener
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.content_main.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MainActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    lateinit var tasks: ArrayList<Task>
    lateinit var taskAdapter: TaskAdapter

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupInitialView()

        username = SharedPreferencesHelper.readString(USERNAME_KEY).substringBeforeLast("@")

        // It assigns references in our activities, services, or fragments to have access to singletons we earlier defined
        (application as MyApplication).getAppComponent().inject(this)

        initialTaskAdapter()
    }

    private fun setupInitialView() {
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            val intent = Intent(this@MainActivity, NewTaskActivity::class.java)
            startActivityForResult(intent, CODE_ADD_NEW_TASK)
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
    }

    private val taskItemClickListener = object : TaskItemClickListener {
        override fun onItemClicked(position: Int) {
            val intent = Intent(this@MainActivity, DetailsTaskActivity::class.java)
            intent.putExtra(EDIT_TASK, tasks[position])
            intent.putExtra(EDIT_TASK_POSITION, position)
            startActivityForResult(intent, CODE_EDIT_TASK)
        }

        override fun onItemLongClicked(position: Int) {
            val builder = AlertDialog.Builder(this@MainActivity)
            builder.setTitle("Delete Confirmation")
                .setMessage("Are you sure to remove task title ${tasks[position].title}?")
                .setPositiveButton("OK") { _, _ ->
                    repositoryHelper.deleteTask(tasks[position]) // Local database
                    removeTaskFromAdapter(position)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialog, _ -> dialog?.dismiss() }
            val myDialog = builder.create()
            myDialog.show()
        }

        override fun onCheckBoxClicked(position: Int, state: Boolean) {
        }
    }

    override fun onBackPressed() {
        Toast.makeText(applicationContext,"SIGN OUT", Toast.LENGTH_SHORT).show()
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                val intent = Intent(this@MainActivity, MultiPurposeActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_clear_completed->true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_tasks -> {

            }
            R.id.nav_new_tag -> {

            }
            R.id.nav_list_tag -> {

            }
            R.id.nav_new_filter -> {

            }
            R.id.nav_uncategorized -> {

            }
            R.id.nav_settings -> {

            }
            R.id.nav_about -> {
                val intent = Intent(this@MainActivity, AboutActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_sign_out -> {
                SharedPreferencesHelper.clearUser()
                repositoryHelper.deleteAll()
                startActivity(Intent(this@MainActivity, FirstActivity::class.java))
            }
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CODE_ADD_NEW_TASK && resultCode == Activity.RESULT_OK) {
            val newTask = data?.extras?.getParcelable(NEW_TASK_KEY) as Task
            val id = repositoryHelper.insertTask(newTask) // Local Database
            newTask.id = id.toInt()
            taskAdapter.appendTask(newTask)
            if (username != USERNAME_DEFAULT)
                repositoryHelper.writeTaskFirebaseDatabase(newTask, username) // Firebase Database
        }
        if (requestCode == CODE_EDIT_TASK && resultCode == Activity.RESULT_OK) {
            val editTask = data?.extras?.getParcelable(EDIT_TASK_KEY) as Task
            val itemClickPosition = data?.extras?.getInt(EDIT_TASK_POSITION_KET) as Int
            taskAdapter.setTask(editTask, itemClickPosition)
            repositoryHelper.updateTask(editTask) // Local Database
            if (username != USERNAME_DEFAULT)
                repositoryHelper.writeTaskFirebaseDatabase(editTask, username) // Firebase Database
        }
    }

    private fun initialTaskAdapter() {
        tasks = repositoryHelper.getAllTasks() as ArrayList<Task>
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        rcv_list_tasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(tasks, this)
        rcv_list_tasks.adapter = taskAdapter
        taskAdapter.setListener(taskItemClickListener)
    }

    private fun removeTaskFromAdapter(position: Int) {
        tasks.removeAt(position)

        taskAdapter.notifyItemRemoved(position)
        Timer(false).schedule(500) {
            runOnUiThread {
                taskAdapter.setArrayListTask(tasks)
                taskAdapter.notifyDataSetChanged()
            }
        }
    }
}
