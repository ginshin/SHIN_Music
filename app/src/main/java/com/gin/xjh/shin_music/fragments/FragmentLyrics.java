package com.gin.xjh.shin_music.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.gin.xjh.shin_music.netrequest.GetNetMusicLrc;
import com.gin.xjh.shin_music.R;
import com.gin.xjh.shin_music.activities.AlbumDetailsActivity;
import com.gin.xjh.shin_music.bean.Album;
import com.gin.xjh.shin_music.bean.Song;
import com.gin.xjh.shin_music.utils.MusicUtil;
import com.gin.xjh.shin_music.view.LyricView;

/**
 * Created by Gin on 2018/4/23.
 */

public class FragmentLyrics extends Fragment implements View.OnClickListener {

    private SeekBar mVolumeSeekBar;
    private ImageView mAlbumDetails;
    private AudioManager mAudioManager;
    private MyVolumeReceiver mMyVolumeReceiver;
    private LyricView mLyricView;
    private TextView mHint;

    public static final String LYRIC_ACTION_CHANGE = "Lyric.To.Change";
    public static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lyrics, null);
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        mMyVolumeReceiver = new MyVolumeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getContext());
        intentFilter.addAction(VOLUME_CHANGED_ACTION);
        intentFilter.addAction(LYRIC_ACTION_CHANGE);
        broadcastManager.registerReceiver(mMyVolumeReceiver, intentFilter);
        initView(view);
        initEvent();
        return view;
    }

    private void initView(View view) {
        mVolumeSeekBar = view.findViewById(R.id.volumeSeekBar);
        mAlbumDetails = view.findViewById(R.id.albumdetails);
        mLyricView = view.findViewById(R.id.lyricView);
        mHint = view.findViewById(R.id.lyric_hint);

        mAlbumDetails.setOnClickListener(this);
        mVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int streamMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);//最大音量
        int streamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        mVolumeSeekBar.setMax(streamMaxVolume);
        mVolumeSeekBar.setProgress(streamVolume);
    }

    private void initEvent() {
        Song song = MusicUtil.getNowSong();
        if (song != null) {
            String lyric = song.getLyric();
            if (lyric != null) {
                if (lyric == "") {
                    mHint.setText("未找到歌词");
                } else {
                    mLyricView.getLyric(lyric);
                    mHint.setVisibility(View.GONE);
                }
            } else {
                if (song.isOnline())
                    new GetNetMusicLrc().getJson(mLyricView, mHint);
                else {
                    song.setLyric("");
                    mHint.setText("未找到歌词");
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.albumdetails:
                //调转到专辑
                Song song = MusicUtil.getNowSong();
                if (song != null && song.isOnline()) {
                    Intent intent = new Intent(getContext(), AlbumDetailsActivity.class);
                    Bundle bundle = new Bundle();
                    Album album = new Album(song.getAlbumName(), song.getAlbumUrl(), song.getAlbumTime(), song.getAlbumId(), song.getSingerName());
                    bundle.putSerializable(getString(R.string.ALBUM), album);
                    intent.putExtra(getString(R.string.ALBUM), bundle);
                    intent.putExtra(getString(R.string.IS_ALBUM), true);
                    startActivity(intent);
                }

                break;
        }
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    public class MyVolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case VOLUME_CHANGED_ACTION:
                    //如果音量发生变化则更改seekbar的位置
                    if (intent.getAction().equals(VOLUME_CHANGED_ACTION)) {
                        int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
                        mVolumeSeekBar.setProgress(currVolume);
                    }
                    break;
                case LYRIC_ACTION_CHANGE:
                    mHint.setVisibility(View.VISIBLE);
                    Song song = MusicUtil.getNowSong();
                    String lyric = song.getLyric();
                    if (lyric != null) {
                        if (lyric == "") {
                            mHint.setText("未找到歌词");
                        } else {
                            mLyricView.getLyric(lyric);
                            mHint.setVisibility(View.GONE);
                        }
                    } else {
                        if (song.isOnline())
                            new GetNetMusicLrc().getJson(mLyricView, mHint);
                        else {
                            song.setLyric("");
                            mHint.setText("未找到歌词");
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMyVolumeReceiver);
        super.onDestroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (mVolumeSeekBar != null) {
            int currVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);// 当前的媒体音量
            mVolumeSeekBar.setProgress(currVolume);
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}
