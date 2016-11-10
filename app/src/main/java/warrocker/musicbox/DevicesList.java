package warrocker.musicbox;

import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

public class DevicesList extends ListFragment {
    ArrayAdapter<Device> devicesAdapter;
    static ArrayList<Device> devices = new ArrayList<>();
    Handler deviceHandler;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

//        setRetainInstance(true);
        getActivity().startService(new Intent(getActivity() , ServerPlayService.class));
        devicesAdapter = new DeviceAdapter(getActivity(), devices);
        setListAdapter(devicesAdapter);
        deviceHandler = new DeviceHandler();
    }

    @Override
    public void onResume() {
        super.onResume();
        new DeviceTask().execute();
        devicesAdapter.notifyDataSetChanged();
    }

    private class DeviceAdapter extends ArrayAdapter<Device> {
        DeviceAdapter(Context context, ArrayList<Device> devices) {
            super(context, R.layout.file_item, devices);
            this.setNotifyOnChange(true);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            Device device = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.file_item, parent, false);
            }
            if (device != null) {
                ((TextView) convertView.findViewById(R.id.textView2))
                        .setText(device.getDeviceName());
                ((TextView) convertView.findViewById(R.id.textView))
                        .setText(device.getIpAddress());

            }
            return convertView;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        Device device = devicesAdapter.getItem(position);

        Intent intent = new Intent(getActivity().getApplicationContext(), ShowFilesActivity.class);
        if (device != null) {
            intent.putExtra("targetDevice", device.getIpAddress());
        }
        startActivity(intent);
        devicesAdapter.notifyDataSetChanged();
    }
    private void initWirelessDialog(){
        FragmentManager manager = getFragmentManager();
        WirelessConnectionDialog myDialogFragment = new WirelessConnectionDialog();
        myDialogFragment.show(manager, "dialog");
    }
    private class DeviceTask extends AsyncTask<Void, Void, ArrayList<Device>> {
        ArrayList<Device> devices = new ArrayList<>();
        Enumeration<InetAddress> inetAddresses;
        @Override
        protected ArrayList<Device> doInBackground(Void... params) {
            try {
                if (NetworkInterface.getByName("wlan0") != null && NetworkInterface.getByName("wlan0").getInetAddresses() != null && NetworkInterface.getByName("wlan0").getInetAddresses().hasMoreElements() && !NetworkInterface.getByName("wlan0").getInetAddresses().nextElement().getHostAddress().equals("0.0.0.0")) {
                    inetAddresses = NetworkInterface.getByName("wlan0").getInetAddresses();

                if (inetAddresses.hasMoreElements()) {
                    getInterfaceAddress(inetAddresses);
                }
                } else {
                    if (NetworkInterface.getByName("ap0") != null) {
                        inetAddresses = NetworkInterface.getByName("ap0").getInetAddresses();
                        if (inetAddresses.hasMoreElements()) {
                            getInterfaceAddress(inetAddresses);
                        }
                    }else{
                        initWirelessDialog();
                        inetAddresses = NetworkInterface.getByName("lo").getInetAddresses();
                        if (inetAddresses.hasMoreElements()) {
                            try {
                                getDeviceInfo(InetAddress.getLocalHost());
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            return devices;
        }

        @Override
        protected void onPostExecute(ArrayList<Device> strings) {
            super.onPostExecute(strings);
            getActivity().runOnUiThread(new Runnable() {
                public void run() {

                    devicesAdapter.notifyDataSetChanged();
                }
            });
        }
        private void getInterfaceAddress(Enumeration<InetAddress> inetAddresses) {
            for (InetAddress inetAddress : Collections.list(inetAddresses)) {
                getDeviceInfo(inetAddress);
            }
        }
        private void getDeviceInfo(InetAddress inetAddress) {

                String IPv4Address = inetAddress.getHostAddress();
                int ipv4Length = IPv4Address.length();
                if (ipv4Length <= 15) {
                    String[] mask = IPv4Address.split("\\.");

                    for (int i = 0; i <= 255; i++) {
                        //create ip address for x.x.x.0 to x.x.x.255
                        String address = mask[0] + "." + mask[1] + "." + mask[2] + "." + i;
                        new DeviceRunnable(address, i);
                    }
                }
        }
    }
    class DeviceRunnable implements Runnable{
        Socket getDevSocket;
        Message devMessage;
        String ipAddress;
        int counter;
        DeviceRunnable(String ipAddress, int counter){
            this.ipAddress = ipAddress;
            this.counter = counter;
            Thread t = new Thread(this);
            t.start();
        }
        @Override
        public void run() {
            try {
                if (InetAddress.getByName(ipAddress).isReachable(3000)) {
                    getDevSocket = new Socket(ipAddress, 19001);
                    byte[] byteArr = new byte[8192];
                    try (BufferedInputStream bufferedInputStream = new BufferedInputStream(getDevSocket.getInputStream())) {
                        int count = bufferedInputStream.read(byteArr);
                        String deviceName = new String(byteArr, 0, count);
                        devMessage  =  deviceHandler.obtainMessage(counter, new Device(deviceName, ipAddress));
                        deviceHandler.sendMessage(devMessage);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static class DeviceHandler extends Handler{

        @Override
        public void handleMessage(Message msg) {
            devices.add((Device) msg.obj);
        }
    }
}
