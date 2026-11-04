package com.vaigarai.paani;
import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
public class AlarmSoundService extends Service {
 private static final String CLIENT_ID="ba853c15adbb4d8da15680aef114a163";
 private static final String REDIRECT_URI="vaigaraipaani://spotify-callback";
 private final Handler handler=new Handler();private Ringtone ringtone;private SpotifyAppRemote remote;private boolean spotifyStarted=false;
 @Override public int onStartCommand(Intent intent,int flags,int startId){
  String uri=intent==null?"":intent.getStringExtra("trackUri");String label=intent==null?"Alarm":intent.getStringExtra("label");
  if(label==null)label="Alarm";if(uri==null)uri="";
  AlarmReceiver.createChannel(this);Notification ongoing=new NotificationCompat.Builder(this,AlarmReceiver.CHANNEL_ID).setSmallIcon(getApplicationInfo().icon).setContentTitle(label).setContentText("Alarm is ringing").setCategory(NotificationCompat.CATEGORY_ALARM).setOngoing(true).build();
  if(Build.VERSION.SDK_INT>=29)startForeground(7402,ongoing,android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);else startForeground(7402,ongoing);
  if(!uri.isEmpty())connectSpotify(uri);handler.postDelayed(()->{if(!spotifyStarted)startFallbackTone();},7000);return START_STICKY;
 }
 private void connectSpotify(String uri){
  ConnectionParams params=new ConnectionParams.Builder(CLIENT_ID).setRedirectUri(REDIRECT_URI).showAuthView(false).build();
  SpotifyAppRemote.connect(getApplicationContext(),params,new Connector.ConnectionListener(){
   @Override public void onConnected(SpotifyAppRemote app){remote=app;try{app.getPlayerApi().play(uri);spotifyStarted=true;stopFallbackTone();}catch(Exception e){startFallbackTone();}}
   @Override public void onFailure(Throwable error){startFallbackTone();}
  });
 }
 private void startFallbackTone(){if(spotifyStarted||ringtone!=null)return;try{Uri sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);if(sound==null)sound=RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);ringtone=RingtoneManager.getRingtone(this,sound);if(ringtone!=null){if(Build.VERSION.SDK_INT>=28)ringtone.setLooping(true);ringtone.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());ringtone.play();}}catch(Exception ignored){}}
 private void stopFallbackTone(){if(ringtone!=null){try{ringtone.stop();}catch(Exception ignored){}ringtone=null;}}
 @Override public void onDestroy(){handler.removeCallbacksAndMessages(null);stopFallbackTone();if(remote!=null){try{remote.getPlayerApi().pause();}catch(Exception ignored){}SpotifyAppRemote.disconnect(remote);remote=null;}stopForeground(true);super.onDestroy();}
 @Override public IBinder onBind(Intent intent){return null;}
}
