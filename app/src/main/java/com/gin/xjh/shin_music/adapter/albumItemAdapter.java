package com.gin.xjh.shin_music.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gin.xjh.shin_music.R;
import com.gin.xjh.shin_music.bean.Album;

import java.util.List;

public class albumItemAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Album> mList;

    public albumItemAdapter(Context context,List<Album> list){
        mContext = context;
        mList=list;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setmList(List<Album> mList) {
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Album getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.fragment_shin_item, null);
            holder = new ViewHolder();
            holder.shin_Img = convertView.findViewById(R.id.shin_img);
            holder.shin_Text = convertView.findViewById(R.id.shin_text);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        //测试
        Drawable drawable = mContext.getResources().getDrawable(R.drawable.dayemen);
        BitmapDrawable bd = (BitmapDrawable) drawable;
        Bitmap bitmap = bd.getBitmap();
        holder.shin_Img.setImageBitmap(bitmap);
        holder.shin_Text.setText(mList.get(position).getAlbumName());
        return convertView;
    }

    public class ViewHolder {
        private TextView shin_Text;
        private ImageView shin_Img;
    }
}
