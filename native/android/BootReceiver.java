package com.vaigarai.paani;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
public class BootReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context c,Intent i){if(i==null)return;String a=i.getAction();if(Intent.ACTION_BOOT_COMPLETED.equals(a)||Intent.ACTION_MY_PACKAGE_REPLACED.equals(a)||"android.intent.action.TIME_SET".equals(a)||"android.intent.action.TIMEZONE_CHANGED".equals(a))AlarmScheduler.rescheduleAll(c);}
}
