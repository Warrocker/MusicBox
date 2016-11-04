package warrocker.musicbox;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Queue;

import static warrocker.musicbox.FilesFragment.TARGET_DEVICE;

public class ServerPlayService extends Service {
    public final static byte PLAY_CODE = 1;
    public final static byte PAUSE_CODE = 2;
    public final static byte ALLOW_CODE = 3;
    public final static byte DENY_CODE = 4;
    public static MediaPlayer mediaPlayer;
    byte[] byteArray = new byte[8192];
    //path to track
    public static String EXTERNAL_TRACK_PATH = Environment.getExternalStorageDirectory() + "/Music/Track.mp3";
    NotificationManager nm;
    ServerRunnable serverRunnable;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("qwe", String.valueOf(intent.getBooleanExtra("ACCESS", true)));
        if(intent.getBooleanExtra("ACCESS", false)) {
            new test(serverRunnable.getCurrentSocket());
            serverRunnable.setAllowAccept(true);
        }else {
            serverRunnable.setAllowAccept(true);
            serverRunnable.setCurrentSocket(null);
        }
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        serverRunnable = new ServerRunnable();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    private class ServerRunnable implements Runnable{
        ServerSocket serverSocket;
        Thread thread;
        boolean allowAccept = true;
        Socket newSocket;



        Socket currentSocket;

        private void setAllowAccept(boolean allowAccept) {
            this.allowAccept = allowAccept;
        }

        ServerRunnable(){
            thread = new Thread(this);
            thread.start();
        }
        //Записать файл и начать проигрывать
        private void sendNotification(){
            Intent intent = new Intent(ServerPlayService.this, AlertActivity.class);
            Notification notif = new Notification.Builder(ServerPlayService.this)
                    .setTicker("К Вам хотят подключиться")
                    .setContentTitle("Новое подключение")
                    .setContentText(
                            "Разрешить устройству подключиться?")
                    .setSmallIcon(R.drawable.ic_play)
                    .setContentIntent(PendingIntent.getActivity(ServerPlayService.this, 0, intent, 0))
                    .build();

            // ставим флаг, чтобы уведомление пропало после нажатия
            notif.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_NO_CLEAR;

            // отправляем
            nm.notify(1, notif);
        }
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(19000);
            }catch (IOException e){
                e.printStackTrace();
            }
            while (!serverSocket.isClosed()) {
                while (allowAccept) {
                    try {
                        newSocket = serverSocket.accept();
                        if(currentSocket != null && currentSocket.getInetAddress().equals(newSocket.getInetAddress())) {
                            currentSocket = newSocket;
                            new test(currentSocket);
                            this.setAllowAccept(true);
                        }else {
                            setAllowAccept(false);
                            currentSocket = newSocket;
                            sendNotification();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
         Socket getCurrentSocket() {
            return currentSocket;
        }

         void setCurrentSocket(Socket currentSocket) {
            this.currentSocket = currentSocket;
        }
    }
private class test implements Runnable{
    Socket socket;
    test(Socket socket){
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
        }
        mediaPlayer = null;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDataSource(EXTERNAL_TRACK_PATH);
        mediaPlayer.prepare();
        mediaPlayer.seekTo(seekTime);
        mediaPlayer.start();
        Intent intent = new Intent(getApplicationContext(), ClientPlayActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

    }

}
    }
