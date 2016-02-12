package net.justudio.justmusicplayer.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import net.justudio.justmusicplayer.JustMusicApplication;

/**
 *  隐藏输入法
 */
public class AppUtil {


    public static void hideInputMethod(View view){
        InputMethodManager imm = (InputMethodManager) JustMusicApplication
                .context.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm.isActive()){
            imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }
}
