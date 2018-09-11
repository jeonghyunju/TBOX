package com.example.hyunjujung.tbox.adapter.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hyunjujung.tbox.R;
import com.example.hyunjujung.tbox.data_vo.user.BookmarkVO;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 *  [ 로그인 사용자 타임라인의 북마크 목록 어댑터 클래스 ]
 *
 *  - 아이템에는 북마크한 BJ의 프로필, 아이디가 포함된다
 *
 */
public class Bookmark_Adapter extends RecyclerView.Adapter<Bookmark_Adapter.ViewHolder>{
    static String PATH_URL = "http://52.78.51.174/";
    Context context;
    ArrayList<BookmarkVO> bookmarkArray;

    public Bookmark_Adapter(Context context, ArrayList<BookmarkVO> bookmarkArray) {
        this.context = context;
        this.bookmarkArray = bookmarkArray;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.bookmark_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Glide.with(context.getApplicationContext())
                .load(PATH_URL + "user_profile/" + bookmarkArray.get(position).getBookmarkId())
                .into(holder.bookmark_pro); //  북마크한 BJ의 프로필
        holder.bookmark_id.setText(bookmarkArray.get(position).getBookmarkPro().substring(0, 4)); // 북마크한 BJ의 아이디
    }

    @Override
    public int getItemCount() {
        return bookmarkArray.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView bookmark_pro;
        public TextView bookmark_id;

        public ViewHolder(View itemView) {
            super(itemView);
            bookmark_pro = (CircleImageView)itemView.findViewById(R.id.bookmark_pro);
            bookmark_id = (TextView)itemView.findViewById(R.id.bookmark_id);
        }
    }

    public void updateBookmark(ArrayList<BookmarkVO> arrayList) {
        this.bookmarkArray = arrayList;
    }
}
