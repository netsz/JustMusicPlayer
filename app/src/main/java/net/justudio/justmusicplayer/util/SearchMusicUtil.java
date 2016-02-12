package net.justudio.justmusicplayer.util;

import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import net.justudio.justmusicplayer.model.SearchResult;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

/**
 * 搜索音乐
 */
public class SearchMusicUtil {

    private static final int SIZE=20;
    private static final String URL=Constants.BAIDU_URL+Constants.BAIDU_SEARCH;
    private static SearchMusicUtil sInstance;
    private OnSearchResultListener mListener;
    private ExecutorService mThreadPool;

    public synchronized static SearchMusicUtil getInstance(){
        if (sInstance==null){
            try {
                sInstance = new SearchMusicUtil();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }
        return sInstance;
    }

    private SearchMusicUtil() throws ParserConfigurationException{
        mThreadPool = Executors.newSingleThreadExecutor();
    }

    public SearchMusicUtil setListener(OnSearchResultListener l){
        mListener = l;
        return this;
    }

    public void search(final String key,final int page){
        final Handler handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.SUCCESS:
                        if (mListener != null)
                            mListener.onSearchResult((ArrayList<SearchResult>) msg.obj);
                        break;
                    case Constants.FAILED:
                        if (mListener != null) mListener.onSearchResult(null);
                        break;
                }
            }
        };

        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                ArrayList<SearchResult> results = getMusicList(key,page);
                if (results==null){
                    handler.sendEmptyMessage(Constants.FAILED);
                    return;
                }
                handler.obtainMessage(Constants.SUCCESS, results).sendToTarget();
            }
        });
    }

    private ArrayList<SearchResult> getMusicList(final String key,final int page){
        final String start = String.valueOf((page-1)*SIZE);
        try {
            Document document = Jsoup.connect(URL)
                    .data("key", key, "start", start,"size", String.valueOf(SIZE))
                    .userAgent(Constants.USER_AGENT)
                    .timeout(6*1000).get();

            Elements songTitles = document.select("div.song-item.clearfix");
            Elements songInfo;
            ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();

            TAG:
            for (Element song : songTitles){
                songInfo = song.getElementsByTag("a");
                SearchResult searchResult = new SearchResult();
                for (Element info:songInfo){
                    if (info.attr("href").startsWith("http://y.baidu.com/song/")){
                        continue TAG;
                    }
                    if (info.attr("href").equals("#")&&!TextUtils.isEmpty(info.attr("data-songdata"))){
                        continue TAG;
                    }
                    if (info.attr("href").startsWith("/song")){
                        searchResult.setMusicName(info.text());
                        searchResult.setUrl(info.attr("href"));
                    }
                    if (info.attr("href").startsWith("/data")){
                        searchResult.setArtist(info.text());
                    }

                    if (info.attr("href").startsWith("/album")){
                        searchResult.setAlbum(info.text().replaceAll("《|》",""));
                    }
                }

                searchResults.add(searchResult);
            }
            return searchResults;
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    public interface OnSearchResultListener{
        public void onSearchResult(ArrayList<SearchResult> results);
    }



}
