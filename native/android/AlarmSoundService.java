package com.vaigarai.paani;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import java.io.File;
public class AlarmSoundService extends Service {
 private final Handler handler=new Handler();private MediaPlayer player;
 private float volume=0.12f;
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  String path=intent==null?"":intent.getStringExtra("localAudioPath");String label=intent==null?"Alarm":intent.getStringExtra("label");
  if(label==null)label="Alarm";
  AlarmReceiver.createChannel(this);
  Notification ongoing=new NotificationCompat.Builder(this,AlarmReceiver.CHANNEL_ID).setSmallIcon(getApplicationInfo().icon).setContentTitle(label).setContentText("Alarm is ringing").setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true).build();
  if(Build.VERSION.SDK_INT>=29)startForeground(7402,ongoing,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);else startForeground(7402,ongoing);
  play(path);return START_STICKY;
 }
 private void play(String path){
  releasePlayer();
  try{
   player=new MediaPlayer();player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
   if(path!=null&&!path.isEmpty()&&new File(path).isFile())player.setDataSource(path);else throw new Exception("Cached preview missing");
   player.setLooping(true);player.setVolume(volume,volume);player.setOnPreparedListener(mp->{mp.start();rampVolume();});player.setOnErrorListener((mp,what,extra)->{playDefault();return true;});player.prepareAsync();
  }catch(Exception e){playDefault();}
 }
 private void playDefault(){
  releasePlayer();try{player=MediaPlayer.create(this,R.raw.default_alarm);if(player==null)return;player.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());player.setLooping(true);player.setVolume(volume,volume);player.setOnPreparedListener(mp->{mp.start();rampVolume();});if(!player.isPlaying())player.start();}catch(Exception ignored){}
 }
 private void rampVolume(){handler.removeCallbacks(ramp);handler.post(ramp);}
 private final Runnable ramp=new Runnable(){public void run(){if(player==null||!player.isPlaying())return;volume=Math.min(1f,volume+0.08f);player.setVolume(volume,volume);if(volume<1f)handler.postDelayed(this,500);}};
 private void releasePlayer(){if(player!=null){try{player.stop();}catch(Exception ignored){}try{player.release();}catch(Exception ignored){}player=null;}}
 @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);releasePlayer();stopForeground(true);super.onDestroy();}
 @Override public IBinder onBind(Intent intent){return null;}
}
