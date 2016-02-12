package net.justudio.justmusicplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.lidroid.xutils.DbUtils;

import net.justudio.justmusicplayer.service.PlayService;
import net.justudio.justmusicplayer.util.Constants;

/**
 * Created by Administrator on 2016/2/2 0002.
 */
public class JustMusicApplication extends Application{


    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;



    @Override
    public void onCreate() {
        super.onCreate();
        sp=getSharedPreferences(Constants.SP_NAME, MODE_PRIVATE);

        dbUtils =DbUtils.create(getApplicationContext(), Constants.DB_NAME);
        context=getApplicationContext();
    }
}
