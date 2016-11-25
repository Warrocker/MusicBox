package warrocker.musicbox.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;


import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import warrocker.musicbox.AlertActivity;
import warrocker.musicbox.ClientPlayActivity;
import warrocker.musicbox.R;

public class ServerPlayService extends Service {
    public final static byte PLAY_CODE = 1;
    public final static byte PAUSE_CODE = 2;
    public static MediaPlayer mediaPlayer;

    //path to track
    public static String EXTERNAL_TRACK_PATH = Environment.getExternalStorageDirectory() + "/Music/Track.mp3";

    ServerRunnable serverRunnable;
    private final IBinder mBinder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serverRunnable = new ServerRunnable();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    public class LocalBinder extends Binder{
        public ServerPlayService getService() {
            // Return this instance of LocalService so clients can call public methods
            return ServerPlayService.this;
        }
    }
    public void setPermissionDeny() {
        serverRunnable.dropSocketConnection();
        serverRunnable.setAllowAccept(true);
    }
    public void setPermissionAllow() {
        serverRunnable.setSavedSocket(serverRunnable.getCurrentSocket());
        new ReadAndPlayThread(serverRunnable.getCurrentSocket());
        serverRunnable.setAllowAccept(true);
    }

    private class ServerRunnable implements Runnable{
        private ServerSocket serverSocket;
        private Thread thread;
        private boolean allowAccept = true;
        private Socket currentSocket;
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        private Socket savedSocket;
        private void setAllowAccept(boolean allowAccept) {
            this.allowAccept = allowAccept;
        }
        ServerRunnable(){
            thread = new Thread(this);
            thread.start();
        }
        private void sendNotification(){
            Intent intent = new Intent(ServerPlayService.this, AlertActivity.class);
            Notification notif = new Notification.Builder(ServerPlayService.this)
                    .setTicker("К Вам хотят подключиться")
                    .setContentTitle("Новое подключение")
                    .setContentText("Разрешить устройству подключиться?")
                    .setSmallIcon(R.drawable.ic_play)
                    .setContentIntent(PendingIntent.getActivity(ServerPlayService.this, 0, intent, 0))
                    .build();
            // ставим флаг, чтобы уведомление пропало после нажатия
            notif.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;

            // отправляем
            nm.notify(1, notif);
            Handler h = new Handler(Looper.getMainLooper());
            long delayInMilliseconds = 15000;
            h.postDelayed(new Runnable() {
                public void run() {
                    nm.cancel(1);
                }
            }, delayInMilliseconds);
        }
        @Override
        public void run() {
            try {
                serverSocket = createServerSocket();
                while (serverSocket != null && !serverSocket.isClosed()) {
                    while (allowAccept) {
                        currentSocket = serverSocket.accept();
                        if (savedSocket != null && savedSocket.getInetAddress().equals(currentSocket.getInetAddress())) {
                            //Записать файл и начать проигрывать
                            new ReadAndPlayThread(currentSocket);
                            this.setAllowAccept(true);
                        } else {
                            this.setAllowAccept(false);
                            sendNotification();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
        void dropSocketConnection(){
            try {
                this.currentSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
         ServerSocket createServerSocket(){
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(19000);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverSocket;
        }

        Socket getCurrentSocket() {
            return currentSocket;
        }
        Socket getSavedSocket() {
            return savedSocket;
        }
        void setSavedSocket(Socket savedSocket) {
            this.savedSocket = savedSocket;
        }
    }
private class ReadAndPlayThread implements Runnable{
    byte[] byteArray = new byte[8192];
    Socket socket;
    ReadAndPlayThread(Socket socket){
        this.socket = socket;
        Thread t = new Thread(this);
        t.start();
    }
    @Override
    public void run() {
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream())) {
            switch (bufferedInputStream.read()) {
                case ServerPlayService.PLAY_CODE:
                    if(mediaPlayer != null) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.stop();
                        }
                    }
                    int seekTime;
                    seekTime = readInt(bufferedInputStream);
                    writeAndPlayFile(bufferedInputStream, seekTime);
                    break;
                case ServerPlayService.PAUSE_CODE:
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    final int readInt(BufferedInputStream bufferedInputStream) throws IOException {
        int ch1 = bufferedInputStream.read();
        int ch2 = bufferedInputStream.read();
        int ch3 = bufferedInputStream.read();
        int ch4 = bufferedInputStream.read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
            throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }
    private void writeAndPlayFile(BufferedInputStream bufferedInputStream, int seekTime) throws IOException{
        try(FileOutputStream fos = new FileOutputStream(EXTERNAL_TRACK_PATH)){
            int i;
            while ((i = bufferedInputStream.read(byteArray)) != -1) {
                fos.write(byteArray, 0, i);
            }
            mediaPlayer = null;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(EXTERNAL_TRACK_PATH);
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        mediaPlayer.seekTo(seekTime);
        Intent intent = new Intent(getApplicationContext(), ClientPlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }

}
    }
