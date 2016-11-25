package warrocker.musicbox;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WifiList extends DialogFragment {
    ListView wifiListView;
    LinearLayout linearView;
    List<ScanResult> wifiConfList = new ArrayList<>();
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder wifiDialogBuilder = new AlertDialog.Builder(getActivity());
        linearView = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.list_view_layout, null);
        wifiDialogBuilder.setView(linearView);

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

        while(wifiManager.getScanResults().isEmpty()) {
            wifiConfList = wifiManager.getScanResults();
        }
        wifiListView = (ListView) linearView.findViewById(R.id.files_list);
        wifiListView.setAdapter(new WifiListAdapter(getActivity()));
        return wifiDialogBuilder.create();
    }
    private class WifiListAdapter extends ArrayAdapter<ScanResult> {
        public WifiListAdapter(Context context) {
            super(context, R.layout.file_item, wifiConfList);
        }
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ScanResult wifiConfiguration = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.file_item, parent, false);
            }
            if (wifiConfiguration != null) {
                ((TextView) convertView.findViewById(R.id.topTextView))
                        .setText(wifiConfiguration.SSID);
                ((TextView) convertView.findViewById(R.id.bottomTextView))
                        .setText(wifiConfiguration.capabilities);
            }
            return convertView;
        }
    }
}
