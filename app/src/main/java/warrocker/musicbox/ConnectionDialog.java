package warrocker.musicbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;

import warrocker.musicbox.services.ServerPlayService;


public class ConnectionDialog extends DialogFragment {
    boolean bound = false;
    ServerPlayService serverPlayService;
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), ServerPlayService.class);
        getActivity().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(bound){
            getActivity().unbindService(sConn);
            bound = false;
        }
    }

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
        builder.setPositiveButton(connextText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                serverPlayService.setPermissionAllow();
                getActivity().finish();
            }
        });
        builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                serverPlayService.setPermissionDeny();
                getActivity().finish();
            }
        });

        return builder.create();

    }
    ServiceConnection sConn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder binder) {
            bound = true;
            ServerPlayService.LocalBinder localBinder = (ServerPlayService.LocalBinder) binder;
            serverPlayService = localBinder.getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };
}
