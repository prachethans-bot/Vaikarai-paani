package com.vaigarai.paani;
import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionCallback;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import org.json.JSONArray;
import org.json.JSONObject;
@CapacitorPlugin(name="AlarmBridge",permissions={@Permission(alias="notifications",strings={Manifest.permission.POST_NOTIFICATIONS})})
public class AlarmBridgePlugin extends Plugin {
 @PluginMethod public void checkExactAlarmPermission(PluginCall call){call.resolve(new JSObject().put("allowed",AlarmScheduler.canScheduleExact(getContext())));}
 @PluginMethod public void requestExactAlarmAccess(PluginCall call){boolean allowed=AlarmScheduler.canScheduleExact(getContext());if(!allowed&&Build.VERSION.SDK_INT>=31){Intent i=new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getContext().getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getContext().startActivity(i);}call.resolve(new JSObject().put("opened",!allowed));}
 @PluginMethod public void requestNotificationPermission(PluginCall call){if(Build.VERSION.SDK_INT<33||getPermissionState("notifications")==PermissionState.GRANTED)call.resolve(new JSObject().put("granted",true));else requestPermissionForAlias("notifications",call,"notificationPermissionCallback");}
 @PermissionCallback private void notificationPermissionCallback(PluginCall call){call.resolve(new JSObject().put("granted",getPermissionState("notifications")==PermissionState.GRANTED));}
 @PluginMethod public void requestFullScreenPermission(PluginCall call){boolean allowed=true;if(Build.VERSION.SDK_INT>=34){allowed=Settings.canUseFullScreenIntent(getContext());if(!allowed){Intent i=new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getContext().getPackageName()));i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);getContext().startActivity(i);}}call.resolve(new JSObject().put("allowed",allowed).put("opened",!allowed));}
 @PluginMethod public void scheduleAlarm(PluginCall call){try{AlarmScheduler.saveAndSchedule(getContext(),buildRecord(call,"alarm"));call.resolve(new JSObject().put("scheduled",true));}catch(Exception e){call.reject(e.getMessage()==null?"Could not schedule alarm.":e.getMessage(),e);}}
 @PluginMethod public void scheduleTimer(PluginCall call){try{JSONObject r=new JSONObject();r.put("id",call.getString("id","vaigarai-countdown"));r.put("at",call.getLong("at"));r.put("time","");r.put("label",call.getString("label","Timer complete"));r.put("type","timer");r.put("days",new JSONArray());r.put("trackUri","");r.put("trackName","");AlarmScheduler.saveAndSchedule(getContext(),r);call.resolve(new JSObject().put("scheduled",true));}catch(Exception e){call.reject(e.getMessage()==null?"Could not schedule timer.":e.getMessage(),e);}}
 @PluginMethod public void cancelAlarm(PluginCall call){AlarmScheduler.cancel(getContext(),call.getString("id",""));call.resolve(new JSObject().put("cancelled",true));}
 @PluginMethod public void cancelTimer(PluginCall call){AlarmScheduler.cancel(getContext(),call.getString("id","vaigarai-countdown"));call.resolve(new JSObject().put("cancelled",true));}
 @PluginMethod public void connectSpotify(PluginCall call){String clientId="ba853c15adbb4d8da15680aef114a163";String redirect="vaigaraipaani://spotify-callback";ConnectionParams p=new ConnectionParams.Builder(clientId).setRedirectUri(redirect).showAuthView(true).build();SpotifyAppRemote.connect(getContext(),p,new Connector.ConnectionListener(){@Override public void onConnected(SpotifyAppRemote remote){SpotifyAppRemote.disconnect(remote);call.resolve(new JSObject().put("connected",true));}@Override public void onFailure(Throwable e){call.reject("Spotify could not connect. Make sure Spotify is installed and signed in.",e);}});}
 @PluginMethod public void stopRinging(PluginCall call){getContext().stopService(new Intent(getContext(),AlarmSoundService.class));((android.app.NotificationManager)getContext().getSystemService(Context.NOTIFICATION_SERVICE)).cancel(AlarmReceiver.NOTIFICATION_ID);call.resolve(new JSObject().put("stopped",true));}
 private JSONObject buildRecord(PluginCall call,String type)throws Exception{JSONObject r=new JSONObject();r.put("id",call.getString("id"));r.put("at",call.getLong("at"));r.put("time",call.getString("time",""));r.put("label",call.getString("label","Alarm"));r.put("type",type);r.put("trackUri",call.getString("trackUri",""));r.put("trackName",call.getString("trackName",""));JSONArray days=new JSONArray();JSArray supplied=call.getArray("days");if(supplied!=null)for(int i=0;i<supplied.length();i++)days.put(supplied.getInt(i));r.put("days",days);return r;}
}
