package net.justudio.justmusicplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import net.justudio.justmusicplayer.model.SearchResult;
import net.justudio.justmusicplayer.util.DownloadUtil;

import java.io.File;

/**
 * Created by Administrator on 2016/2/5 0005.
 */
public class DownloadDialogFragment extends DialogFragment {
    private SearchResult searchResult;
    private MainActivity mainActivity;


    public static DownloadDialogFragment newInstance(SearchResult searchResult){
        DownloadDialogFragment downloadDialogFragment = new DownloadDialogFragment();
        downloadDialogFragment.searchResult = searchResult;
        return downloadDialogFragment;
        }

    private String[] items;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity)getActivity();
        items=new String[]{"下载","取消"};
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        downloadMusic();
                        break;
                    case 1:
                        dialog.dismiss();
                        break;
                }

            }
        });
        return builder.show();

    }

    private void downloadMusic(){
        Toast.makeText(mainActivity,"正在下载"+searchResult.getMusicName(),Toast.LENGTH_LONG).show();
        DownloadUtil.getInstance().setListener(new DownloadUtil.OnDownlaodlistener(){

            @Override
            public void onDownload(String mp3Url){
                Toast.makeText(mainActivity,"下载成功",Toast.LENGTH_SHORT).show();
                //扫描下载歌曲
//                Uri contentUri = Uri.fromFile(new File(mp3Url));
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
//                getContext().sendBroadcast(mediaScanIntent);

            }

            @Override
            public void onFailed(String error){
                Toast.makeText(mainActivity,"下载失败",Toast.LENGTH_LONG).show();
            }


        }).download(searchResult);
    }

}
