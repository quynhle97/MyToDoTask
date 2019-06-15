package group14.finalproject.mytodotask.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Vibrator
import android.provider.Settings

class RepeatAlarmReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val vibrate = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrate.vibrate(2000)
        val mediaPlayer = MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI)
        mediaPlayer.start()
    }
}