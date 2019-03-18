package com.eightsines.tgchallenge2019.feature.util;

import android.content.Context;
import androidx.annotation.RawRes;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class AppResUtils {
    private static final int READ_BUFFER_SIZE = 1024;
    private static final String CHARSET_UTF_8 = "UTF-8";

    private AppResUtils() {
    }

    public static String readToString(Context context, @RawRes int resId) throws IOException {
        try (InputStream is = context.getResources().openRawResource(resId)) {
            return readToString(is);
        }
    }

    public static String readToString(InputStream is) throws IOException {
        byte[] buffer = new byte[READ_BUFFER_SIZE];

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            for (;;) {
                int bytesRead = is.read(buffer);

                if (bytesRead <= 0) {
                    break;
                }

                baos.write(buffer, 0, bytesRead);
            }

            return baos.toString(CHARSET_UTF_8);
        }
    }
}
