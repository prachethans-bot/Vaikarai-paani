import fs from 'node:fs';
import path from 'node:path';
const root=process.cwd();
const androidRoot=path.join(root,'android');
const appRoot=path.join(androidRoot,'app');
const javaRoot=path.join(appRoot,'src/main/java/com/vaigarai/paani');
const nativeRoot=path.join(root,'native/android');
if(!fs.existsSync(path.join(androidRoot,'settings.gradle')))throw new Error('Capacitor Android project must be generated first.');
fs.mkdirSync(javaRoot,{recursive:true});
for(const file of fs.readdirSync(nativeRoot).filter((name)=>name.endsWith('.java')))fs.copyFileSync(path.join(nativeRoot,file),path.join(javaRoot,file));
const manifestPath=path.join(appRoot,'src/main/AndroidManifest.xml');
let manifest=fs.readFileSync(manifestPath,'utf8');
const permissions=['android.permission.SCHEDULE_EXACT_ALARM','android.permission.POST_NOTIFICATIONS','android.permission.USE_FULL_SCREEN_INTENT','android.permission.RECEIVE_BOOT_COMPLETED','android.permission.VIBRATE','android.permission.WAKE_LOCK','android.permission.FOREGROUND_SERVICE','android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK'];
for(const permission of permissions){const line='<uses-permission android:name="'+permission+'" />';if(!manifest.includes(line))manifest=manifest.replace('</manifest>','  '+line+'\n</manifest>');}
const components=[
'        <receiver android:name=".AlarmReceiver" android:enabled="true" android:exported="false" />',
'        <receiver android:name=".BootReceiver" android:enabled="true" android:exported="true"><intent-filter><action android:name="android.intent.action.BOOT_COMPLETED" /><action android:name="android.intent.action.MY_PACKAGE_REPLACED" /><action android:name="android.intent.action.TIME_SET" /><action android:name="android.intent.action.TIMEZONE_CHANGED" /></intent-filter></receiver>',
'        <service android:name=".AlarmSoundService" android:enabled="true" android:exported="false" android:foregroundServiceType="mediaPlayback" />',
'        <activity android:name=".AlarmRingActivity" android:exported="false" android:launchMode="singleTop" android:showWhenLocked="true" android:turnScreenOn="true" android:excludeFromRecents="true" android:theme="@android:style/Theme.Material.NoActionBar" />'
].join('\n');
if(!manifest.includes('AlarmRingActivity'))manifest=manifest.replace('</application>',components+'\n    </application>');
const deepLink='            <intent-filter><action android:name="android.intent.action.VIEW" /><category android:name="android.intent.category.DEFAULT" /><category android:name="android.intent.category.BROWSABLE" /><data android:scheme="vaigaraipaani" android:host="spotify-callback" /></intent-filter>';
if(!manifest.includes('vaigaraipaani'))manifest=manifest.replace('</activity>',deepLink+'\n        </activity>');
fs.writeFileSync(manifestPath,manifest);
const mainPath=path.join(javaRoot,'MainActivity.java');
if(!fs.existsSync(mainPath))throw new Error('Capacitor MainActivity.java not found.');
fs.writeFileSync(mainPath,'package com.vaigarai.paani;\nimport android.os.Bundle;\nimport com.getcapacitor.BridgeActivity;\npublic class MainActivity extends BridgeActivity { @Override public void onCreate(Bundle savedInstanceState){ registerPlugin(AlarmBridgePlugin.class); super.onCreate(savedInstanceState); } }\n');
const gradlePath=path.join(appRoot,'build.gradle');
let gradle=fs.readFileSync(gradlePath,'utf8');
if(!gradle.includes('spotify-app-remote-release-0.8.0.aar'))gradle=gradle.replace(/dependencies\s*\{/,"dependencies {\n    implementation files('libs/spotify-app-remote-release-0.8.0.aar')\n    implementation 'com.google.code.gson:gson:2.10.1'");
fs.writeFileSync(gradlePath,gradle);
const logo=path.join(root,'public/logo.jpg');
if(!fs.existsSync(logo))throw new Error('Permanent logo public/logo.jpg is missing.');
for(const density of ['mdpi','hdpi','xhdpi','xxhdpi','xxxhdpi']){
 const folder=path.join(appRoot,'src/main/res/mipmap-'+density);fs.mkdirSync(folder,{recursive:true});
 fs.copyFileSync(logo,path.join(folder,'ic_launcher.jpg'));
 fs.copyFileSync(logo,path.join(folder,'ic_launcher_round.jpg'));
}
console.log('Prepared native alarm, Spotify App Remote and logo assets.');
