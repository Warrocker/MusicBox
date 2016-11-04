package warrocker.musicbox;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.TimeUnit;



public class FilesFragment extends ListFragment {
    ArrayAdapter fileAdapter;
    String[] pathes;
    ArrayList<String> trackList = new ArrayList<>();
    String targetDevice;
    String path;
    final static String TARGET_DEVICE = "targetDevice";
    static MediaMetadataRetriever metadataRetriever;
    final static String FILES_PREFIX = Environment.getExternalStorageDirectory() + "/Music/";

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        path = FILES_PREFIX;
        File musicDirectory = new File(path);
        pathes = musicDirectory.list(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".mp3");
                    }
                });
        for(int i = 0; i<pathes.length; i++){
            trackList.add(i, FILES_PREFIX + pathes[i]);
        }
        fileAdapter = new FileAdapter(getActivity());
        fileAdapter.setNotifyOnChange(true);
        setListAdapter(fileAdapter);
        Intent extraIntent= getActivity().getIntent();
        targetDevice = extraIntent.getStringExtra(TARGET_DEVICE);

    }


        @Override
        public void onListItemClick (ListView l, View v,int position, long id){
            String item = (String) getListAdapter().getItem(position);
            PlayFragment fragment = (PlayFragment) getFragmentManager().findFragmentById(R.id.activity_play);
            if (fragment != null && fragment.isInLayout()) {
                fragment.playTrack(item, targetDevice);
            } else {
                Intent intent = new Intent(getActivity().getApplicationContext(), PlayActivity.class);
                intent.putExtra(TARGET_DEVICE, targetDevice);
                intent.putExtra("trackPath",item);
                intent.putStringArrayListExtra("trackList", trackList);
                startActivity(intent);

            }
        }
    private class FileAdapter extends ArrayAdapter<String>{
         FileAdapter(Context context) {
            super(context, R.layout.file_item, trackList);
            this.notifyDataSetChanged();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            String trackPath = getItem(position);
            String fileName = pathes[position];
             metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(trackPath);
            String title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
            String artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
            Integer durationInMillis = Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            String duration = String.format(Locale.getDefault() ,"%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(durationInMillis),
                    TimeUnit.MILLISECONDS.toSeconds(durationInMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationInMillis))
            );
            if (title != null && title.length() != 0) {
                title = artist + " - " + title;
            } else {
                title = artist + " - " + fileName;
            }

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.file_item, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.textView2))
                    .setText(title);
            ((TextView) convertView.findViewById(R.id.textView))
                    .setText(duration);
            return convertView;
        }
    }
    }

