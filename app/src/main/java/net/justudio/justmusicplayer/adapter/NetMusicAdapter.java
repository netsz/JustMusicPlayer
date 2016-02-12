package net.justudio.justmusicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import net.justudio.justmusicplayer.R;
import net.justudio.justmusicplayer.model.SearchResult;

import java.util.ArrayList;

/**
 * 网络音乐适配器
 */
public class NetMusicAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<SearchResult> searchResults;

    public NetMusicAdapter(Context context, ArrayList<SearchResult> searchResults){
        super();
        this.context=context;
        this.searchResults = searchResults;

    }

    public void setSearchResults(ArrayList<SearchResult> searchResults) {
        this.searchResults = searchResults;
    }

    public ArrayList<SearchResult> getSearchResults() {
        return searchResults;
    }

    @Override
    public int getCount() {
        return searchResults.size();
    }

    @Override
    public Object getItem(int position) {
        return searchResults.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.net_music_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.net_listSongName=(TextView)convertView.findViewById(R.id.net_listSongName);
            viewHolder.net_listSinger=(TextView)convertView.findViewById(R.id.net_listSinger);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        SearchResult searchResult = searchResults.get(position);
        viewHolder.net_listSongName.setText(searchResult.getMusicName());
        viewHolder.net_listSinger.setText(searchResult.getArtist());
        return convertView;
    }

    static class ViewHolder{
        TextView net_listSongName;
        TextView net_listSinger;
    }
}
