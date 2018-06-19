package com.gin.xjh.shin_music.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.gin.xjh.shin_music.bean.Song;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class BitmapUtil {
    /**
     * 根据专辑ID获取专辑封面图
     *
     * @param song 歌曲
     * @return
     */
    public static Bitmap getAlbumArt(Context context, Song song) {
        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        ContentResolver resolver = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(artworkUri, song.getAlbumId());
        InputStream is;
        try {
            is = resolver.openInputStream(uri);
        } catch (FileNotFoundException ignored) {
            return null;
        }
        if (is == null) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }
}
