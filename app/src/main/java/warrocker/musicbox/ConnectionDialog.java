package warrocker.musicbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ConnectionDialog extends DialogFragment {
    boolean bound = false;
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = "Подключить устройство?";
        String title = "Подключение";
        String connextText = "Да";
        String cancelText = "Нет";

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);  // заголовок
        builder.setMessage(message);
        builder.setCancelable(false);

       final ServiceConnection sConn = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder binder) {
                bound = true;
            }

            public void onServiceDisconnected(ComponentName name) {
                bound = false;
            }
        };
        if(bound){
            getActivity().unbindService(sConn);
        }
        builder.setPositiveButton(connextText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getActivity(), ServerPlayService.class);
                intent.putExtra("THREAD_STATUS", true);
                if(bound){
                    getActivity().unbindService(sConn);
                }else{
                    getActivity().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
                    getActivity().finish();
                }


            }
        });
        builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent = new Intent(getActivity(), ServerPlayService.class);
                intent.putExtra("THREAD_STATUS", false);
                getActivity().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
                getActivity().finish();
            }
        });

        return builder.create();

    }
}
