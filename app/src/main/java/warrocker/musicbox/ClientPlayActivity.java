package warrocker.musicbox;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import static warrocker.musicbox.ServerPlayService.EXTERNAL_TRACK_PATH;
import static warrocker.musicbox.ServerPlayService.mediaPlayer;

public class ClientPlayActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        initUI();
        getTrackInfo(EXTERNAL_TRACK_PATH);
        playTimer = new CountDownTimer(duration, 50) {
            public void onTick(long millisUntilFinished) {
                trackDurationProgressBar.setProgress(mediaPlayer.getCurrentPosition());
            }
            public void onFinish() {
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mediaPlayer.isPlaying()){
            playPauseButton.setImageResource(R.drawable.ic_pause);
            playPauseButton.setOnClickListener(pauseClick);
            playTimer.start();
        }else{
            playPauseButton.setImageResource(R.drawable.ic_play);
            playPauseButton.setOnClickListener(playClick);
        }
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
        trackDurationProgressBar.setActivated(false);
        byte[] art = metadataRetriever.getEmbeddedPicture();
        if (art != null){
            Bitmap songImage = BitmapFactory
                    .decodeByteArray(art, 0, art.length);
            albumArt.setImageBitmap(songImage);
        }else {
            albumArt.setImageResource(R.mipmap.vinyl);
        }
    }
    private void initUI() {
        playPauseButton = (ImageButton) findViewById(R.id.play_button);
        playPauseButton.setVisibility(View.VISIBLE);
        nextButton = (ImageButton) findViewById(R.id.next_button);
        nextButton.setVisibility(View.INVISIBLE);
        previousButton = (ImageButton) findViewById(R.id.previous_button);
        previousButton.setVisibility(View.INVISIBLE);
        albumTitle = (TextView) findViewById(R.id.album_title);
        albumText = (TextView) findViewById(R.id.album_text);
        albumArt = (ImageView) findViewById(R.id.album_image);
        trackDurationProgressBar = (SeekBar) findViewById(R.id.track_duration);
        trackDurationProgressBar.setActivated(false);


    }
        //play click Listener
        View.OnClickListener playClick = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //Change play button to pause button
                playPauseButton.setImageResource(R.drawable.ic_pause);

                mediaPlayer.start();
                playPauseButton.setOnClickListener(pauseClick);

            }
        };
        //pause click listener
        View.OnClickListener pauseClick = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                playPauseButton.setImageResource(R.drawable.ic_play);
                mediaPlayer.pause();
                playTimer.cancel();

                playPauseButton.setOnClickListener(playClick);
            }
        };
    }
