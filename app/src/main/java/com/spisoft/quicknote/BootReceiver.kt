package com.spisoft.quicknote

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.spisoft.quicknote.reminders.RemindersManager
import com.spisoft.sync.synchro.SynchroService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        RemindersManager.getInstance(context)!!.onBoot()
        try {
            SynchroService.startIfNeeded(context)
        } catch(ex:IllegalStateException){

        }
    }
}
