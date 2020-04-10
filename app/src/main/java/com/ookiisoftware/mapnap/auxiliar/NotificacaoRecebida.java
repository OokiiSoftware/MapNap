package com.ookiisoftware.mapnap.auxiliar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

public class NotificacaoRecebida extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Intent pushIntent = new Intent(context, SegundoPlanoService.class);
            context.startService(pushIntent);
            NotificationManagerCompat notificationCompat = NotificationManagerCompat.from(context);
            notificationCompat.cancel(1);
        }
    }
}
