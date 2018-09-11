package com.example.hyunjujung.tbox.chatting;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *  [ 사용자가 어플에 로그인 하면 실행되는 채팅 서비스 ]
 *
 *  - 로그인 한후 사용자는 해당 아이피와 포트를 통해 소켓에 연결된다
 *  - ChatCallBack 과 IBinder 를 통해 다른 액티비티에서 채팅 메세지를 받을수 있도록 한다
 *
 */

public class ChatService extends Service {
    private static final String TAG = "ChatService";

    /* 서비스에서 발생하는 자원을 액티비티에서도 사용하기 위해 바인더를 이용한다 */
    private final IBinder mBinder = new LocalBinder();

    private static final String IP = "52.78.51.174";
    private static final int PORT = 9090;
    Socket socket;
    private BufferedReader socketIn;
    private BufferedWriter writer;
    private PrintWriter socketOut;

    /* 소켓 연결 후 받은 메시지 변수 */
    public String receiveMessage = "";

    /* 보내는 메시지 변수 */
    public String sendMessage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        /* 서비스 호출 */
        Log.e(TAG, "서비스 실행 시작");
        ConnectSocket connectSocket = new ConnectSocket();
        connectSocket.start();
    }

    /* 서비스 바인드
     *  - 서비스에서 소켓을 통해 받은 메세지를 가져오기 위해 바인드 해준다 */
    public class LocalBinder extends Binder {
        public ChatService getService() {
            return ChatService.this;
        }
    }
    /* 채팅 콜백 */
    public interface ChatCallBack {
        void receivedMessage();
    }
    private ChatCallBack chatCallBack;
    public void registerCallback(ChatCallBack back) {
        chatCallBack = back;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /* 소켓 연결하고 메세지 받는 부분 */
    public class ConnectSocket extends Thread {
        @Override
        public void run() {
            try {
                /* 소켓 연결 */
                socket = new Socket(IP, PORT);
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                socketOut = new PrintWriter(writer, true);

                while(true) {
                    receiveMessage = socketIn.readLine();
                    if(receiveMessage.length() == 0) {
                        continue;
                    }
                    if(chatCallBack != null) {
                        Log.e(TAG, "받은 메시지" + receiveMessage);
                        setReceiveMessage(receiveMessage);
                        chatCallBack.receivedMessage();
                    }else {
                        Log.e(TAG,  "chat Call back null");
                    }
                }
            }catch (Exception e) {
               e.printStackTrace();
            }
        }
    }

    /* 메세지 보내기 */
    public void sendMessageSocket(final JSONObject jsonMessage) {
        Log.e(TAG, "제이슨 메시지, " + jsonMessage.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    socketOut.println(jsonMessage.toString());
                    socketOut.flush();
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /* getter / setter */
    public String getReceiveMessage() {
        return receiveMessage;
    }

    public void setReceiveMessage(String receiveMessage) {
        this.receiveMessage = receiveMessage;
    }

    public String getSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(String sendMessage) {
        this.sendMessage = sendMessage;
    }
}
