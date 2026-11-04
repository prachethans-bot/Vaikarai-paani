package com.vaigarai.paani;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
public class AlarmReceiver extends BroadcastReceiver {
 static final String CHANNEL_ID="vaigarai_alarm_channel";static final int NOTIFICATION_ID=7401;
 @Override public void onReceive(Context c,Intent intent){
  String id=intent.getStringExtra("id");if(id==null)return;org.json.JSONObject r=AlarmScheduler.consumeAndAdvance(c,id);if(r==null)return;
  String label=r.optString("label","Alarm");String uri=r.optString("trackUri","");String track=r.optString("trackName","");
  c.getSharedPreferences("vaigarai_active_ring",Context.MODE_PRIVATE).edit().putString("id",id).putString("label",label).putString("trackUri",uri).putString("trackName",track).apply();
  Intent service=new Intent(c,AlarmSoundService.class).putExtra("id",id).putExtra("label",label).putExtra("trackUri",uri).putExtra("trackName",track);
  try{if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)c.startForegroundService(service);else c.startService(service);}catch(Exception ignored){}
  createChannel(c);Intent ring=new Intent(c,AlarmRingActivity.class).putExtra("id",id).putExtra("label",label).putExtra("trackUri",uri).putExtra("trackName",track);
  ring.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
  PendingIntent full=PendingIntent.getActivity(c,id.hashCode(),ring,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification n=new NotificationCompat.Builder(c,CHANNEL_ID).setSmallIcon(c.getApplicationInfo().icon).setContentTitle(label).setContentText(track.isEmpty()?"It's time to wake up.":track).setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX).setVisibility(NotificationCompat.VISIBILITY_PUBLIC).setOngoing(true).setAutoCancel(false).setFullScreenIntent(full,true).setContentIntent(full).build();
  ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID,n);
 }
 static void createChannel(Context c){if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){NotificationManager m=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);NotificationChannel ch=new NotificationChannel(CHANNEL_ID,"Alarms",NotificationManager.IMPORTANCE_HIGH);ch.setDescription("Alarm ringing and countdown completion");ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);ch.enableVibration(true);m.createNotificationChannel(ch);}}
}
