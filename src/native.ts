import { Capacitor, registerPlugin } from '@capacitor/core';
export type NativeAlarmInput = { id:string; at:number; time:string; label:string; days:number[]; trackUri?:string; trackName?:string; type?:'alarm'|'timer' };
type AlarmBridgePlugin = {
 checkExactAlarmPermission():Promise<{allowed:boolean}>;
 requestExactAlarmAccess():Promise<{opened:boolean}>;
 requestNotificationPermission():Promise<{granted:boolean}>;
 requestFullScreenPermission():Promise<{allowed:boolean;opened:boolean}>;
 scheduleAlarm(options:NativeAlarmInput):Promise<{scheduled:boolean}>;
 scheduleTimer(options:{id:string;at:number;label:string}):Promise<{scheduled:boolean}>;
 cancelAlarm(options:{id:string}):Promise<{cancelled:boolean}>;
 cancelTimer(options:{id:string}):Promise<{cancelled:boolean}>;
 connectSpotify():Promise<{connected:boolean}>;
 stopRinging():Promise<{stopped:boolean}>;
};
export const AlarmBridge = registerPlugin<AlarmBridgePlugin>('AlarmBridge');
export const isNative = Capacitor.isNativePlatform();
