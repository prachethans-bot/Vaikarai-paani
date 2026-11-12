package com.vaigarai.paani;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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
 private int dp(float value){return (int)(value*getResources().getDisplayMetrics().density+0.5f);}
 private Button actionButton(String text,int background,int stroke,Runnable action){
  Button button=new Button(this);button.setText(text);button.setAllCaps(false);button.setTextColor(Color.WHITE);button.setTextSize(16);button.setTypeface(Typeface.create("sans-serif",Typeface.BOLD));button.setGravity(Gravity.CENTER);button.setPadding(dp(18),0,dp(18),0);button.setMinHeight(dp(64));button.setMinimumHeight(dp(64));button.setBackground(new GradientDrawable(){ {setColor(background);setCornerRadius(dp(18));setStroke(dp(2),stroke);} });button.setElevation(dp(3));button.setOnClickListener(v->action.run());button.setContentDescription(text);return button;
 }
 private void render(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setGravity(Gravity.CENTER);root.setPadding(dp(28),dp(40),dp(28),dp(36));root.setBackgroundColor(Color.rgb(7,8,10));
  TextView brand=new TextView(this);brand.setText("வைகறைப் பாணி");brand.setTextColor(Color.rgb(198,201,206));brand.setTextSize(14);brand.setLetterSpacing(.16f);brand.setGravity(Gravity.CENTER);root.addView(brand,new LinearLayout.LayoutParams(-1,-2));
  TextView status=new TextView(this);status.setText("ALARM IS RINGING");status.setTextColor(Color.rgb(92,190,139));status.setTextSize(12);status.setTypeface(Typeface.DEFAULT_BOLD);status.setLetterSpacing(.12f);status.setGravity(Gravity.CENTER);LinearLayout.LayoutParams statusParams=new LinearLayout.LayoutParams(-1,-2);statusParams.topMargin=dp(34);root.addView(status,statusParams);
  TextView time=new TextView(this);time.setText(new java.text.SimpleDateFormat("hh:mm",java.util.Locale.getDefault()).format(new java.util.Date()));time.setTextColor(Color.WHITE);time.setTextSize(68);time.setTypeface(Typeface.create("sans-serif-light",Typeface.NORMAL));time.setGravity(Gravity.CENTER);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(-1,-2);tp.topMargin=dp(8);root.addView(time,tp);
  TextView title=new TextView(this);title.setText(label);title.setTextColor(Color.WHITE);title.setTextSize(22);title.setTypeface(Typeface.DEFAULT_BOLD);title.setGravity(Gravity.CENTER);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.topMargin=dp(14);root.addView(title,lp);
  if(!track.isEmpty()){TextView t=new TextView(this);t.setText("♫  "+track);t.setTextColor(Color.LTGRAY);t.setTextSize(13);t.setGravity(Gravity.CENTER);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(10);root.addView(t,p);}
  TextView instruction=new TextView(this);instruction.setText("Choose what to do");instruction.setTextColor(Color.rgb(155,160,168));instruction.setTextSize(13);instruction.setGravity(Gravity.CENTER);LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(-1,-2);ip.topMargin=dp(42);root.addView(instruction,ip);
  Button snooze=actionButton("SNOOZE  ·  5 MINUTES",Color.rgb(17,25,22),Color.rgb(65,145,105),()->snooze());snooze.setTextColor(Color.rgb(132,218,170));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,dp(66));sp.topMargin=dp(16);root.addView(snooze,sp);
  Button stop=actionButton("STOP ALARM",Color.rgb(18,19,21),Color.rgb(112,101,72),()->dismiss());stop.setTextColor(Color.rgb(211,195,151));LinearLayout.LayoutParams stopParams=new LinearLayout.LayoutParams(-1,dp(66));stopParams.topMargin=dp(14);root.addView(stop,stopParams);
  setContentView(root);
 }
 private void snooze(){try{AlarmScheduler.scheduleSnooze(this,id,label,track,artist,audio,art);}catch(Exception ignored){}dismiss();}
 private void dismiss(){stopService(new Intent(this,AlarmSoundService.class));((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(AlarmReceiver.NOTIFICATION_ID);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(7402);finish();}
}
