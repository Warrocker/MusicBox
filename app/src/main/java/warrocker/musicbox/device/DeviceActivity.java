package warrocker.musicbox.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import warrocker.musicbox.R;
import warrocker.musicbox.services.MyNameService;
import warrocker.musicbox.services.ServerPlayService;

public class DeviceActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.startService(new Intent(this, ServerPlayService.class));
        this.startService(new Intent(this, MyNameService.class));
        setContentView(R.layout.device_fragment);
        Toolbar toolbar =(Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_toolbar, menu);

        return true;
    }

}
