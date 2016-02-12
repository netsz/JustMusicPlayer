package net.justudio.justmusicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;

import net.justudio.justmusicplayer.R;
import net.justudio.justmusicplayer.model.Mp3Info;
import net.justudio.justmusicplayer.util.MediaUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/1/29 0029.
 */
public class MyMusicListAdapter extends BaseAdapter implements Scrollable {

    private Context context;
    private ArrayList<Mp3Info> mp3Infos;

    public MyMusicListAdapter(Context context,ArrayList<Mp3Info> mp3Infos){
        super();
        this.context=context;
        this.mp3Infos=mp3Infos;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    @Override
    public int getCount() {
        return mp3Infos.size();
    }

    @Override
    public Object getItem(int position) {
        return mp3Infos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView==null){
            convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.tv_listSongName=(TextView)convertView.findViewById(R.id.listSongName);
            viewHolder.tv_listSinger=(TextView)convertView.findViewById(R.id.listSinger);
            viewHolder.tv_listTime=(TextView)convertView.findViewById(R.id.listTime);
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        Mp3Info mp3Info = mp3Infos.get(position);
        viewHolder.tv_listSongName.setText(mp3Info.getTitle());
        viewHolder.tv_listSinger.setText(mp3Info.getArtist());
        viewHolder.tv_listTime.setText(MediaUtil.FormatTime(mp3Info.getDuration()) );
        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return Character.toString((mp3Infos.get(childposition)).getTitle().charAt(0));
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }


    static class ViewHolder{
        TextView tv_listSongName;
        TextView tv_listSinger;
        TextView tv_listTime;
    }
}
