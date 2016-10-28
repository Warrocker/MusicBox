package warrocker.musicbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class PlayActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String targetDevice = intent.getStringExtra("targetDevice");
        String trackPath = intent.getStringExtra("trackPath");
        setContentView(R.layout.play_fragment);
        PlayFragment fragment = (PlayFragment) getFragmentManager().findFragmentById(R.id.activity_play);
        if (fragment != null && fragment.isInLayout()) {
            fragment.playTrack(trackPath, targetDevice);
        }
    }
}
