package net.justudio.justmusicplayer;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import net.justudio.justmusicplayer.adapter.MyMusicListAdapter;
import net.justudio.justmusicplayer.model.Mp3Info;
import net.justudio.justmusicplayer.service.PlayService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/2 0002.
 */
public class MyLoveMusicActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView love_music_list;
    private JustMusicApplication app;
    private ArrayList<Mp3Info> loveMp3Infos;
    private MyMusicListAdapter myLoveMusicAdapter;
//    private boolean isChange = false;//表示当前播放列别是否为收藏列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
//        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.love_title_bar);
        setContentView(R.layout.activity_love_music);
        app= (JustMusicApplication) getApplication();
        love_music_list = (ListView)findViewById(R.id.love_music_list);
        love_music_list.setOnItemClickListener(this);
        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindPlayService();
    }

    private void initData() {

        try {
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLove","=", 1));
            if (list==null||list.size()==0){
                return;
            }
            loveMp3Infos = (ArrayList<Mp3Info>) list;
            myLoveMusicAdapter = new MyMusicListAdapter(this,loveMp3Infos);
            love_music_list.setAdapter(myLoveMusicAdapter);
        } catch (DbException e) {
            e.printStackTrace();
        }




    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList()!=playService.MY_LOVE_MUSIC_LIST){
            playService.setMp3Infos(loveMp3Infos);
            playService.setChangePlayList(PlayService.MY_LOVE_MUSIC_LIST);
        }
        playService.play(position);
        savePlayRecord();

    }

    private void savePlayRecord() {

        Mp3Info mp3Info = playService.getMp3Infos().get(playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getMp3InfoId()));
            if (playRecordMp3Info==null){
                mp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecordMp3Info,"playTime");
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}
