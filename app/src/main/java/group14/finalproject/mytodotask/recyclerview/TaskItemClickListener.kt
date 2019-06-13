package group14.finalproject.mytodotask.recyclerview

interface TaskItemClickListener {
    fun onItemClicked(position: Int)
    fun onItemLongClicked(position: Int)
    fun onCheckBoxClicked(position: Int, state: Boolean)
}