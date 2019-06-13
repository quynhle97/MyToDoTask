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
import android.view.LayoutInflater
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import group14.finalproject.mytodotask.otheractivity.*
import group14.finalproject.mytodotask.recyclerview.TaskAdapter
import group14.finalproject.mytodotask.recyclerview.TaskItemClickListener
import group14.finalproject.mytodotask.repo.RepositoryHelper
import group14.finalproject.mytodotask.room.*
import group14.finalproject.mytodotask.sharedpreferences.SharedPreferencesHelper
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.*
import kotlinx.android.synthetic.main.dialog_add_new_tag.view.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class MainActivity: AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    @Inject
    lateinit var repositoryHelper: RepositoryHelper

    lateinit var tasks: ArrayList<Task>
    lateinit var taskAdapter: TaskAdapter

    lateinit var tags: ArrayList<Tag>

    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupInitialView()

        username = SharedPreferencesHelper.readString(USERNAME_KEY)

        // It assigns references in our activities, services, or fragments to have access to singletons we earlier defined
        (application as MyApplication).getAppComponent().inject(this)

        initial()
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
                    repositoryHelper.removeTaskFirebaseDatabase(tasks[position], username)
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
        signOut()

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
                showDialogCreateNewTag()
            }
            R.id.nav_list_tag -> {
                showDialogListOfTags()
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
                signOut()
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

    private fun initial() {
        tasks = repositoryHelper.getAllTasks() as ArrayList<Task>
        tags = repositoryHelper.getAllTags() as ArrayList<Tag>
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        rcv_list_tasks.layoutManager = LinearLayoutManager(this)
        taskAdapter = TaskAdapter(tasks, this)
        rcv_list_tasks.adapter = taskAdapter
        taskAdapter.setListener(taskItemClickListener)
    }

    private fun signOut() {
        SharedPreferencesHelper.clearUser()
        repositoryHelper.deleteAll()                                    // Delete Local Database
        repositoryHelper.removeAllTasksFirebaseDatabase(username)       // Delete Tasks Firebase Database
        repositoryHelper.removeAllTagsFirebaseDatabase(username)        // Delete Tags Firebase Database
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

    private fun showDialogListOfTags() {
        val listTags = repositoryHelper.getAllTags()
        val listTagsString = ArrayList<String>()
        for (i in listTags) listTagsString.add(i.tag)
        val tagsDialog = convertToArrayString(listTagsString)
        val checkedTagsDialog = BooleanArray(tagsDialog.size)
        for (i in 0 until checkedTagsDialog.size) checkedTagsDialog[i] = false

        val builder = AlertDialog.Builder(this)
        builder.setMultiChoiceItems(tagsDialog, checkedTagsDialog) { dialog, which, isChecked ->
            checkedTagsDialog[which] = isChecked
        }

        builder.setCancelable(false)
        builder.setTitle("List of Tags")

        builder.setPositiveButton("Delete") { dialog, which ->
            for (i in 0 until tagsDialog.size) {
                if (checkedTagsDialog[i]) {
                    Toast.makeText(applicationContext, "Tag ${tags[i]} is deleted", Toast.LENGTH_SHORT).show()
                    repositoryHelper.deleteTag(tags[i])                                 // Delete tags local database
                    repositoryHelper.removeTagFirebaseDatabase(tags[i], username)       // Delete tags firebase database
                    tags.removeAt(i)                                                    // Delete tags view
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->

        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun convertToArrayString(arrayList: ArrayList<String>): Array<String?> {
        val array = arrayOfNulls<String>(arrayList.size)
        arrayList.toArray(array)
        return array
    }
}
