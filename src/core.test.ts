import {describe,expect,it} from 'vitest';
import {formatDuration,nextAlarmAt} from './core';
describe('alarm time calculations',()=>{
 it('selects a future same-day occurrence',()=>{const now=new Date(2026,10,5,6,0);const next=new Date(nextAlarmAt('07:15',[],now));expect(next.getHours()).toBe(7);expect(next.getMinutes()).toBe(15);});
 it('moves a passed one-time alarm to tomorrow',()=>{const now=new Date(2026,10,5,8,0);const next=new Date(nextAlarmAt('07:15',[],now));expect(next.getDate()).toBe(6);});
 it('selects the next matching weekday',()=>{const now=new Date(2026,10,5,8,0);const next=new Date(nextAlarmAt('07:00',[1,3,5],now));expect(next.getDay()).toBe(5);});
});
describe('duration formatting',()=>{
 it('formats hours minutes and seconds',()=>expect(formatDuration(3661)).toBe('01:01:01'));
 it('clamps negative durations',()=>expect(formatDuration(-3)).toBe('00:00:00'));
});
