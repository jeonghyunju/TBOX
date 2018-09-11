package com.example.hyunjujung.tbox.chatting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *  [ 사용자가 어플을 켜면 서비스를 호출하여 사용자와 채팅 소켓을 연결한다 ]
 *
 */

public class ChatBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Intent rIntent = new Intent(context, ChatService.class);
            context.startService(rIntent);
        }
    }
}
