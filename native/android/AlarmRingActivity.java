package com.vaigarai.paani;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
public class AlarmRingActivity extends Activity {
 private String id="",label="Alarm",track="",artist="",audio="",art="";
 @Override protected void onCreate(Bundle b){super.onCreate(b);if(android.os.Build.VERSION.SDK_INT>=27){setShowWhenLocked(true);setTurnScreenOn(true);}getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);read(getIntent());render();}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);read(i);render();}
 private void read(Intent i){id=i.getStringExtra("id")==null?"":i.getStringExtra("id");label=i.getStringExtra("label")==null?"Alarm":i.getStringExtra("label");track=i.getStringExtra("trackName")==null?"":i.getStringExtra("trackName");artist=i.getStringExtra("trackArtist")==null?"":i.getStringExtra("trackArtist");audio=i.getStringExtra("localAudioPath")==null?"":i.getStringExtra("localAudioPath");art=i.getStringExtra("localArtworkPath")==null?"":i.getStringExtra("localArtworkPath");}
 private void render(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(36,48,36,48);root.setBackgroundColor(Color.rgb(7,8,10));
  TextView brand=new TextView(this);brand.setText("வைகறைப் பாணி");brand.setTextColor(Color.rgb(198,201,206));brand.setTextSize(14);brand.setLetterSpacing(.16f);brand.setGravity(Gravity.CENTER);root.addView(brand,new LinearLayout.LayoutParams(-1,-2));
  TextView time=new TextView(this);time.setText(new java.text.SimpleDateFormat("hh:mm",java.util.Locale.getDefault()).format(new java.util.Date()));time.setTextColor(Color.WHITE);time.setTextSize(68);time.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));time.setGravity(Gravity.CENTER);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=36;root.addView(time,tp);
  TextView title=new TextView(this);title.setText(label);title.setTextColor(Color.WHITE);title.setTextSize(22);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=18;root.addView(title,lp);
  if(!track.isEmpty()){TextView t=new TextView(this);t.setText("♫  "+track);t.setTextColor(Color.LTGRAY);t.setTextSize(12);t.setGravity(Gravity.CENTER);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=10;root.addView(t,p);}
  Button snooze=new Button(this);snooze.setText("Snooze · 5 minutes");snooze.setAllCaps(false);snooze.setOnClickListener(v->snooze());LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,58);sp.topMargin=62;root.addView(snooze,sp);
  Button dismiss=new Button(this);dismiss.setText("Dismiss");dismiss.setAllCaps(false);dismiss.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(198,201,206)));dismiss.setTextColor(Color.rgb(16,18,22));dismiss.setOnClickListener(v->dismiss());LinearLayout.LayoutParams dp=new LinearLayout.LayoutParams(-1,58);dp.topMargin=12;root.addView(dismiss,dp);setContentView(root);
 }
 private void snooze(){try{AlarmScheduler.scheduleSnooze(this,id,label,track,artist,audio,art);}catch(Exception ignored){}dismiss();}
 private void dismiss(){stopService(new Intent(this,AlarmSoundService.class));((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(AlarmReceiver.NOTIFICATION_ID);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(7402);finish();}
}
