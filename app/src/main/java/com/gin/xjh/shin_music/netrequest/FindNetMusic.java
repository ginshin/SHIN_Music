package com.gin.xjh.shin_music.netrequest;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.gin.xjh.shin_music.R;
import com.gin.xjh.shin_music.interfaces.RequestServicesFindMusicInter;
import com.gin.xjh.shin_music.adapter.MusicRecyclerViewAdapter;
import com.gin.xjh.shin_music.bean.Song;
import com.gin.xjh.shin_music.utils.ConstantUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FindNetMusic {

    private RecyclerView mRecyclerView;
    private TextView mListHint;
    private Context mContext;

    private List<Song> mSongList;
    private MusicRecyclerViewAdapter mMusicListViewAdapter;

    private static final int REQUEST_SUCCESS = 200;

    private Handler mMainHandler;

    private void obtainMainHandler() {
        if (mMainHandler != null) {
            return;
        }
        mMainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == REQUEST_SUCCESS) {
                    try {
                        String result = (String) msg.obj;
                        JSONObject AllObject = new JSONObject(result);
                        String ListString = AllObject.getString(mContext.getString(R.string.RESULT));
                        JSONObject ListObject = new JSONObject(ListString);
                        String JSONString = ListObject.getString(mContext.getString(R.string.SONGS));
                        JSONArray jsonArray = new JSONArray(JSONString);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            //歌手
                            String ar = jsonObject.getString(mContext.getString(R.string.ARTISTS));
                            JSONArray arArray = new JSONArray(ar);
                            JSONObject arObject = arArray.getJSONObject(0);
                            String Singer = arObject.getString(mContext.getString(R.string.NAME));
                            Long SingerId = arObject.getLong(mContext.getString(R.string.ID));

                            //专辑
                            String al = jsonObject.getString(mContext.getString(R.string.ALBUM));
                            Long id = jsonObject.getLong(mContext.getString(R.string.ID));
                            JSONObject alObject = new JSONObject(al);
                            String AlbumName = alObject.getString(mContext.getString(R.string.NAME));
                            Long time = alObject.getLong(mContext.getString(R.string.PUBLISH_TIME));
                            Song song = new Song(jsonObject.getString(mContext.getString(R.string.NAME)), jsonObject.getLong(mContext.getString(R.string.ID)), Singer, SingerId, AlbumName, null, 0);
                            song.setAlbumTime(time);
                            song.setAlbumId(id);
                            mSongList.add(song);
                        }

                        //RecyclerView
                        mMusicListViewAdapter = new MusicRecyclerViewAdapter(mContext, mSongList);
                        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
                        mRecyclerView.setItemAnimator(new DefaultItemAnimator());//默认动画
                        mRecyclerView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
                        mRecyclerView.setAdapter(mMusicListViewAdapter);

                        //取消加载提醒
                        mListHint.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public void getJson(String name, View mRecyclerView, View list_hint, Context mContext) {
        this.mRecyclerView = (RecyclerView) mRecyclerView;
        this.mListHint = (TextView) list_hint;
        this.mContext = mContext;
        mSongList = new ArrayList<>();
        obtainMainHandler();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ConstantUtil.URL_BASE)
                .build();
        RequestServicesFindMusicInter requestServices = retrofit.create(RequestServicesFindMusicInter.class);
        retrofit2.Call<ResponseBody> call = requestServices.getString(name, 1);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        //返回的结果保存在response.body()中
                        Message msg = new Message();
                        msg.what = REQUEST_SUCCESS;
                        msg.obj = response.body().string();
                        mMainHandler.sendMessage(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
                Log.i("NET", "访问失败");
            }
        });
    }

}
