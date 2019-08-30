package com.beproffer.beproffer.util;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.InputStream;
import java.util.Objects;

public class ImageUtil {

    public static boolean isImageSizeCorrect(Context context, Uri uri, long maxImageSize) {

        long dataSize = 0;
        File file = null;
        boolean isSizeCorrect = false;

        String scheme = uri.getScheme();

        switch (Objects.requireNonNull(scheme)) {
            case ContentResolver.SCHEME_CONTENT:
                try {
                    InputStream fileInputStream = context.getContentResolver().openInputStream(uri);
                    dataSize = fileInputStream.available();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                isSizeCorrect = dataSize <= maxImageSize;
                break;

            case ContentResolver.SCHEME_FILE:
                String path = uri.getPath();
                try {
                    file = new File(path);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                isSizeCorrect = file.length() <= maxImageSize;

                break;
        }

        return isSizeCorrect;
    }
}
