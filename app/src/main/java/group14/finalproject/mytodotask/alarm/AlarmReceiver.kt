package group14.finalproject.mytodotask.alarm

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Vibrator
import android.provider.Settings
import group14.finalproject.mytodotask.R

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val vibrate = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate.vibrate(6000)
        val notify = Notification.Builder(context)
            .setContentTitle("Alarm MyToDoTask")
            .setContentText("You need check App")
            .setSmallIcon(R.drawable.ic_launcher) as Notification
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notify.flags = Notification.FLAG_AUTO_CANCEL
        notificationManager.notify(0, notify)
        val ringtone = RingtoneManager.getRingtone(context, Settings.System.DEFAULT_RINGTONE_URI)   as Ringtone

        val notification = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE) as Uri
        val currentRingtone = RingtoneManager.getRingtone(context, notification) as Ringtone
        currentRingtone.play()
        ringtone.play()
    }
}