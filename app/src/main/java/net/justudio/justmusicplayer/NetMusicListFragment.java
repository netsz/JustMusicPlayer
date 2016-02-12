package net.justudio.justmusicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import net.justudio.justmusicplayer.adapter.NetMusicAdapter;
import net.justudio.justmusicplayer.model.SearchResult;
import net.justudio.justmusicplayer.service.PlayService;
import net.justudio.justmusicplayer.util.AppUtil;
import net.justudio.justmusicplayer.util.Constants;
import net.justudio.justmusicplayer.util.SearchMusicUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 我的音乐Fragment
 */
public class NetMusicListFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemClickListener {

    private MainActivity mainActivity;
    private ListView listView_net_music_list;
    private LinearLayout load_layout,search_container,search_btn_container;
    private ImageButton ib_search_btn;
    private EditText search_content;
    private ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();
    private NetMusicAdapter netMusicAdapter;

    private int page = 1;

    public ArrayList<SearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public static NetMusicListFragment newInstance() {
        NetMusicListFragment net = new NetMusicListFragment();
        return net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.net_music_list_layout,null);
        listView_net_music_list = (ListView)view.findViewById(R.id.listView_net_music_list);
        load_layout = (LinearLayout)view.findViewById(R.id.load_layout);
        search_container = (LinearLayout)view.findViewById(R.id.search_container);
        search_btn_container = (LinearLayout)view.findViewById(R.id.search_btn_container);
        ib_search_btn = (ImageButton)view.findViewById(R.id.ib_search_btn);
        search_content = (EditText)view.findViewById(R.id.search_content);

        listView_net_music_list.setOnItemClickListener(this);
        search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);
        loadNetData();
        return view;
    }

    private void loadNetData() {
        load_layout.setVisibility(View.VISIBLE);
        new loadNetDataTask().execute(Constants.BAIDU_URL + Constants.BAIDU_DAYHOT);
    }

    public void ChangeUIStateOnPlay(int position){

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.search_btn_container:
                search_btn_container.setVisibility(View.GONE);
                search_container.setVisibility(View.VISIBLE);
                break;
            case R.id.ib_search_btn:
                //搜索歌曲事件处理
                searchMusic();
                break;
        }

    }

    private void searchMusic() {

        AppUtil.hideInputMethod(search_content);
        search_btn_container.setVisibility(View.VISIBLE);
        search_container.setVisibility(View.VISIBLE);
        String key = search_content.getText().toString();
        if (TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity,"啥都没有，搜不出来啊",Toast.LENGTH_SHORT).show();
        }
        load_layout.setVisibility(View.VISIBLE);
        SearchMusicUtil.getInstance().setListener(new SearchMusicUtil.OnSearchResultListener(){

            @Override
            public void onSearchResult(ArrayList<SearchResult> results){
                ArrayList<SearchResult> sr = netMusicAdapter.getSearchResults();
                sr.clear();
                sr.addAll(results);
                netMusicAdapter.notifyDataSetChanged();
                load_layout.setVisibility(View.GONE);
            }
        }).search(key, page);

    }

    class loadNetDataTask extends AsyncTask<String,Integer,Integer>{
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            load_layout.setVisibility(View.VISIBLE);
            listView_net_music_list.setVisibility(View.GONE);
            searchResults.clear();

        }

        @Override
        protected Integer doInBackground(String... params) {
            String url = params[0];
            try {
                Document doc = Jsoup.connect(url).
                        userAgent(Constants.USER_AGENT).timeout(6*1000).get();
                Elements songTitle = doc.select("span.song-title");
                Elements artists = doc.select("span.author_list");
                for (int i=0;i<songTitle.size();i++){
                    SearchResult searchResult = new SearchResult();
                    Elements urls = songTitle.get(i).getElementsByTag("a");
                    searchResult.setUrl(urls.get(0).attr("href"));
                    searchResult.setMusicName(urls.get(0).text());
                    Elements artistElements = artists.get(i).getElementsByTag("a");
                    searchResult.setArtist(artistElements.get(0).text());

                    searchResult.setAlbum("热歌");
                    searchResults.add(searchResult);
                }


            } catch (IOException e){
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result){
            super.onPostExecute(result);
            if (result==1){
                netMusicAdapter = new NetMusicAdapter(mainActivity,searchResults);
                listView_net_music_list.setAdapter(netMusicAdapter);
                listView_net_music_list.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout,null));

            }
            load_layout.setVisibility(View.GONE);
            listView_net_music_list.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (position>=netMusicAdapter.getSearchResults().size()||position<0) return;
        showDownloadDialog(position);

    }

    private void showDownloadDialog(final int position){
        DownloadDialogFragment downloadDialogFragment = DownloadDialogFragment.newInstance(searchResults.get(position));
//        downloadDialogFragment.show(getFragmentManager(),"download");
    }
}
