package net.justudio.justmusicplayer.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import net.justudio.justmusicplayer.JustMusicApplication;
import net.justudio.justmusicplayer.model.Mp3Info;
import net.justudio.justmusicplayer.util.MediaUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * MusicPlay Service
 */

public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer mediaPlayer;
    private int currentPosition;
    public ArrayList<Mp3Info> mp3Infos;
    private MusicUpdateListener musicUpdateListener;
    private ExecutorService es = Executors.newSingleThreadExecutor();
    private boolean isPause =false;

    public static final int MY_MUSIC_LIST=1;
    public static final int MY_LOVE_MUSIC_LIST=2;
    public static final int NET_MUSIC_LIST=3;
    public static final int NEAR_PLAY_LIST=4;

    private int changePlayList=MY_MUSIC_LIST;

    //播放模式
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;

    private int play_Mode= ORDER_PLAY;

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public void setPlay_Mode(int play_Mode) {
        this.play_Mode = play_Mode;
    }

    public int getPlay_Mode() {
        return play_Mode;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public PlayService() {
    }
    public int getCurrentPosition() {
        return currentPosition;
    }
    private  Random random = new Random();
    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_Mode){
            case ORDER_PLAY:
                next();
                break;
            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;
            case SINGLE_PLAY:
                play(currentPosition);
                break;
            default:
                break;
        }

    }

    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    public class PlayBinder extends Binder{

        public PlayService getPlayService(){
            return PlayService.this;


        }
    }
    

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        //throw new UnsupportedOperationException("Not yet implemented");
        return new PlayBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JustMusicApplication app = (JustMusicApplication) getApplication();
        currentPosition = app.sp.getInt("currentPosition",0 );
        play_Mode = app.sp.getInt("play_Mode",ORDER_PLAY);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mp3Infos= MediaUtil.getMp3Infos(this);
        es.execute(updateStatusRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es!=null && !es.isShutdown()){
            es.shutdown();
            es=null;
        }
        mediaPlayer = null;
        mp3Infos =null;
        musicUpdateListener = null;
    }

    public boolean isPause(){
        return isPause;
    }

    Runnable updateStatusRunnable = new Runnable() {
        @Override
        public void run() {
            while (true){
                if (musicUpdateListener!=null&& mediaPlayer!=null && mediaPlayer.isPlaying()){
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    public int getCurrentProgress(){
        if (mediaPlayer!=null&&mediaPlayer.isPlaying()){
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }


    //播放
    public void play(int position){

        if(position>=0 && position<mp3Infos.size()) {
            Mp3Info mp3Info = mp3Infos.get(position);
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(this, Uri.parse(mp3Info.getUrl()));
                mediaPlayer.prepare();
                mediaPlayer.start();
                currentPosition=position;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (musicUpdateListener!=null){
                musicUpdateListener.onChange(currentPosition);
            }
        }
    }

    public void pause(){
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            isPause=true;
        }

    }
    //下一首
    public void next(){
        if (currentPosition+1>=mp3Infos.size()){
            currentPosition=0;
        } else{
            currentPosition++;
        }
        play(currentPosition);

    }
    //上一首
    public void previous(){
        if (currentPosition-1<0){
            currentPosition= mp3Infos.size()-1;
        } else{
            currentPosition --;
        }
        play(currentPosition);

    }

    public void start(){
        if (mediaPlayer!=null&&!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            mediaPlayer.getDuration();
        }
    }

    public boolean isPlaying(){
        if (mediaPlayer!=null){
            return mediaPlayer.isPlaying();
        }
        return false;
    }


    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public void seekTo(int msec){
        mediaPlayer.seekTo(msec);
    }


    //更新歌曲状态
    public interface MusicUpdateListener{
        public void onPublish(int progress);
        public void onChange(int position);

    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
