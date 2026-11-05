package com.vaigarai.paani;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
@CapacitorPlugin(name="AlarmBridge",permissions={@Permission(alias="notifications",strings={Manifest.permission.POST_NOTIFICATIONS})})
public class AlarmBridgePlugin extends Plugin {
 private final ExecutorService io=Executors.newSingleThreadExecutor();
 @PluginMethod public void checkExactAlarmPermission(PluginCall call){call.resolve(new JSObject().put("allowed",AlarmScheduler.canScheduleExact(getContext())));}
 @PluginMethod public void requestExactAlarmAccess(PluginCall call){boolean allowed=AlarmScheduler.canScheduleExact(getContext());if(!allowed&&Build.VERSION.SDK_INT>=31){Intent i=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getContext().getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getContext().startActivity(i);}call.resolve(new JSObject().put("opened",!allowed));}
 @PluginMethod public void requestNotificationPermission(PluginCall call){if(Build.VERSION.SDK_INT<33||getPermissionState("notifications")==PermissionState.GRANTED)call.resolve(new JSObject().put("granted",true));else requestPermissionForAlias("notifications",call,"notificationPermissionCallback");}
 @PermissionCallback private void notificationPermissionCallback(PluginCall call){call.resolve(new JSObject().put("granted",getPermissionState("notifications")==PermissionState.GRANTED));}
 @PluginMethod public void requestFullScreenPermission(PluginCall call){boolean allowed=true;if(Build.VERSION.SDK_INT>=34){android.app.NotificationManager manager=(android.app.NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE);allowed=manager.canUseFullScreenIntent();if(!allowed){Intent i=new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getContext().getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getContext().startActivity(i);}}call.resolve(new JSObject().put("allowed",allowed).put("opened",!allowed));}
 @PluginMethod public void requestBatteryOptimizationExemption(PluginCall call){android.os.PowerManager pm=(android.os.PowerManager)getContext().getSystemService(Context.POWER_SERVICE);boolean allowed=Build.VERSION.SDK_INT<23||pm.isIgnoringBatteryOptimizations(getContext().getPackageName());if(!allowed){Intent i=new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,Uri.parse("package:"+getContext().getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);try{getContext().startActivity(i);}catch(Exception e){i=new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getContext().startActivity(i);}}call.resolve(new JSObject().put("allowed",allowed).put("opened",!allowed));}
 @PluginMethod public void downloadTrack(PluginCall call){int id=call.getInt("trackId",0);String audio=call.getString("previewUrl",""),art=call.getString("artworkUrl","");if(id<=0||!audio.startsWith("https://audio-ssl.itunes.apple.com/")){call.reject("The selected iTunes preview URL is invalid.");return;}io.execute(()->{try{File dir=new File(getContext().getFilesDir(),"music");if(!dir.exists()&&!dir.mkdirs())throw new Exception("Could not create offline music storage.");File audioFile=new File(dir,"track-"+id+".m4a");download(audio,audioFile);File artFile=new File(dir,"artwork-"+id+".jpg");if(art.startsWith("https://")&&!artFile.exists())download(art,artFile);JSObject result=new JSObject().put("localAudioPath",audioFile.getAbsolutePath()).put("localArtworkPath",artFile.exists()?artFile.getAbsolutePath():"");call.resolve(result);}catch(Exception e){call.reject("Could not save this preview for offline playback.",e);}});}
 private void download(String source,File target)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(source).openConnection();c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setInstanceFollowRedirects(true);try{if(c.getResponseCode()<200||c.getResponseCode()>=300)throw new Exception("Download failed with HTTP "+c.getResponseCode());File tmp=new File(target.getParentFile(),target.getName()+".tmp");try(InputStream in=c.getInputStream();FileOutputStream out=new FileOutputStream(tmp)){byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}if(!tmp.renameTo(target)){tmp.delete();throw new Exception("Could not store downloaded media.");}}finally{c.disconnect();}}
 @PluginMethod public void scheduleAlarm(PluginCall call){try{AlarmScheduler.saveAndSchedule(getContext(),buildRecord(call,"alarm"));call.resolve(new JSObject().put("scheduled",true));}catch(Exception e){call.reject(e.getMessage()==null?"Could not schedule alarm.":e.getMessage(),e);}}
 @PluginMethod public void scheduleTimer(PluginCall call){try{JSONObject r=new JSONObject();r.put("id",call.getString("id","vaigarai-countdown"));r.put("at",call.getLong("at"));r.put("time","");r.put("label",call.getString("label","Timer complete"));r.put("type","timer");r.put("days",new JSONArray());AlarmScheduler.saveAndSchedule(getContext(),r);call.resolve(new JSObject().put("scheduled",true));}catch(Exception e){call.reject(e.getMessage()==null?"Could not schedule timer.":e.getMessage(),e);}}
 @PluginMethod public void cancelAlarm(PluginCall call){AlarmScheduler.cancel(getContext(),call.getString("id",""));call.resolve(new JSObject().put("cancelled",true));}
 @PluginMethod public void cancelTimer(PluginCall call){AlarmScheduler.cancel(getContext(),call.getString("id","vaigarai-countdown"));call.resolve(new JSObject().put("cancelled",true));}
 @PluginMethod public void stopRinging(PluginCall call){getContext().stopService(new Intent(getContext(),AlarmSoundService.class));((android.app.NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(AlarmReceiver.NOTIFICATION_ID);call.resolve(new JSObject().put("stopped",true));}
 private JSONObject buildRecord(PluginCall call,String type)throws Exception{JSONObject r=new JSONObject();r.put("id",call.getString("id"));r.put("at",call.getLong("at"));r.put("time",call.getString("time",""));r.put("label",call.getString("label","Alarm"));r.put("type",type);r.put("trackId",call.getInt("trackId",0));r.put("trackName",call.getString("trackName",""));r.put("trackArtist",call.getString("trackArtist",""));r.put("previewUrl",call.getString("previewUrl",""));r.put("artworkUrl",call.getString("artworkUrl",""));r.put("localAudioPath",call.getString("localAudioPath",""));r.put("localArtworkPath",call.getString("localArtworkPath",""));JSONArray days=new JSONArray();JSArray supplied=call.getArray("days");if(supplied!=null)for(int i=0;i<supplied.length();i++)days.put(supplied.getInt(i));r.put("days",days);return r;}
}
