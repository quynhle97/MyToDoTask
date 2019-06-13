package group14.finalproject.mytodotask.otheractivity

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
    var checkedTags: ArrayList<Boolean> = ArrayList()
    lateinit var reqTags: ArrayList<Tag>

    // Flags
    var indexRadioButton = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_purpose)

        (application as MyApplication).getAppComponent().inject(this)

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radio: RadioButton = group.findViewById(checkedId)
            indexRadioButton = group.indexOfChild(radio)
        }
    }
}
