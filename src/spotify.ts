import {Browser} from '@capacitor/browser';
import {Capacitor} from '@capacitor/core';
const CLIENT_ID='ba853c15adbb4d8da15680aef114a163';
export const SPOTIFY_REDIRECT='vaigaraipaani://spotify-callback';
const TOKEN_KEY='vp_spotify_tokens'; const VERIFIER_KEY='vp_spotify_verifier'; const STATE_KEY='vp_spotify_state';
export type SpotifyTrack={uri:string;name:string;artist:string;album:string;image?:string;duration_ms:number};
type TokenSet={access_token:string;refresh_token?:string;expires_at:number};
function randomString(length:number):string {
 const bytes=new Uint8Array(length); crypto.getRandomValues(bytes);
 const alphabet='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
 return Array.from(bytes,(b)=>alphabet[b%alphabet.length]).join('');
}
async function challengeFor(verifier:string):Promise<string>{
 const digest=await crypto.subtle.digest('SHA-256',new TextEncoder().encode(verifier)); const bytes=new Uint8Array(digest);
 let binary=''; bytes.forEach((b)=>{binary+=String.fromCharCode(b);});
 return btoa(binary).replace(/\+/g,'-').replace(/\//g,'_').replace(/=+$/,'');
}
function readTokens():TokenSet|null {try{const raw=localStorage.getItem(TOKEN_KEY);return raw?JSON.parse(raw) as TokenSet:null;}catch{return null;}}
function saveTokens(tokens:TokenSet){localStorage.setItem(TOKEN_KEY,JSON.stringify(tokens));}
export async function startSpotifyLogin():Promise<void>{
 const verifier=randomString(64); const state=randomString(24); localStorage.setItem(VERIFIER_KEY,verifier); localStorage.setItem(STATE_KEY,state);
 const challenge=await challengeFor(verifier); const url=new URL('https://accounts.spotify.com/authorize');
 url.search=new URLSearchParams({client_id:CLIENT_ID,response_type:'code',redirect_uri:SPOTIFY_REDIRECT,code_challenge_method:'S256',code_challenge:challenge,state}).toString();
 if(Capacitor.isNativePlatform()) await Browser.open({url:url.toString()}); else window.location.assign(url.toString());
}
export async function finishSpotifyRedirect(rawUrl:string):Promise<boolean>{
 let url:URL;try{url=new URL(rawUrl);}catch{return false;}
 const code=url.searchParams.get('code'); if(!code)return false;
 const state=url.searchParams.get('state'); if(!state||state!==localStorage.getItem(STATE_KEY))throw new Error('Spotify sign-in could not be verified. Please try again.');
 const verifier=localStorage.getItem(VERIFIER_KEY); if(!verifier)throw new Error('Spotify sign-in expired. Please connect again.');
 const body=new URLSearchParams({grant_type:'authorization_code',code,redirect_uri:SPOTIFY_REDIRECT,client_id:CLIENT_ID,code_verifier:verifier});
 const response=await fetch('https://accounts.spotify.com/api/token',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});
 const data=await response.json() as {access_token?:string;refresh_token?:string;expires_in?:number;error_description?:string};
 if(!response.ok||!data.access_token)throw new Error(data.error_description||'Spotify sign-in failed.');
 saveTokens({access_token:data.access_token,refresh_token:data.refresh_token,expires_at:Date.now()+(data.expires_in||3600)*1000});
 localStorage.removeItem(VERIFIER_KEY);localStorage.removeItem(STATE_KEY);if(Capacitor.isNativePlatform())await Browser.close().catch(()=>undefined);return true;
}
async function accessToken():Promise<string>{
 const tokens=readTokens();if(!tokens)throw new Error('Connect Spotify first.');if(tokens.expires_at>Date.now()+60000)return tokens.access_token;
 if(!tokens.refresh_token)throw new Error('Spotify session expired. Please reconnect.');
 const response=await fetch('https://accounts.spotify.com/api/token',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({grant_type:'refresh_token',refresh_token:tokens.refresh_token,client_id:CLIENT_ID})});
 const data=await response.json() as {access_token?:string;refresh_token?:string;expires_in?:number;error_description?:string};
 if(!response.ok||!data.access_token)throw new Error(data.error_description||'Could not refresh Spotify session.');
 const next={access_token:data.access_token,refresh_token:data.refresh_token||tokens.refresh_token,expires_at:Date.now()+(data.expires_in||3600)*1000};saveTokens(next);return next.access_token;
}
export async function searchSpotifyTracks(query:string):Promise<SpotifyTrack[]>{
 const token=await accessToken();const response=await fetch('https://api.spotify.com/v1/search?'+new URLSearchParams({q:query,type:'track',limit:'12'}),{headers:{Authorization:'Bearer '+token}});
 if(!response.ok){if(response.status===401)localStorage.removeItem(TOKEN_KEY);throw new Error(response.status===401?'Spotify session expired. Reconnect Spotify.':'Spotify search is unavailable right now.');}
 const data=await response.json() as {tracks?:{items?:Array<{uri:string;name:string;artists:Array<{name:string}>;album:{name:string;images?:Array<{url:string}>};duration_ms:number}>}};
 return (data.tracks?.items||[]).map((track)=>({uri:track.uri,name:track.name,artist:track.artists.map((a)=>a.name).join(', '),album:track.album.name,image:track.album.images?.[0]?.url,duration_ms:track.duration_ms}));
}
export function spotifyConnected(){return Boolean(readTokens());}
export function disconnectSpotify(){localStorage.removeItem(TOKEN_KEY);}
