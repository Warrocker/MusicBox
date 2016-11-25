package warrocker.musicbox.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class MyNameService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new MyNameReturner();
    }

    private class MyNameReturner implements Runnable{
        Socket socket;
        MyNameReturner(){
            Thread t = new Thread(this);
            t.start();
        }
        private void returnMyName(Socket socket) throws IOException{
            try(BufferedOutputStream nameOut = new BufferedOutputStream(socket.getOutputStream())){
                String device = Build.BRAND + " " +android.os.Build.MODEL ;
                nameOut.write(device.getBytes());
            }
        }
        @Override
        public void run() {
            try {
                ServerSocket nameServer = new ServerSocket(19001);

                socket = nameServer.accept();
                while (!nameServer.isClosed()){
                    returnMyName(socket);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
