package net.justudio.justmusicplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import net.justudio.justmusicplayer.model.Mp3Info;
import net.justudio.justmusicplayer.model.SearchResult;
import net.justudio.justmusicplayer.service.PlayService;
import net.justudio.justmusicplayer.util.Constants;
import net.justudio.justmusicplayer.util.DownloadUtil;
import net.justudio.justmusicplayer.util.MediaUtil;
import net.justudio.justmusicplayer.util.SearchMusicUtil;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

import douzi.android.view.DefaultLrcBuilder;
import douzi.android.view.ILrcBuilder;
import douzi.android.view.ILrcView;
import douzi.android.view.LrcRow;
import douzi.android.view.LrcView;

/**
 * 音乐播放页
 */
public class PlayActivity extends BaseActivity implements View.OnClickListener,SeekBar.OnSeekBarChangeListener {

    private TextView play_title, start_time, end_time;
    private ImageView play_album, playing_mode, favoriteIcon, previous_Song, play_pause_song, next_Song;
    private SeekBar playingSeekBar;
//    private ArrayList<Mp3Info> mp3Infos;
    private static final int UPDATE_TIME = 0X3;
    private static final int UPDATE_LRC = 0X4;
    private ViewPager viewPager;
    private LrcView lrcView;
    private ArrayList<View> views = new ArrayList<>();
    private JustMusicApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_play);

        app = (JustMusicApplication) getApplication();
//        play_title = (TextView) findViewById(R.id.play_title);
        start_time = (TextView) findViewById(R.id.start_time);
        end_time = (TextView) findViewById(R.id.end_time);
//        play_album = (ImageView) findViewById(R.id.play_album);
        playing_mode = (ImageView) findViewById(R.id.playing_mode);
        favoriteIcon = (ImageView) findViewById(R.id.favoriteIcon);
        previous_Song = (ImageView) findViewById(R.id.previous_Song);
        next_Song = (ImageView) findViewById(R.id.next_Song);
        play_pause_song = (ImageView) findViewById(R.id.play_pause_song);
        playingSeekBar = (SeekBar) findViewById(R.id.playingSeekBar);
