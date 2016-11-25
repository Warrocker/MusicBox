package warrocker.musicbox;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;



public class AlertActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.device_fragment);
        initConnectionDialog();
    }
    private void initConnectionDialog(){
        FragmentManager manager = getFragmentManager();
        ConnectionDialog myDialogFragment = new ConnectionDialog();
        myDialogFragment.show(manager, "dialog");

    }

}
