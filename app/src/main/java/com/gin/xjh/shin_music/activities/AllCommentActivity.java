package com.gin.xjh.shin_music.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.gin.xjh.shin_music.R;
import com.gin.xjh.shin_music.adapter.CommentRecyclerViewAdapter;
import com.gin.xjh.shin_music.bean.Comment;
import com.gin.xjh.shin_music.bean.Song;
import com.gin.xjh.shin_music.bean.User;
import com.gin.xjh.shin_music.user.UserState;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

public class AllCommentActivity extends BaseActivity implements View.OnClickListener {

    private ImageView mGoBack, mWriteComment;
    private RecyclerView mCommentRv;

    private List<Comment> mCommentList;
    private CommentRecyclerViewAdapter mCommentRecyclerViewAdapter;

    private Song mSong;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comment);
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra(getString(R.string.SONG));
        mSong = (Song) bundle.get(getString(R.string.SONG));
        initView();
        initData();
    }

    private void initView() {
        mGoBack = findViewById(R.id.go_back);
        mWriteComment = findViewById(R.id.write_comment);
        mCommentRv = findViewById(R.id.comment_rv);
        mGoBack.setOnClickListener(this);
        mWriteComment.setOnClickListener(this);
    }

    private void initData() {
        BmobQuery<Comment> query = new BmobQuery<>();
        query.setCachePolicy(BmobQuery.CachePolicy.CACHE_THEN_NETWORK);
        query.setMaxCacheAge(86400000);//缓存有1天的有效期
        query.addWhereEqualTo(getString(R.string.SONG_ID), mSong.getSongId());
        query.findObjects(new FindListener<Comment>() {
            @Override
            public void done(List<Comment> list, BmobException e) {
                if (e == null) {
                    Collections.sort(list, new SortByTime());
                    mCommentList = list;
                    initEvent();
                }
            }
        });
    }

    private void initEvent() {
        mCommentRecyclerViewAdapter = new CommentRecyclerViewAdapter(this, mCommentList);
        mCommentRv.setLayoutManager(new LinearLayoutManager(this));
        mCommentRv.setItemAnimator(new DefaultItemAnimator());//默认动画
        mCommentRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mCommentRv.setAdapter(mCommentRecyclerViewAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.go_back:
                finish();
                break;
            case R.id.write_comment:
                //判读是否有资格写评论，如果没有提示需要登入
                if (!UserState.getState()) {
                    Toast.makeText(this, "需要登录后才具有评论功能", Toast.LENGTH_SHORT).show();
                    break;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(AllCommentActivity.this);
                LayoutInflater inflater = LayoutInflater.from(AllCommentActivity.this);
                View viewDialog = inflater.inflate(R.layout.dialog_add_comment, null);
                final EditText Personal_profile = viewDialog.findViewById(R.id.Personal_profile);
                builder.setView(viewDialog);
                builder.setTitle("添加评论(100字以内)：");
                builder.setPositiveButton("提交评论", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Date date = new Date();
                        User user = UserState.getLoginUser();

                        //评论内容的判空
                        String comment = Personal_profile.getText().toString();
                        if (comment.length() == 0 && comment.equals("")) {
                            Toast.makeText(AllCommentActivity.this, "请输入内容后点击提交", Toast.LENGTH_SHORT).show();
                        } else {
                            final Comment mComment = new Comment(user.getUserName(), user.getUserId(), mSong.getSongId(), comment, date.getTime());
                            mComment.save(new SaveListener<String>() {
                                @Override
                                public void done(String s, BmobException e) {
                                    //更新评论列表
                                    if (e == null) {
                                        mCommentRecyclerViewAdapter.addData(mComment);
                                    }
                                }
                            });
                        }
                    }
                });
                builder.setNegativeButton("取消", null);
                builder.create();
                builder.show();
                break;
        }
    }

    private class SortByTime implements java.util.Comparator {
        @Override
        public int compare(Object o1, Object o2) {
            Comment a = (Comment) o1;
            Comment b = (Comment) o2;
            return -a.getTimes().compareTo(b.getTimes());
        }
    }
}
