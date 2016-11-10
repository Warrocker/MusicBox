package warrocker.musicbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;

public class DeviceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.stopService(new Intent(this, ServerPlayService.class));
        this.startService(new Intent(this, ServerPlayService.class));
        this.startService(new Intent(this, MyNameService.class));
        setContentView(R.layout.device_fragment);

    }
}
