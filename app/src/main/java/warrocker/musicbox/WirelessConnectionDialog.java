package warrocker.musicbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WirelessConnectionDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = "Вы не подключены к беспроводной локальной сети. Создать новую или подключиться к существующей?";
        String title = "Не создано подключение";
        String createText = "Создать";
        String connextText = "Подключиться";
        String cancelText = "Отмена";

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);  // заголовок
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton(connextText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                wifiManager.setWifiEnabled(true);
//                Log.e("QWE", wifiManager.getScanResults().toString());
            }
        });
        builder.setNegativeButton(cancelText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        builder.setNeutralButton(createText, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int id) {
                WifiConfiguration accessPointConneсtion = new WifiConfiguration();
                accessPointConneсtion.SSID = "MBOX";
                accessPointConneсtion.allowedAuthAlgorithms.set(
                        WifiConfiguration.AuthAlgorithm.OPEN);
                WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
                Method[] wmMethods = wifiManager.getClass().getDeclaredMethods(); //Get all declared methods in WifiManager class
                for (Method method: wmMethods){
                    if (method.getName().equals("setWifiApEnabled")){
                try {
                    boolean apstatus = (Boolean) method.invoke(wifiManager, accessPointConneсtion,true);
//                    getActivity().finish();

                    //statusView.setText("Creating a Wi-Fi Network \""+netConfig.SSID+"\"");
                    for (Method isWifiApEnabledmethod: wmMethods)
                    {
                        if (isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){

                            for (Method method1: wmMethods){
                                if(method1.getName().equals("getWifiApState")){
                                    int apstate;
                                    apstate = (Integer)method1.invoke(wifiManager);
                                    if(apstatus){
                                        while(apstate != 13){
                                            apstate = (Integer)method1.invoke(wifiManager);
                                            Log.e("QWE", String.valueOf(apstate));
                                        }
                                            setShowsDialog(false);
                                            getActivity().recreate();
                                    }
                                }
                            }
                        }
                    }

                }
                catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                    }
        }

            }

        });
        return builder.create();

    }

}
