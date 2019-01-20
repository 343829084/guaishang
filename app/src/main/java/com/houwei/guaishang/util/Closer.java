package com.houwei.guaishang.util;

import android.database.Cursor;

import java.io.Closeable;

/**
 * @author jieshao
 *
 *
 */
public class Closer {

    public static void close(Closeable stream ){
        if (stream == null) {
            return ;
        }
        try {
            stream.close();
        } catch (Exception e) {

        }

    }

    public static void close(Cursor stream ){
        if (stream == null) {
            return ;
        }
        try {
            stream.close();
        } catch (Exception e) {

        }

    }

}