package net.justudio.justmusicplayer.util;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import net.justudio.justmusicplayer.model.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.IllegalFormatCodePointException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 下载部件
 */
public class DownloadUtil {

    private static final String DOWNLOAD_URL = "/download?_o=%2Fsearch%2Fsong";
    public static final int SUCCESS_LRC = 1;
    public static final int FAILED_LRC = 2;
    private static final int SUCCESS_MP3 = 3;
    private static final int FAILED_MP3 = 4;
    private static final int GET_MP3_URL = 5;
    private static final int GET_FAILED_MP3_URL=6;
    private static final int MUSIC_EXISTS=7;

    private static DownloadUtil sInstance;
    private ExecutorService mThreadPool;
    private OnDownlaodlistener mListener;

    //设置回调的监听器

    public DownloadUtil setListener(OnDownlaodlistener mListener){
        this.mListener  = mListener;
        return this;
    }

    //获取下载工具的实例
    public synchronized static DownloadUtil getInstance(){
        if (sInstance==null){
            try {
                sInstance = new DownloadUtil();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private DownloadUtil() throws ParserConfigurationException{
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    //下载的方法
    public void download(final SearchResult searchResult){
        final Handler handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case SUCCESS_LRC:
                        if (mListener!=null)
                            mListener.onDownload("歌词下载成功");
                        break;
                    case FAILED_LRC:
                        if (mListener!=null)
                            mListener.onFailed("歌词下载失败");
                        break;
                    case GET_MP3_URL:
                        downloadMusic(searchResult, (String)msg.obj,this);
                        break;
                    case GET_FAILED_MP3_URL:
                        if (mListener!=null)
                            mListener.onFailed("下载失败，这是百度收费类的歌曲");
                        break;

                    case SUCCESS_MP3:
                        if (mListener!=null)
                            mListener.onDownload(searchResult.getMusicName()+"已下载");
                            String url = Constants.BAIDU_URL + searchResult.getUrl();
                        downloadLRC(url,searchResult.getMusicName(),this);
                        break;
                    case FAILED_MP3:
                        if (mListener!=null)
                            mListener.onFailed(searchResult.getMusicName() + "下载失败");
                        break;
                    case MUSIC_EXISTS:
                        if (mListener!=null)
                            mListener.onFailed("音乐已存在");

                }
            }
        };
        getDownloadMusicURL(searchResult, handler);
    }

    public void downloadLRC(final String url,final String musicName,final Handler handler) {

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(url).userAgent(Constants.USER_AGENT).timeout(6000)
                            .get();
                    Elements lrcTag = doc.select("div.lyric-content");
                    String lrcURL = lrcTag.attr("data-lrclink");
                    File lrcDirFile = new File(Environment.getExternalStorageDirectory() + Constants.DIR_LRC);
                    if (!lrcDirFile.exists()) {
                        lrcDirFile.mkdirs();
                    }

                    lrcURL = Constants.BAIDU_URL + lrcURL;
                    String target = lrcDirFile + "/" + musicName + ".lrc";
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(lrcURL).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()) {
                            PrintStream ps = new PrintStream(new File(target));
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes, 0, bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_LRC, target).sendToTarget();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    private void getDownloadMusicURL(final SearchResult searchResult, final Handler handler) {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String url = Constants.BAIDU_URL+"/song/"+searchResult.getUrl().substring(searchResult.getUrl().lastIndexOf("/")+1) +DOWNLOAD_URL;
                    Document doc = Jsoup.connect(url).userAgent(Constants.USER_AGENT).timeout(6000).get();
                    Elements targetElements = doc.select("a[data-btndata]");
                    if (targetElements.size()<=0){
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                    }
                    for (Element e:targetElements){
                        if(e.attr("href").contains("mp3")){
                            String result = e.attr("href");
                            Message msg = handler.obtainMessage(GET_MP3_URL, result);
                            msg.sendToTarget();
                            return;
                        }
                        if (e.attr("href").startsWith("/vip")){
                            targetElements.remove(e);
                        }
                    }
                    if (targetElements.size()<=0){
                        handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                        return;
                    }
                    String result = targetElements.get(0).attr("href");
                    Message msg = handler.obtainMessage(GET_MP3_URL, result);
                    msg.sendToTarget();

                } catch (IOException e){
                    e.printStackTrace();
                    handler.obtainMessage(GET_FAILED_MP3_URL).sendToTarget();
                }
            }
        });
    }


    private void downloadMusic(final SearchResult searchResult, final String url,final Handler handler){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                File musicDirFile = new File(Environment.getExternalStorageDirectory()+Constants.DIR_MUSIC);
                if (!musicDirFile.exists()){
                    musicDirFile.mkdirs();
                }
                String mp3Url = Constants.BAIDU_URL + url;
                String target = musicDirFile +"/"+searchResult.getMusicName()+".mp3";
                File fileTarget = new File(target);
                if (fileTarget.exists()){
                    handler.obtainMessage(MUSIC_EXISTS).sendToTarget();
                } else {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(mp3Url).build();
                    try {
                        Response response = client.newCall(request).execute();
                        if (response.isSuccessful()){
                            PrintStream ps = new PrintStream(fileTarget);
                            byte[] bytes = response.body().bytes();
                            ps.write(bytes,0,bytes.length);
                            ps.close();
                            handler.obtainMessage(SUCCESS_MP3).sendToTarget();
                        }

                    } catch (IOException e){
                        e.printStackTrace();
                        handler.obtainMessage(FAILED_MP3).sendToTarget();
                    }
                }
            }
        });

    }

    public interface OnDownlaodlistener{
        public void onDownload(String mp3Url);
        public void onFailed(String error);
    }

}


