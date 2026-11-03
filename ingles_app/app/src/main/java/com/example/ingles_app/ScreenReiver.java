package com.example.ingles_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ScreenReiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
            //Connection to DB with Room to extract one of the 5 cards of the day
            //So now we're gonna send data to test
            Intent serviceIntent = new Intent(context, FlashcardOverlayService.class);
            serviceIntent.putExtra("word", "Office");
            serviceIntent.putExtra("image_url", "https://server.png");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
        }
    }
}
