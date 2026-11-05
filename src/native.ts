import { Capacitor, registerPlugin } from '@capacitor/core';
export type NativeAlarmInput = { id:string; at:number; time:string; label:string; days:number[]; trackId?:number; trackName?:string; trackArtist?:string; previewUrl?:string; artworkUrl?:string; localAudioPath?:string; localArtworkPath?:string; type?:'alarm'|'timer' };
type AlarmBridgePlugin = {
 checkExactAlarmPermission():Promise<{allowed:boolean}>;
 requestExactAlarmAccess():Promise<{opened:boolean}>;
 requestNotificationPermission():Promise<{granted:boolean}>;
 requestFullScreenPermission():Promise<{allowed:boolean;opened:boolean}>;
 requestBatteryOptimizationExemption():Promise<{allowed:boolean;opened:boolean}>;
 downloadTrack(options:{trackId:number;previewUrl:string;artworkUrl:string}):Promise<{localAudioPath:string;localArtworkPath:string}>;
 scheduleAlarm(options:NativeAlarmInput):Promise<{scheduled:boolean}>;
 scheduleTimer(options:{id:string;at:number;label:string}):Promise<{scheduled:boolean}>;
 cancelAlarm(options:{id:string}):Promise<{cancelled:boolean}>;
 cancelTimer(options:{id:string}):Promise<{cancelled:boolean}>;
 stopRinging():Promise<{stopped:boolean}>;
};
export const AlarmBridge = registerPlugin<AlarmBridgePlugin>('AlarmBridge');
export const isNative = Capacitor.isNativePlatform();
