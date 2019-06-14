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
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.WindowManager
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    lateinit var relationships: ArrayList<Relationship>

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
//            val intent = Intent(this@MainActivity, NewTaskActivity::class.java)
            val intent = Intent(this@MainActivity, TaskActivity::class.java)
            intent.putExtra(INDEX_NEW_DETAIL, 0)
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
//            val intent = Intent(this@MainActivity, DetailsTaskActivity::class.java)
            val intent = Intent(this@MainActivity, TaskActivity::class.java)
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
                val intent = Intent(this@MainActivity, SearchActivity::class.java)
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
                initial()
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
            taskAdapter.appendTask(newTask)
        }
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

    private fun initial() {
        // Fix bugs in this
        // Update fakeItem to datachange and get data from database
        val fakeTask = Task(1001, "", false, "", "", 0, "", "", "", "", "", "")
        repositoryHelper.writeTaskFirebaseDatabase(fakeTask, username)
        repositoryHelper.removeTaskFirebaseDatabase(fakeTask, username)
        val fakeTag = Tag(1001, "")
        repositoryHelper.writeTagFirebaseDatabase(fakeTag, username)
        repositoryHelper.removeTagFirebaseDatabase(fakeTag, username)
        val fakeRel = Relationship(1001, 1001, 1001)
        repositoryHelper.writeRelationshipFirebaseDatabase(fakeRel, username)
        repositoryHelper.removeRelationshipFirebaseDatabase(fakeRel, username)

        // Get database from Firebase or Roomdatabase
        if (username != USERNAME_DEFAULT) {
            tasks = repositoryHelper.getAllTasks() as ArrayList<Task>
            tags = repositoryHelper.getAllTags() as ArrayList<Tag>
            relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>

            if (tasks.size == 0 && tags.size == 0 && relationships.size == 0) {
                tasks = repositoryHelper.getTasksFirebaseDatabase(username)
                tags = repositoryHelper.getTagsFirebaseDatabase(username)
                relationships = repositoryHelper.getRelationshipsFirebaseDatabase(username)
                for (i in tasks) {
                    repositoryHelper.insertTask(i)
                    Log.d("initial tasks", tasks.toString())
                }
                for (i in tags) {
                    repositoryHelper.insertTag(i)
                    Log.d("initial tags", tags.toString())
                }
                for (i in relationships) {
                    repositoryHelper.insertRelationship(i)
                    Log.d("initial relationships", relationships.toString())
                }
            }
        } else {
            tasks = repositoryHelper.getAllTasks() as ArrayList<Task>
            tags = repositoryHelper.getAllTags() as ArrayList<Tag>
            relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>
        }
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
        tasks = ArrayList()
        tags = ArrayList()
        relationships = ArrayList()
        repositoryHelper.deleteAll()                                        // Delete Local Database
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
            val id = repositoryHelper.insertTag(newTag)                                       // Add Local Database
            newTag.id = id.toInt()
            if (username != USERNAME_DEFAULT)
                repositoryHelper.writeTagFirebaseDatabase(newTag, username)                         // Add Firebase Database
            tags.add(newTag)                                                                        // Add dialog view
            Toast.makeText(applicationContext,"New tag added: $tagName", Toast.LENGTH_SHORT).show()
        }
        mAlertDialog.btnCancelDialog.setOnClickListener{
            mAlertDialog.dismiss()
        }
    }

    private fun showDialogListOfTags() {
        this.tags = ArrayList()
        tags = repositoryHelper.getAllTags() as ArrayList<Tag>
        Log.d("show dialog tags", tags.toString())
        this.relationships = ArrayList()
        relationships = repositoryHelper.getAllRelationships() as ArrayList<Relationship>
        Log.d("show dialog tags", relationships.toString())

        // Save flag as tag can't be deleted: True - Can't be deleted and False - Can be deleted
        var arrTaged = ArrayList<Boolean>(tags.size)
        for (i in 0 until tags.size) {
            arrTaged.add(false)
        }
        for (i in 0 until arrTaged.size) {
            for (j in 0 until relationships.size)
                if (getTagName(relationships[j].tag) == tags[i].tag) {
                    arrTaged[i] = true
                }
        }

        val array = ArrayList<String>()
        for (i in tags) {
            array.add(i.tag)
        }
        val listTags = arrayOfNulls<String>(array.size)
        array.toArray(listTags)
        var checkedTags = BooleanArray(tags.size)

        for (i in 0 until tags.size) {
            checkedTags[i] = false
        }

        val builder = AlertDialog.Builder(this)
        builder.setMultiChoiceItems(listTags, checkedTags) { dialog, which, isChecked ->
            Toast.makeText(applicationContext, "$which - $isChecked", Toast.LENGTH_SHORT).show()
            checkedTags[which] = isChecked
        }

        builder.setCancelable(false)
        builder.setTitle("List of Tags")

        builder.setPositiveButton("Delete") { dialog, which ->
            // delete tag from Database Tag
            for (i in 0 until tags.size) {
                if (checkedTags[i] && !arrTaged[i]) {
                    // delete database tags
                    repositoryHelper.deleteTag(tags[i])                                 // Local Database
                    if (username != USERNAME_DEFAULT)
                        repositoryHelper.removeTagFirebaseDatabase(tags[i], username)   // Firebase Database
                    tags.removeAt(i)                                                    // Tags Adapter for View
                }
            }
            Toast.makeText(applicationContext, "Tag attached won't be deleted", Toast.LENGTH_SHORT).show()
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

    private fun showDialogDeleteTask(position: Int) {
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

    private fun handleOnCheckBoxClicked(position: Int, state: Boolean) {
        tasks[position].checked = state
        repositoryHelper.updateTask(tasks[position])
        repositoryHelper.writeTaskFirebaseDatabase(tasks[position], username)
    }

    private fun getTagName(idInt: Int): String {
        return repositoryHelper.findByIdTag(idInt).tag
    }
}
