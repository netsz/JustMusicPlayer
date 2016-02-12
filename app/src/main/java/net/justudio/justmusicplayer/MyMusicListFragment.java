package net.justudio.justmusicplayer;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.andraskindler.quickscroll.QuickScroll;
import com.lidroid.xutils.db.sqlite.Selector;

import net.justudio.justmusicplayer.adapter.MyMusicListAdapter;
import net.justudio.justmusicplayer.model.Mp3Info;
import net.justudio.justmusicplayer.service.PlayService;
import net.justudio.justmusicplayer.util.MediaUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 我的音乐Fragment
 */
public class MyMusicListFragment extends Fragment implements OnItemClickListener,View.OnClickListener{

    private ListView listView_my_music;
    ArrayList<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();
    private MainActivity mainActivity;
    private MyMusicListAdapter myMusicListAdapter;
    private ImageView albumCover;
    private TextView songName;
    private TextView singer;
    private ImageView playPause;
    private ImageView nextSong;
    private boolean isPause=false;
    private QuickScroll quickScroll;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)context;

    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.my_music_list_layout,null);
        listView_my_music=(ListView)view.findViewById(R.id.my_music_list);
        albumCover=(ImageView)view.findViewById(R.id.albumCover);
        songName= (TextView) view.findViewById(R.id.songName);
        singer= (TextView) view.findViewById(R.id.singer);
        playPause=(ImageView)view.findViewById(R.id.playPause);
        nextSong= (ImageView) view.findViewById(R.id.nextSong);
        quickScroll =(QuickScroll)view.findViewById(R.id.quickscroll);
        playPause.setOnClickListener(this);
        nextSong.setOnClickListener(this);
        albumCover.setOnClickListener(this);
        listView_my_music.setOnItemClickListener(this);

//        loadData();
        return view;
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

    }

    public void loadData(){
        mp3Infos= MediaUtil.getMp3Infos(mainActivity);
//        mp3Infos = mainActivity.playService.mp3Infos;

        myMusicListAdapter = new MyMusicListAdapter(mainActivity,mp3Infos);
        listView_my_music.setAdapter(myMusicListAdapter);

        initQuickScroll();

    }

    private void initQuickScroll() {
        quickScroll.init(QuickScroll.TYPE_POPUP_WITH_HANDLE,listView_my_music,myMusicListAdapter,QuickScroll.STYLE_HOLO);
        quickScroll.setFixedSize(1);
        quickScroll.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);

        quickScroll.setPopupColor(QuickScroll.BLUE_LIGHT,QuickScroll.BLUE_LIGHT_SEMITRANSPARENT,1, Color.WHITE,1);
    }

    @Override
    public void onResume() {
        super.onResume();
        mainActivity.bindPlayService();
    }

    @Override
    public void onPause() {
        super.onPause();
        mainActivity.unbindPlayService();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList()!= PlayService.MY_MUSIC_LIST) {
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);

        }
        mainActivity.playService.play(position);

        savePlayRecord();
    }

    private void savePlayRecord() {
        Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info = mainActivity.app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getId()));
            if (playRecordMp3Info==null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.save(mp3Info);
            } else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.update(playRecordMp3Info,"playTime");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void ChangeUIStateOnPlay(int position){
        if (position>=0 && position<mp3Infos.size()) {
            Mp3Info mp3Info = mp3Infos.get(position);
            songName.setText(mp3Info.getTitle());
            singer.setText(mp3Info.getArtist());

            if (mainActivity.playService.isPlaying()) {
                playPause.setImageResource(R.mipmap.pause_normal);
            } else {
                playPause.setImageResource(R.mipmap.play_normal);
            }
            Bitmap albumBitmap = MediaUtil.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            albumCover.setImageBitmap(albumBitmap);

        }

    }

    @Override
    public void onClick(View v){
        switch (v.getId()){
            case R.id.playPause:
                if (mainActivity.playService.isPlaying()){
                    playPause.setImageResource(R.mipmap.play_normal);
                    mainActivity.playService.pause();
                    isPause=true;
                } else{

                    if (mainActivity.playService.isPause()){
                        playPause.setImageResource(R.mipmap.pause_normal);
                        mainActivity.playService.start();
                    } else {
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
                break;
            case R.id.nextSong:

                mainActivity.playService.next();

                break;

            case R.id.albumCover:

                Intent i= new Intent(mainActivity, PlayActivity.class);
                startActivity(i);
                break;

            default:
                break;
        }
    }
}
