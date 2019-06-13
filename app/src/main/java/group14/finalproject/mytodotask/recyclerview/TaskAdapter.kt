package group14.finalproject.mytodotask.recyclerview

import android.content.Context
import android.graphics.Paint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import group14.finalproject.mytodotask.R
import group14.finalproject.mytodotask.room.Task
import kotlinx.android.synthetic.main.item_task_main.view.*
import java.util.ArrayList

class TaskAdapter(var items: ArrayList<Task>, val context: Context) : RecyclerView.Adapter<TaskViewHolder>() {

    lateinit var mListener: TaskItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): TaskViewHolder {
        return TaskViewHolder(LayoutInflater.from(context).inflate(R.layout.item_task_main, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(taskViewHolder: TaskViewHolder, position: Int) {
        taskViewHolder.title.text = items[position].title
        taskViewHolder.description.text = items[position].description
        taskViewHolder.date.text = items[position].date
        taskViewHolder.checkBox.isChecked = items[position].checked
//        taskViewHolder.imageAttach.visibility = View.VISIBLE
//        taskViewHolder.imageReminder.visibility = View.VISIBLE
        if (taskViewHolder.checkBox.isChecked) taskViewHolder.title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
        else taskViewHolder.title.paintFlags = 0

        taskViewHolder.itemView.setOnClickListener {
            mListener.onItemClicked(position)
        }

        taskViewHolder.itemView.setOnLongClickListener {
            mListener.onItemLongClicked(position)
            true
        }

        taskViewHolder.checkBox.setOnClickListener {
            if (taskViewHolder.checkBox.isChecked) taskViewHolder.title.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            else taskViewHolder.title.paintFlags = 0
            mListener.onCheckBoxClicked(position, taskViewHolder.checkBox.isChecked)
        }
    }

    fun setListener(listener: TaskItemClickListener) {
        this.mListener = listener
    }

    fun setData(items: ArrayList<Task>) {
        this.items = items
    }

    fun appendData(newTaskAdded: Task) {
        this.items.add(newTaskAdded)
        notifyItemInserted(items.size - 1)
    }

}

class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    var checkBox = view.cb_item
    var title = view.tv_title_item
    var description = view.tv_description_item
    var date = view.tv_date_item
//    var imageAttach = view.imv_attach
//    var imageReminder = view.imv_reminder
}