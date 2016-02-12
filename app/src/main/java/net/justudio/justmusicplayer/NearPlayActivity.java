package net.justudio.justmusicplayer;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import net.justudio.justmusicplayer.adapter.MyMusicListAdapter;
import net.justudio.justmusicplayer.model.Mp3Info;
import net.justudio.justmusicplayer.service.PlayService;
import net.justudio.justmusicplayer.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/2/3 0003.
 */
public class NearPlayActivity extends BaseActivity implements AdapterView.OnItemClickListener{

    private ListView near_play_list;
    private JustMusicApplication app;
    private ArrayList<Mp3Info> nearMp3Infos;
    private MyMusicListAdapter nearPlayMusicAdapter;
    private TextView no_near_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_near_play);
        app= (JustMusicApplication) getApplication();
        near_play_list =(ListView)findViewById(R.id.near_play_list);
        no_near_data= (TextView)findViewById(R.id.no_near_data);
        near_play_list.setOnItemClickListener(this);
        intiData();
    }

    public void intiData() {
        try {
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime","!=",0).
                    orderBy("playTime", true).limit(Constants.NEAR_PLAY_LIMIT));
            if (list==null || list.size()==0){
                no_near_data.setVisibility(View.VISIBLE);
                near_play_list.setVisibility(View.GONE);
            } else {
                no_near_data.setVisibility(View.GONE);
                near_play_list.setVisibility(View.VISIBLE);
                nearMp3Infos = (ArrayList<Mp3Info>)list;
                nearPlayMusicAdapter = new MyMusicListAdapter(this, nearMp3Infos);
                near_play_list.setAdapter(nearPlayMusicAdapter);
            }
        } catch (DbException e){
            e.printStackTrace();
        }
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

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (playService.getChangePlayList()!=playService.NEAR_PLAY_LIST){
            playService.setMp3Infos(nearMp3Infos);
            playService.setChangePlayList(PlayService.NEAR_PLAY_LIST);
        }
        playService.play(position);

    }
}
