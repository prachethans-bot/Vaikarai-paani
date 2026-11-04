export type Alarm = { id:string; time:string; label:string; days:number[]; enabled:boolean; trackUri?:string; trackName?:string };
export const WEEKDAYS = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
export function nextAlarmAt(time:string,days:number[],now=new Date()):number {
 const parts=time.split(':').map(Number); const hour=parts[0]; const minute=parts[1];
 if(!Number.isInteger(hour)||!Number.isInteger(minute)) throw new Error('Alarm time must be HH:mm');
 for(let offset=0;offset<8;offset+=1){
  const candidate=new Date(now); candidate.setDate(now.getDate()+offset); candidate.setHours(hour,minute,0,0);
  const matches=days.length===0?offset===0:days.includes(candidate.getDay());
  if(matches&&candidate.getTime()>now.getTime()) return candidate.getTime();
 }
 const fallback=new Date(now); fallback.setDate(now.getDate()+1); fallback.setHours(hour,minute,0,0); return fallback.getTime();
}
export function formatDuration(totalSeconds:number):string {
 const safe=Math.max(0,Math.floor(totalSeconds)); const hours=Math.floor(safe/3600); const minutes=Math.floor((safe%3600)/60); const seconds=safe%60;
 return [hours,minutes,seconds].map((part)=>String(part).padStart(2,'0')).join(':');
}
export function formatClock(date:Date):string { return date.toLocaleTimeString('en-IN',{hour:'2-digit',minute:'2-digit',hour12:true}); }
