import fs from 'node:fs';
import path from 'node:path';
const root=process.cwd(),androidRoot=path.join(root,'android'),appRoot=path.join(androidRoot,'app');
const javaRoot=path.join(appRoot,'src/main/java/com/vaigarai/paani'),nativeRoot=path.join(root,'native/android');
if(!fs.existsSync(path.join(androidRoot,'settings.gradle')))throw new Error('Generate the Capacitor Android project before running this script.');
fs.mkdirSync(javaRoot,{recursive:true});
for(const file of fs.readdirSync(nativeRoot).filter(name=>name.endsWith('.java')))fs.copyFileSync(path.join(nativeRoot,file),path.join(javaRoot,file));
const manifestPath=path.join(appRoot,'src/main/AndroidManifest.xml');let manifest=fs.readFileSync(manifestPath,'utf8');
const permissions=['android.permission.SCHEDULE_EXACT_ALARM','android.permission.POST_NOTIFICATIONS','android.permission.USE_FULL_SCREEN_INTENT','android.permission.RECEIVE_BOOT_COMPLETED','android.permission.VIBRATE','android.permission.WAKE_LOCK','android.permission.FOREGROUND_SERVICE','android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK','android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS','android.permission.INTERNET'];
for(const item of permissions){const row='<uses-permission android:name="'+item+'" />';if(!manifest.includes(row))manifest=manifest.replace('</manifest>','  '+row+'\n</manifest>');}
const components=['<receiver android:name=".AlarmReceiver" android:enabled="true" android:exported="false" />','<receiver android:name=".BootReceiver" android:enabled="true" android:exported="true"><intent-filter><action android:name="android.intent.action.BOOT_COMPLETED"/><action android:name="android.intent.action.MY_PACKAGE_REPLACED"/><action android:name="android.intent.action.TIME_SET"/><action android:name="android.intent.action.TIMEZONE_CHANGED"/></intent-filter></receiver>','<service android:name=".AlarmSoundService" android:enabled="true" android:exported="false" android:foregroundServiceType="mediaPlayback"/>','<activity android:name=".AlarmRingActivity" android:exported="false" android:launchMode="singleTop" android:showWhenLocked="true" android:turnScreenOn="true" android:excludeFromRecents="true" android:theme="@android:style/Theme.Material.NoActionBar"/>'].join('\n        ');
if(!manifest.includes('AlarmRingActivity'))manifest=manifest.replace('</application>',components+'\n    </application>');
manifest=manifest.replace('android:icon="@mipmap/ic_launcher"','android:icon="@mipmap/ic_launcher_logo"').replace('android:roundIcon="@mipmap/ic_launcher_round"','android:roundIcon="@mipmap/ic_launcher_logo"');
fs.writeFileSync(manifestPath,manifest);
const mainPath=path.join(javaRoot,'MainActivity.java');if(!fs.existsSync(mainPath))throw new Error('Capacitor MainActivity.java was not found.');
fs.writeFileSync(mainPath,'package com.vaigarai.paani;\nimport android.os.Bundle;\nimport com.getcapacitor.BridgeActivity;\npublic class MainActivity extends BridgeActivity { @Override public void onCreate(Bundle savedInstanceState){ registerPlugin(AlarmBridgePlugin.class); super.onCreate(savedInstanceState); } }\n');
const logo=path.join(root,'public/logo.jpg');if(!fs.existsSync(logo))throw new Error('Permanent logo public/logo.jpg is missing.');
for(const density of ['mdpi','hdpi','xhdpi','xxhdpi','xxxhdpi']){const dir=path.join(appRoot,'src/main/res/mipmap-'+density);fs.mkdirSync(dir,{recursive:true});fs.copyFileSync(logo,path.join(dir,'ic_launcher_logo.jpg'));}
const rawDir=path.join(appRoot,'src/main/res/raw');fs.mkdirSync(rawDir,{recursive:true});
const wav=path.join(rawDir,'default_alarm.wav');
if(!fs.existsSync(wav)){
 const rate=22050,duration=4,samples=rate*duration,pcm=Buffer.alloc(samples*2),freqs=[523.25,659.25,783.99,659.25];
 for(let i=0;i<samples;i++){const t=i/rate,phase=(t%1),f=freqs[Math.floor(t)%4],env=Math.min(1,phase*8)*Math.min(1,(1-phase)*8);const v=Math.sin(2*Math.PI*f*t)*0.24*env;pcm.writeInt16LE(Math.round(v*32767),i*2);}
 const header=Buffer.alloc(44);header.write('RIFF',0);header.writeUInt32LE(36+pcm.length,4);header.write('WAVEfmt ',8);header.writeUInt32LE(16,16);header.writeUInt16LE(1,20);header.writeUInt16LE(1,22);header.writeUInt32LE(rate,24);header.writeUInt32LE(rate*2,28);header.writeUInt16LE(2,32);header.writeUInt16LE(16,34);header.write('data',36);header.writeUInt32LE(pcm.length,40);fs.writeFileSync(wav,Buffer.concat([header,pcm]));
}
console.log('Prepared iTunes offline alarm runtime, default alarm tone and launcher assets.');
