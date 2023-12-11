package com.dicoding.todoapp.ui.detail

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.ui.ViewModelFactory
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.TASK_ID

class DetailTaskActivity : AppCompatActivity() {
    private lateinit var vM : DetailTaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_detail)

        //TODO 11 : Show detail task and implement delete action

        val judul = findViewById<TextView>(R.id.detail_ed_title)
        val desc = findViewById<TextView>(R.id.detail_ed_description)
        val dueDate = findViewById<TextView>(R.id.detail_ed_due_date)
        val hps = findViewById<Button>(R.id.btn_delete_task)


        val fact = ViewModelFactory.getInstance(this)
        vM = ViewModelProvider(this, fact)[DetailTaskViewModel::class.java]

       val taskId = intent.getIntExtra(TASK_ID,0)
        val task = taskData(taskId)

        task.observe(this, Observer { task ->
            if (task != null) {
                hps.setOnClickListener { vM.deleteTask(task) }
                judul.text = task.title
                desc.text = task.description
                dueDate.text = DateConverter.convertMillisToString(task.dueDateMillis)
            } else {
                finish()
            }
        })
    }




    private fun taskData(taskId: Int): LiveData<Task> {
        vM.setTaskId(taskId)
        val data = vM.task
        return data
    }
}
