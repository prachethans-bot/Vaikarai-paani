package com.vaigarai.paani;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Map;
final class AlarmStore {
 private static final String PREFS="vaigarai_alarm_records";
 private AlarmStore(){}
 static void put(Context c,JSONObject r){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().putString(r.optString("id"),r.toString()).apply();}
 static JSONObject get(Context c,String id){String raw=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getString(id,null);if(raw==null)return null;try{return new JSONObject(raw);}catch(Exception ignored){return null;}}
 static void remove(Context c,String id){c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).edit().remove(id).apply();}
 static JSONArray all(Context c){JSONArray result=new JSONArray();Map<String,?> values=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE).getAll();for(Object value:values.values()){if(value instanceof String){try{result.put(new JSONObject((String)value));}catch(Exception ignored){}}}return result;}
}
