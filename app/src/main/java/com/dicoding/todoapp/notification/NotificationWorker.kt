package com.dicoding.todoapp.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.preference.PreferenceManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dicoding.todoapp.R
import com.dicoding.todoapp.data.Task
import com.dicoding.todoapp.data.TaskRepository
import com.dicoding.todoapp.ui.detail.DetailTaskActivity
import com.dicoding.todoapp.utils.DateConverter
import com.dicoding.todoapp.utils.NOTIFICATION_CHANNEL_ID
import com.dicoding.todoapp.utils.TASK_ID


class NotificationWorker(ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    private val channelName = inputData.getString(NOTIFICATION_CHANNEL_ID)

    private fun getPendingIntent(task: Task): PendingIntent? {
        val intent = Intent(applicationContext, DetailTaskActivity::class.java).apply {
            putExtra(TASK_ID, task.id)
        }
        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(intent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            } else {
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }
    }

    override fun doWork(): Result {
        //TODO 14 : If notification preference on, get nearest active task from repository and show notification with pending intent
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val notPref = preferences.getBoolean(applicationContext.getString(R.string.pref_key_notify),false)

        if (notPref){
            val nearTask = TaskRepository.getInstance(context = applicationContext).getNearestActiveTask()
            getPendingIntent(nearTask)?.let {
                Notification(
                    applicationContext,
                    task = nearTask,
                    pendIntent = it

                )
            }
        }


        return Result.success()
    }

    private fun Notification (context: Context, task: Task, pendIntent : PendingIntent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val aChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Channel Name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            aChannel.description = "Pengingat"
            val notifManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notifManager.createNotificationChannel(aChannel)
        }
            val notBuilder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID).setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(task.title).setContentIntent(pendIntent).setContentText(context.getString(
                    R.string.notify_content, DateConverter.convertMillisToString(
                        task.dueDateMillis
                    ))).setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(context)){
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                notify(1,notBuilder.build())
            }

        }

}
