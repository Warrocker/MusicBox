package warrocker.musicbox;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerPlayService extends Service {
    public final static byte PLAY_CODE = 1;
    public final static byte PAUSE_CODE = 2;
    public final static byte RESUME_CODE = 3;
    private static MediaPlayer mediaPlayer;
    byte[] byteArray = new byte[8192];

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new ServerRunnable();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
    private class ServerRunnable implements Runnable{
    ServerSocket serverSocket;
    ServerRunnable(){
        Thread thread = new Thread(this);
        thread.start();
    }
        //Записать файл и начать проигрывать
        private void writeAndPlayFile(BufferedInputStream bufferedInputStream, int seekTime) throws IOException{
            try(FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Music/Track.mp3")){


                int i;
                while ((i = bufferedInputStream.read(byteArray)) != -1) {
                    fos.write(byteArray, 0, i);
                }
            }
            mediaPlayer = null;
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(Environment.getExternalStorageDirectory() + "/Music/Track.mp3");
            mediaPlayer.prepare();
            mediaPlayer.seekTo(seekTime);
            mediaPlayer.start();

        }
    @Override
    public void run() {
        try {
             serverSocket = new ServerSocket(19000);

        }catch (IOException e){
            e.printStackTrace();
        }
        while (!serverSocket.isClosed()) {
           try{
               Socket socket = serverSocket.accept();
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
                            Log.e("QWE", String.valueOf(seekTime));
                            writeAndPlayFile(bufferedInputStream, seekTime);
                            break;
                        case ServerPlayService.PAUSE_CODE:
                            if (mediaPlayer.isPlaying()) {
                                mediaPlayer.stop();
                            }
                            break;
                        case ServerPlayService.RESUME_CODE:
                            if (mediaPlayer.isPlaying())
                            //Resume code
                            break;
                        default:
                            break;
                    }
                }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
}
}
