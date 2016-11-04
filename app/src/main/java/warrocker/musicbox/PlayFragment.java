package warrocker.musicbox;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


public class PlayFragment extends Fragment {
    ImageButton playPauseButton;
    ImageButton previousButton;
    ImageButton nextButton;
    String trackPath;
    String targetDevice;
    static String DEFAULT_TITLE = "Unknown Track";
    String title;
    String artist;
    String album;
    Integer duration;
    TextView albumTitle;
    TextView albumText;
    ImageView albumArt;
    SeekBar trackDurationProgressBar;
    CountDownTimer playTimer;
    ArrayList<String> trackList;
    boolean isPlaying = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_play, container, false);
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Intent in = getActivity().getIntent();
        targetDevice = in.getStringExtra(FilesFragment.TARGET_DEVICE);
        trackList = in.getStringArrayListExtra("trackList");
        setRetainInstance(true);
    }
    private void getTrackInfo(String trackPath){
        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(trackPath);
        title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
        artist = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        duration = Integer.parseInt(metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
        if (title != null && title.length() != 0) {
            title = artist + " - " + title;
            albumTitle.setText(title);
        } else {
            title = artist + " - " + DEFAULT_TITLE;
            albumTitle.setText(title);
        }
        albumText.setText(album);
        trackDurationProgressBar.setMax(duration);
        trackDurationProgressBar.setOnSeekBarChangeListener(trackBarListener);
        byte[] art = metadataRetriever.getEmbeddedPicture();
        if (art != null){
            Bitmap songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
            albumArt.setImageBitmap(songImage);
        }else {
            albumArt.setImageResource(R.mipmap.vinyl);
        }
    }
    public void playTrack(String trackPath, String targetDevice){
        initUI();
        this.trackPath = trackPath;
        this.targetDevice = targetDevice;
        if(getView() != null) {
            getTrackInfo(trackPath);
            playPauseButton.setOnClickListener(playClick);
            playTimer = new CountDownTimer(duration, 50) {
                public void onTick(long millisUntilFinished) {
                    trackDurationProgressBar.setProgress(trackDurationProgressBar.getProgress() + 50);
                }
                public void onFinish() {
                    isPlaying = false;
                }
            };
        }
    }
    private void initUI(){
        if(getView() != null) {
            playPauseButton = (ImageButton) getView().findViewById(R.id.play_button);
            playPauseButton.setVisibility(View.VISIBLE);
            nextButton = (ImageButton) getView().findViewById(R.id.next_button);

            nextButton.setOnClickListener(new View.OnClickListener() {
                int currentPosition;
                @Override
                public void onClick(View v) {
                    for(int i = 0; i< trackList.size(); i++){
                        String track = trackList.get(i);
                        if(track.equals(trackPath)){
                            currentPosition = i;
                            break;
                        }
                    }
                    if(currentPosition+1 != trackList.size()){
                        String nextTrack = trackList.get(currentPosition + 1);
                        getTrackInfo(nextTrack);
                        trackPath = nextTrack;
                        trackDurationProgressBar.setProgress(0);
                        if(isPlaying){
                            new PlayTask().execute(trackPath, targetDevice, String.valueOf(0));
                        }
                    }
                }
            });

            previousButton = (ImageButton) getView().findViewById(R.id.previous_button);
            previousButton.setOnClickListener(new View.OnClickListener() {
                int currentPosition;
                @Override
                public void onClick(View v) {
                    for(int i = 0; i< trackList.size(); i++){
                        String track = trackList.get(i);
                        if(track.equals(trackPath)){
                            currentPosition = i;
                            break;
                        }
                    }
                    if(currentPosition != 0){
                        String nextTrack = trackList.get(currentPosition - 1);
                        getTrackInfo(nextTrack);
                        trackPath = nextTrack;
                        trackDurationProgressBar.setProgress(0);
                        if(isPlaying){
                            new PlayTask().execute(trackPath, targetDevice, String.valueOf(0));
                        }
                    }
                }
            });
            albumTitle = (TextView) getView().findViewById(R.id.album_title);
            albumText = (TextView) getView().findViewById(R.id.album_text);
            albumArt = (ImageView) getView().findViewById(R.id.album_image);
            trackDurationProgressBar = (SeekBar) getView().findViewById(R.id.track_duration);

        }
    }
    //play click Listener
    View.OnClickListener playClick = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            //Change play button to pause button
            playPauseButton.setImageResource(R.drawable.ic_pause);
            new PlayTask().execute(trackPath, targetDevice, String.valueOf(trackDurationProgressBar.getProgress()));
            playPauseButton.setOnClickListener(pauseClick);
            isPlaying = true;
        }
    };
    //pause click listener
    View.OnClickListener pauseClick = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            new PauseTask().execute(targetDevice);
            playPauseButton.setImageResource(R.drawable.ic_play);
            playPauseButton.setOnClickListener(playClick);
            isPlaying = false;
        }
    };
    SeekBar.OnSeekBarChangeListener trackBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            new PlayTask().execute(trackPath, targetDevice, String.valueOf(seekBar.getProgress()));
        }
    };
    class PlayTask extends AsyncTask<String, Void, Boolean> {
        ProgressDialog progress = new ProgressDialog(getActivity());
        private Socket socket;

        //Пишем интовое знчение в поток
        final void writeInt(BufferedOutputStream bufferedOutputStream, int v) throws IOException {
            bufferedOutputStream.write((v >>> 24) & 0xFF);
            bufferedOutputStream.write((v >>> 16) & 0xFF);
            bufferedOutputStream.write((v >>>  8) & 0xFF);
            bufferedOutputStream.write((v) & 0xFF);
        }
        @Override
        protected void onPreExecute(){
            playTimer.cancel();
            progress.setMessage("Загрузка...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }
        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                socket = new Socket(strings[1], 19000);
            }catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getActivity(), "Невозможно подключиться к устройству", Toast.LENGTH_SHORT).show();
            }
                Log.e("QWEALL", "truee");
                try (FileInputStream fileInputStream = new FileInputStream(strings[0]);
                     BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
                    byte[] byteArray = new byte[8092];
                    int i;
                    out.write(ServerPlayService.PLAY_CODE);
                    writeInt(out, Integer.parseInt(strings[2]));
                    while ((i = fileInputStream.read(byteArray)) != -1) {
                        out.write(byteArray, 0, i);
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("QWEACC", "false1");
                    return false;
                }
        }
        @Override
        protected void onPostExecute(Boolean aBool) {
            super.onPostExecute(aBool);
            progress.cancel();
            if(aBool) {
                playTimer.start();
            }else {
                Toast.makeText(getActivity(), "Неудалось подключиться", Toast.LENGTH_SHORT).show();
            }
        }
    }
    class PauseTask extends AsyncTask<String, Void, Void> {
        private Socket socket;
        @Override
        protected void onPreExecute(){

        }
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socket = new Socket(strings[0], 19000);
            try(BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream())) {
                out.write(ServerPlayService.PAUSE_CODE);
            }
            }catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getActivity(), "Неудалось подключиться", Toast.LENGTH_SHORT).show();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            playTimer.cancel();
        }
    }


}