//        mp3Infos = MediaUtil.getMp3Infos(this);
        myHandler = new MyHandler(this);


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        initViewPager();

        playing_mode.setOnClickListener(this);
        play_pause_song.setOnClickListener(this);
        favoriteIcon.setOnClickListener(this);
        previous_Song.setOnClickListener(this);
        next_Song.setOnClickListener(this);
        playingSeekBar.setOnSeekBarChangeListener(this);
    }

    private void initViewPager() {

        View album_image_layout=getLayoutInflater().inflate(R.layout.album_image_layout,null);
        play_album = (ImageView) album_image_layout.findViewById(R.id.play_album);
        play_title = (TextView)album_image_layout.findViewById(R.id.play_title);
        views.add(album_image_layout);
        View lrc_layout = getLayoutInflater().inflate(R.layout.lrc_layout, null);
        lrcView =(LrcView)lrc_layout.findViewById(R.id.lrcView);
        lrcView.setListener(new ILrcView.LrcViewListener() {
            @Override
            public void onLrcSeeked(int newPosition, LrcRow row) {
                if (playService.isPlaying()) {
                    playService.seekTo((int) row.time);
                }
            }
        });


        lrcView.setLoadingTipText("正在加载歌词");
        lrcView.setBackgroundResource(R.mipmap.music_play_back);
        lrcView.getBackground().setAlpha(150);
        views.add(lrc_layout);
        viewPager.setAdapter(new MyPagerAdapter());
//        viewPager.addOnPageChangeListener(this);


    }

    private void loadLRC(File lrcFile){
        StringBuffer buf = new StringBuffer(1020 * 10);
        char[] chars = new char[1024];
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(lrcFile)));
            int len=-1;
            while ((len=in.read(chars))!=-1){
                buf.append(chars,0,len);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ILrcBuilder builder = new DefaultLrcBuilder();
        List<LrcRow> rows = builder.getLrcRows(buf.toString());
        lrcView.setLrc(rows);
    }

   private long getId(Mp3Info mp3Info) {
//        初始收藏状态
       long id= 0 ;
       switch (playService.getChangePlayList()){
           case PlayService.MY_MUSIC_LIST:
               id=mp3Info.getId();
               break;
           case PlayService.MY_LOVE_MUSIC_LIST:
               id=mp3Info.getMp3InfoId();
               break;
       }
       return id;
    }

    class MyPagerAdapter extends PagerAdapter {

        @Override
        public void destroyItem(ViewGroup container, int position, Object object){
            container.removeView(views.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position){
            View v = views.get(position);
            container.addView(v);
            return v;
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
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
    protected void onDestroy() {

        super.onDestroy();
        exit();
    }

    private static MyHandler myHandler;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser){
            playService.pause();
            playService.seekTo(progress);
            playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    static class MyHandler extends android.os.Handler {


        private PlayActivity playActivity;

        public MyHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            playActivity = weak.get();
            if (playActivity != null) {
                switch (msg.what) {
                    case UPDATE_TIME:
                        playActivity.start_time.setText(MediaUtil.FormatTime(msg.arg1));
                        break;
                    case UPDATE_LRC:
                        playActivity.lrcView.seekLrcToTime((int)msg.obj);
                        break;

                    case DownloadUtil.SUCCESS_LRC:
                        playActivity.loadLRC(new File((String)msg.obj));
                        break;

                    case DownloadUtil.FAILED_LRC:
                        Toast.makeText(playActivity,"歌词下载失败", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;

                }
            }
        }

    }


    @Override
    public void publish(int progress) {

        Message msg = myHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        myHandler.sendMessage(msg);
        playingSeekBar.setProgress(progress);
        myHandler.obtainMessage(UPDATE_LRC,progress).sendToTarget();


    }

    @Override
    public void change(int position) {
//        if (this.playService.isPlaying()) {
        Mp3Info mp3Info = playService.mp3Infos.get(position);
        play_title.setText(mp3Info.getTitle());
        Bitmap albumBitmap = MediaUtil.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
//        Bitmap albumBitmap = MediaUtil.getArtworkFromFile(this, mp3Info.getId(), mp3Info.getAlbumId());
        play_album.setImageBitmap(albumBitmap);
        end_time.setText(MediaUtil.FormatTime(mp3Info.getDuration()));
        playingSeekBar.setProgress(0);
        playingSeekBar.setMax((int) mp3Info.getDuration());
        if (playService.isPlaying()) {
            play_pause_song.setImageResource(R.mipmap.pause_normal);
        } else {
            play_pause_song.setImageResource(R.mipmap.play_normal);
        }

        switch (playService.getPlay_Mode()) {
            case PlayService.ORDER_PLAY:
                playing_mode.setImageResource(R.mipmap.order);
                playing_mode.setTag(PlayService.ORDER_PLAY);
                break;
            case PlayService.SINGLE_PLAY:
                playing_mode.setImageResource(R.mipmap.single);
                playing_mode.setTag(PlayService.SINGLE_PLAY);
                break;
            case PlayService.RANDOM_PLAY:
                playing_mode.setImageResource(R.mipmap.random);
                playing_mode.setTag(PlayService.RANDOM_PLAY);
                break;
        }

        //初始化收藏图标

        try {
            Mp3Info likeMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            if (likeMp3Info != null) {
                favoriteIcon.setImageResource(R.mipmap.shoucang_ok);
            } else {
                favoriteIcon.setImageResource(R.mipmap.shou_cang);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

        String songName = mp3Info.getTitle();
        String lrcPath = Environment.getExternalStorageDirectory()+ Constants.DIR_LRC+"/"+songName+".lrc";
        File lrcFile = new File(lrcPath);
        if (!lrcFile.exists()){
            SearchMusicUtil.getInstance().setListener(new SearchMusicUtil.OnSearchResultListener() {
                @Override
                public void onSearchResult(ArrayList<SearchResult> results) {
                    SearchResult searchResult = results.get(0);
                    String url = Constants.BAIDU_URL+searchResult.getUrl();
                    DownloadUtil.getInstance().downloadLRC(url,searchResult.getMusicName(),myHandler);

                }
            }).search(songName+""+mp3Info.getArtist(),1);
        } else {
            loadLRC(lrcFile);
        }
    }



    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.play_pause_song: {
                if (playService.isPlaying()) {
                    play_pause_song.setImageResource(R.mipmap.play_normal);
                    playService.pause();
                } else {
                    if (playService.isPause()) {
                        play_pause_song.setImageResource(R.mipmap.pause_normal);
                        this.playService.start();
                    } else {
                        this.playService.play(playService.getCurrentPosition());
                    }
                }
                break;
            }
            case R.id.next_Song: {
                playService.next();
                break;
            }
            case R.id.previous_Song: {
                playService.previous();
                break;
            }
            case R.id.playing_mode: {
                int mode = (int) playing_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        playing_mode.setImageResource(R.mipmap.random);
                        playing_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_Mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT).show();
                        break;

                    case PlayService.RANDOM_PLAY:
                        playing_mode.setImageResource(R.mipmap.single);
                        playing_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_Mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT).show();
                        break;

                    case PlayService.SINGLE_PLAY:
                        playing_mode.setImageResource(R.mipmap.order);
                        playing_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_Mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;

                }
                break;
                    }

            case R.id.favoriteIcon:{
                Mp3Info mp3Info = playService.mp3Infos.get(playService.getCurrentPosition());
                try {
                    Mp3Info loveMp3Info = app.dbUtils.findFirst(Selector.from(Mp3Info.class).where("mp3InfoId","=",getId(mp3Info)));
                    if (loveMp3Info==null){
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLove(1);
                        app.dbUtils.save(mp3Info);
                        favoriteIcon.setImageResource(R.mipmap.shoucang_ok);
                    }else {
                        int isLove = loveMp3Info.getIsLove();
                        if (isLove==1){
                            loveMp3Info.setIsLove(0);
                            favoriteIcon.setImageResource(R.mipmap.shou_cang);
                        } else{
                            loveMp3Info.setIsLove(1);
                            favoriteIcon.setImageResource(R.mipmap.shoucang_ok);
                        }
                        app.dbUtils.update(loveMp3Info, "isLove");


                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                break;
            }

        }


}
