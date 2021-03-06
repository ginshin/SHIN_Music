package com.gin.xjh.shin_music.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gin.xjh.shin_music.R;
import com.gin.xjh.shin_music.activities.AllCommentActivity;
import com.gin.xjh.shin_music.activities.MusicPlayActivity;
import com.gin.xjh.shin_music.bean.Song;
import com.gin.xjh.shin_music.service.MusicService;
import com.gin.xjh.shin_music.user.UserState;
import com.gin.xjh.shin_music.utils.DensityUtil;
import com.gin.xjh.shin_music.utils.ListDataSaveUtil;
import com.gin.xjh.shin_music.utils.MusicUtil;
import com.gin.xjh.shin_music.utils.NetStateUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gin on 2018/4/24.
 */

public class MusicRecyclerViewAdapter extends RecyclerView.Adapter<MusicRecyclerViewAdapter.MusicViewHolder> {
    public List<Song> mSongList;
    private Context mContext;

    public MusicRecyclerViewAdapter(Context context, List<Song> list) {
        this.mSongList = list;
        this.mContext = context;
    }

    @Override
    public MusicRecyclerViewAdapter.MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false));
    }

    //绑定视图
    @Override
    public void onBindViewHolder(MusicRecyclerViewAdapter.MusicViewHolder holder, int position) {
        holder.init(mSongList.get(position), mContext, position);
    }

    @Override
    public int getItemCount() {
        return mSongList.size();
    }

    @SuppressLint("ResourceAsColor")
    private void showbottomDialog(final int position) {
        final Dialog bottomDialog = new Dialog(mContext, R.style.BottomDialog);
        final Song song = mSongList.get(position);
        View contentView = null;
        if(song.isOnline()){
            contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_content_circle, null);
            TextView icComment = contentView.findViewById(R.id.ic_comment);
            if (song.isOnline()) {
                icComment.setTextColor(mContext.getResources().getColor(R.color.Check));
            }
            icComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetStateUtil.getNetWorkState(mContext) == NetStateUtil.NO_STATE) {
                        Toast.makeText(mContext, "当前无网络...", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //发消息告知弹出评论
                    if (song.isOnline()) {
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(mContext.getString(R.string.SONG), song);
                        Intent ic_comment_intent = new Intent(mContext, AllCommentActivity.class);
                        ic_comment_intent.putExtra(mContext.getString(R.string.SONG), bundle);
                        mContext.startActivity(ic_comment_intent);
                    } else {
                        Toast.makeText(mContext, "该歌曲不支持评论功能", Toast.LENGTH_SHORT).show();
                    }
                    bottomDialog.dismiss();
                }
            });
        } else {
            contentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_content_circle_local, null);
            TextView icDelete = contentView.findViewById(R.id.ic_delete);
            icDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, MediaStore.Video.Media.DATA + "='" + song.getUrl() + "'", null);
                    File mf = new File(song.getUrl());
                    if (mf.exists()) {
                        mf.delete();
                        Toast.makeText(mContext, "删除成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, "该文件不存在", Toast.LENGTH_LONG).show();
                    }
                    mSongList.remove(position);
                    notifyItemRemoved(position);
                    notifyDataSetChanged();
                    bottomDialog.dismiss();
                }
            });
        }

        TextView icPlay = contentView.findViewById(R.id.ic_play);
        icPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (song.isOnline()) {
                    if (NetStateUtil.getNetWorkState(mContext) == NetStateUtil.NO_STATE) {
                        Toast.makeText(mContext, "当前网络无法播放", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (NetStateUtil.getNetWorkState(mContext) == NetStateUtil.DATA_STATE && UserState.isUse_4G() == false) {
                        Toast.makeText(mContext, "请允许4G播放后尝试", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (MusicUtil.getListSize() == 0) {
                    List<Song> mSong = new ArrayList<>();
                    mSong.add(song);
                    MusicUtil.changeSongList(mSong);
                    Intent startIntent = new Intent(mContext, MusicService.class);
                    startIntent.putExtra("action", MusicService.PLAY);
                    mContext.startService(startIntent);
                } else {
                    List<Song> mlist = MusicUtil.getSongList();
                    boolean isFlag = true;
                    //判断是不是存在歌曲
                    for (Song nowsong : mlist) {
                        if (nowsong.equals(song)) {
                            isFlag = false;
                            break;
                        }
                    }
                    if (isFlag) {
                        MusicUtil.addSong(song, true);
                        ListDataSaveUtil.setSongList(mContext.getString(R.string.SONG_LIST), MusicUtil.getSongList());
                    } else {
                        Toast.makeText(mContext, "该歌曲已经存在，请勿重复添加", Toast.LENGTH_SHORT).show();
                    }
                }
                bottomDialog.dismiss();
            }
        });
        TextView icSinger = contentView.findViewById(R.id.ic_singer);
        TextView icAlbum = contentView.findViewById(R.id.ic_album);
        icAlbum.setText(song.getAlbumName());
        icSinger.setText(song.getSingerName());
        bottomDialog.setCanceledOnTouchOutside(true);
        bottomDialog.setContentView(contentView);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) contentView.getLayoutParams();
        params.width = mContext.getResources().getDisplayMetrics().widthPixels - DensityUtil.dp2px(mContext, 16f);
        params.bottomMargin = DensityUtil.dp2px(mContext, 8f);
        contentView.setLayoutParams(params);
        bottomDialog.getWindow().setGravity(Gravity.BOTTOM);
        bottomDialog.getWindow().setWindowAnimations(R.style.BottomDialog_Animation);
        bottomDialog.show();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        private TextView mSongName, mSingerName;
        private ImageView mAbout;

        public MusicViewHolder(View itemView) {
            super(itemView);
            mSongName = itemView.findViewById(R.id.itemSongName);
            mSingerName = itemView.findViewById(R.id.itemSingerName);
            mAbout = itemView.findViewById(R.id.music_sz);
        }

        public void init(final Song song, final Context context, final int position) {
            mSongName.setText(song.getSongName());
            mSingerName.setText(song.toString());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (song.isOnline()) {
                        if (NetStateUtil.getNetWorkState(context) == NetStateUtil.NO_STATE) {
                            Toast.makeText(context, "当前网络无法播放", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (NetStateUtil.getNetWorkState(context) == NetStateUtil.DATA_STATE && UserState.isUse_4G() == false) {
                            Toast.makeText(context, "请允许4G播放后尝试", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    List<Song> mList = new ArrayList<>();
                    for (int i = 0; i < mSongList.size(); i++) {
                        mList.add(mSongList.get(i));
                    }
                    MusicUtil.changeSongList(mList);
                    MusicUtil.setIndex(position);
                    Intent startIntent = new Intent(context, MusicService.class);
                    startIntent.putExtra("action", MusicService.PLAY);
                    context.startService(startIntent);
                    Intent intent = new Intent(context, MusicPlayActivity.class);
                    context.startActivity(intent);
                }
            });

            mAbout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showbottomDialog(position);
                }
            });
        }
    }
}
