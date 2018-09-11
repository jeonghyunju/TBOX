package com.example.hyunjujung.tbox.adapter.chatting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.chatting.ChatItemVO;

import org.json.JSONObject;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *  [ 채팅 메세지 출력하는 RecyclerView 의 어댑터 클래스 ]
 *
 *  - liveChatArray : 소켓에서 넘어오는 메세지나 보낸 메세지를 저장하는 ArrayList
 *
 */

public class Chat_Adapter extends RecyclerView.Adapter<Chat_Adapter.ViewHolder>{
    Context context;

    /* 라이브 방송일때 채팅 저장 Array */
    ArrayList<JSONObject> livechatArray;

    /* VOD 재생할때 해당 VOD 에 해당하는 채팅 저장 Array */
    ArrayList<ChatItemVO> vodChatArray;

    /* 라이브 방송인지, VOD 재생인지 판단하는 Flag */
    boolean isLive;

    String imagePath = "http://52.78.51.174/user_profile/";

    /* 오전, 오후를 구별하기 위한 변수 */
    String AM_PM = "";
    int clockInt = 0;

    /* VOD 재생시에 보일 채팅 목록 받는 생성자 */
    public Chat_Adapter(Context context, ArrayList<ChatItemVO> vodChatArray, boolean isLive) {
        this.context = context;
        this.vodChatArray = vodChatArray;
        this.isLive = isLive;
    }

    /* 라이브 영상시에 보일 채팅 목록 받는 생성자 */
    public Chat_Adapter(Context context, boolean isLive, ArrayList<JSONObject> livechatArray) {
        this.context = context;
        this.livechatArray = livechatArray;
        this.isLive = isLive;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.chatting_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if(isLive) {
            /* 라이브 방송일때 채팅 아이템 */
            try {
                if(livechatArray.get(position).getString("sendProfile").equals("null")) {
                    holder.chat_profile.setImageResource(R.drawable.ic_clickuser);
                }else {
                    Glide.with(context.getApplicationContext())
                            .load(imagePath + livechatArray.get(position).getString("sendProfile"))
                            .into(holder.chat_profile);
                }
                clockInt = Integer.parseInt(livechatArray.get(position).getString("messageDate").substring(11, 13));

                /* 0 <= clockInt <= 12 : 오전 */
                /* 12 < clockInt <= 24 : 오후 */
                if(0 <= clockInt && clockInt <= 12) {
                    AM_PM = "오전 ";
                    holder.chat_time.setText(AM_PM + livechatArray.get(position).getString("messageDate").substring(11, 16));
                }else if(12 < clockInt && clockInt <= 24) {
                    AM_PM = "오후 ";
                    holder.chat_time.setText(AM_PM + livechatArray.get(position).getString("messageDate").substring(11, 16));
                }

                holder.chat_ID.setText(livechatArray.get(position).getString("sendUser").substring(0, 4));
                holder.chat_message.setText(livechatArray.get(position).getString("message"));
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            /* VOD 재생할때 채팅 아이템 */
            if(vodChatArray.get(position).getSendProfile().equals("null")) {
                holder.chat_profile.setImageResource(R.drawable.ic_clickuser);
            }else {
                Glide.with(context.getApplicationContext())
                        .load(imagePath + vodChatArray.get(position).getSendProfile())
                        .into(holder.chat_profile);
            }
            clockInt = Integer.parseInt(vodChatArray.get(position).getmDate().substring(11, 13));

            if(0 <= clockInt && clockInt <= 12) {
                AM_PM = "오전 ";
                holder.chat_time.setText(AM_PM + vodChatArray.get(position).getmDate().substring(11, 16));
            }else if(12 < clockInt && clockInt <= 24){
                AM_PM = "오후 ";
                holder.chat_time.setText(AM_PM + vodChatArray.get(position).getmDate().substring(11, 16));
            }

            holder.chat_ID.setText(vodChatArray.get(position).getSendUser().substring(0, 4));
            holder.chat_message.setText(vodChatArray.get(position).getMessage());
        }
    }

    @Override
    public int getItemCount() {
        int chatsize = 0;
        if(isLive) {
            chatsize = livechatArray.size();
        }else {
            chatsize = vodChatArray.size();
        }
        return chatsize;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView chat_profile;
        public TextView chat_time, chat_ID, chat_message;

        public ViewHolder(View view) {
            super(view);
            chat_profile = (CircleImageView)view.findViewById(R.id.chat_profile);
            chat_time = (TextView)view.findViewById(R.id.chat_time);
            chat_ID = (TextView)view.findViewById(R.id.chat_ID);
            chat_message = (TextView)view.findViewById(R.id.chat_message);
        }
    }
}
