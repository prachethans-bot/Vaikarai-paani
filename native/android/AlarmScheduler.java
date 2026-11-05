package com.vaigarai.paani;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Calendar;
final class AlarmScheduler {
 private AlarmScheduler(){}
 static boolean canScheduleExact(Context c){AlarmManager m=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);return Build.VERSION.SDK_INT<Build.VERSION_CODES.S||m.canScheduleExactAlarms();}
 static void saveAndSchedule(Context c,JSONObject r)throws Exception{if(!canScheduleExact(c))throw new SecurityException("Allow Alarms & reminders before saving an exact alarm.");AlarmStore.put(c,r);arm(c,r);}
 static void arm(Context c,JSONObject r){
  long at=r.optLong("at",0L);if(at<=System.currentTimeMillis())return;AlarmManager m=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
  Intent i=new Intent(c,AlarmReceiver.class);i.setAction("com.vaigarai.paani.ALARM");i.putExtra("id",r.optString("id"));
  PendingIntent pi=PendingIntent.getBroadcast(c,requestCode(r.optString("id")),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Intent show=new Intent(c,MainActivity.class);PendingIntent showPi=PendingIntent.getActivity(c,requestCode(r.optString("id"))+50000,show,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  m.setAlarmClock(new AlarmManager.AlarmClockInfo(at,showPi),pi);
 }
 static void cancel(Context c,String id){AlarmManager m=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);Intent i=new Intent(c,AlarmReceiver.class).setAction("com.vaigarai.paani.ALARM");PendingIntent pi=PendingIntent.getBroadcast(c,requestCode(id),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);m.cancel(pi);AlarmStore.remove(c,id);}
 static void scheduleSnooze(Context c,String sourceId,String label,String uri,String trackName)throws Exception{JSONObject r=new JSONObject();String id=sourceId+"-snooze-"+System.currentTimeMillis();r.put("id",id);r.put("at",System.currentTimeMillis()+300000L);r.put("time","");r.put("label",label);r.put("type","alarm");r.put("days",new JSONArray());r.put("trackName",trackName);saveAndSchedule(c,r);}
 static void rescheduleAll(Context c){JSONArray records=AlarmStore.all(c);for(int i=0;i<records.length();i++){JSONObject r=records.optJSONObject(i);if(r==null)continue;long at=r.optLong("at",0L);if(at<=System.currentTimeMillis()){JSONArray days=r.optJSONArray("days");if(days!=null&&days.length()>0&&"alarm".equals(r.optString("type"))){long next=nextRepeat(r,System.currentTimeMillis());if(next>0){try{r.put("at",next);AlarmStore.put(c,r);}catch(Exception ignored){}}}else{AlarmStore.remove(c,r.optString("id"));continue;}}try{arm(c,r);}catch(Exception ignored){}}}
 static JSONObject consumeAndAdvance(Context c,String id){JSONObject r=AlarmStore.get(c,id);if(r==null)return null;JSONArray days=r.optJSONArray("days");if("alarm".equals(r.optString("type"))&&days!=null&&days.length()>0&&!id.contains("-snooze-")){long next=nextRepeat(r,System.currentTimeMillis());if(next>0){try{r.put("at",next);AlarmStore.put(c,r);arm(c,r);}catch(Exception ignored){}}}else AlarmStore.remove(c,id);return r;}
 private static long nextRepeat(JSONObject r,long now){String[] pieces=r.optString("time","").split(":");if(pieces.length!=2)return 0L;int hour,minute;try{hour=Integer.parseInt(pieces[0]);minute=Integer.parseInt(pieces[1]);}catch(NumberFormatException e){return 0L;}JSONArray days=r.optJSONArray("days");if(days==null)return 0L;Calendar c=Calendar.getInstance();for(int offset=1;offset<=8;offset++){c.setTimeInMillis(now);c.add(Calendar.DAY_OF_YEAR,offset);c.set(Calendar.HOUR_OF_DAY,hour);c.set(Calendar.MINUTE,minute);c.set(Calendar.SECOND,0);c.set(Calendar.MILLISECOND,0);int jsDay=c.get(Calendar.DAY_OF_WEEK)-1;boolean matches=false;for(int j=0;j<days.length();j++)if(days.optInt(j,-1)==jsDay)matches=true;if(matches&&c.getTimeInMillis()>now)return c.getTimeInMillis();}return 0L;}
 private static int requestCode(String id){return id.hashCode()&0x7fffffff;}
}
