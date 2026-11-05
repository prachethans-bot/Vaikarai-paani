import {Browser} from '@capacitor/browser';
import {Capacitor} from '@capacitor/core';

const CLIENT_ID='ba853c15adbb4d8da15680aef114a163';
export const SPOTIFY_REDIRECT='vaigaraipaani://spotify-callback';
const TOKEN_KEY='vp_spotify_tokens_v2';
const VERIFIER_KEY='vp_spotify_verifier';
const STATE_KEY='vp_spotify_state';

export type SpotifyTrack={uri:string;name:string;artist:string;album:string;image?:string;duration_ms:number};
export type SpotifyPlaylist={id:string;name:string;image?:string;total:number;owner:string};
type TokenSet={access_token:string;refresh_token?:string;expires_at:number};
type RawTrack={uri?:string;name?:string;artists?:Array<{name:string}>;album?:{name?:string;images?:Array<{url:string}>};duration_ms?:number;type?:string};
type Page<T>={items?:T[];next?:string|null;total?:number};

function randomString(length:number):string {
 const bytes=new Uint8Array(length); crypto.getRandomValues(bytes);
 const alphabet='ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
 return Array.from(bytes,(b)=>alphabet[b%alphabet.length]).join('');
}
async function challengeFor(verifier:string):Promise<string>{
 const digest=await crypto.subtle.digest('SHA-256',new TextEncoder().encode(verifier));
 const bytes=new Uint8Array(digest);let binary='';bytes.forEach((b)=>{binary+=String.fromCharCode(b);});
 return btoa(binary).replaceAll('+','-').replaceAll('/','_').replace(/=+$/,'');
}
function readTokens():TokenSet|null {try{const raw=localStorage.getItem(TOKEN_KEY);return raw?JSON.parse(raw) as TokenSet:null;}catch{return null;}}
function saveTokens(tokens:TokenSet){localStorage.setItem(TOKEN_KEY,JSON.stringify(tokens));}

export async function startSpotifyLogin():Promise<void>{
 const verifier=randomString(64);const state=randomString(24);
 localStorage.setItem(VERIFIER_KEY,verifier);localStorage.setItem(STATE_KEY,state);
 const challenge=await challengeFor(verifier);
 const url=new URL('https://accounts.spotify.com/authorize');
 url.search=new URLSearchParams({
  client_id:CLIENT_ID,response_type:'code',redirect_uri:SPOTIFY_REDIRECT,
  code_challenge_method:'S256',code_challenge:challenge,state,
  scope:['playlist-read-private','playlist-read-collaborative','user-library-read','user-read-private','app-remote-control'].join(' '),
  show_dialog:'true'
 }).toString();
 if(Capacitor.isNativePlatform())await Browser.open({url:url.toString()});else window.location.assign(url.toString());
}

export async function finishSpotifyRedirect(rawUrl:string):Promise<boolean>{
 let url:URL;try{url=new URL(rawUrl);}catch{return false;}
 const error=url.searchParams.get('error');if(error)throw new Error(error==='access_denied'?'Spotify playlist and library access was not approved. Connect Spotify again and allow access.':'Spotify authorization failed: '+error);
 const code=url.searchParams.get('code');if(!code)return false;
 const state=url.searchParams.get('state');
 if(!state||state!==localStorage.getItem(STATE_KEY))throw new Error('Spotify sign-in could not be verified. Please try again.');
 const verifier=localStorage.getItem(VERIFIER_KEY);if(!verifier)throw new Error('Spotify sign-in expired. Please connect again.');
 const body=new URLSearchParams({grant_type:'authorization_code',code,redirect_uri:SPOTIFY_REDIRECT,client_id:CLIENT_ID,code_verifier:verifier});
 const response=await fetch('https://accounts.spotify.com/api/token',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body});
 const data=await response.json() as {access_token?:string;refresh_token?:string;expires_in?:number;error_description?:string};
 if(!response.ok||!data.access_token)throw new Error(data.error_description||'Spotify sign-in failed.');
 saveTokens({access_token:data.access_token,refresh_token:data.refresh_token,expires_at:Date.now()+(data.expires_in||3600)*1000});
 localStorage.removeItem(VERIFIER_KEY);localStorage.removeItem(STATE_KEY);
 if(Capacitor.isNativePlatform())await Browser.close().catch(()=>undefined);
 return true;
}

async function accessToken():Promise<string>{
 const tokens=readTokens();if(!tokens)throw new Error('Connect Spotify first.');
 if(tokens.expires_at>Date.now()+60000)return tokens.access_token;
 if(!tokens.refresh_token)throw new Error('Spotify session expired. Reconnect Spotify to renew playlist permissions.');
 const response=await fetch('https://accounts.spotify.com/api/token',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body:new URLSearchParams({grant_type:'refresh_token',refresh_token:tokens.refresh_token,client_id:CLIENT_ID})});
 const data=await response.json() as {access_token?:string;refresh_token?:string;expires_in?:number;error_description?:string};
 if(!response.ok||!data.access_token){localStorage.removeItem(TOKEN_KEY);throw new Error(data.error_description||'Could not refresh Spotify session. Reconnect Spotify.');}
 const next={access_token:data.access_token,refresh_token:data.refresh_token||tokens.refresh_token,expires_at:Date.now()+(data.expires_in||3600)*1000};
 saveTokens(next);return next.access_token;
}

async function spotifyGet<T>(path:string):Promise<T>{
 const token=await accessToken();
 const response=await fetch(path.startsWith('https://')?path:'https://api.spotify.com/v1'+path,{headers:{Authorization:'Bearer '+token}});
 const raw=await response.text();let data:Record<string,unknown>={};
 try{data=raw?JSON.parse(raw) as Record<string,unknown>:{};}catch{data={};}
 if(!response.ok){
  if(response.status===401){localStorage.removeItem(TOKEN_KEY);throw new Error('Spotify session expired. Reconnect Spotify and approve the requested permissions.');}
  if(response.status===403){if(path.includes('/playlists/'))throw new Error('Spotify only lets this app read tracks from playlists you own or collaborate on. Try one of your own playlists.');throw new Error('Spotify denied this request (403). Check that this Spotify account is on your app allowlist, approve the requested playlist/library permissions, and make sure the app owner has Spotify Premium in Development Mode.');}
  if(response.status===429)throw new Error('Spotify is rate-limiting requests. Wait a moment and try again.');
  const err=data.error as {message?:string}|undefined;
  throw new Error(err?.message||'Spotify request failed ('+response.status+').');
 }
 return data as T;
}

function toTrack(track:RawTrack|undefined):SpotifyTrack|null{
 if(!track?.uri||track.type&&track.type!=='track')return null;
 return {uri:track.uri,name:track.name||'Unknown track',artist:(track.artists||[]).map(a=>a.name).join(', '),album:track.album?.name||'',image:track.album?.images?.[0]?.url,duration_ms:track.duration_ms||0};
}

export async function getSpotifyPlaylists():Promise<SpotifyPlaylist[]>{
 const page=await spotifyGet<Page<{id:string;name:string;images?:Array<{url:string}>;owner?:{display_name?:string};items?:{total?:number};tracks?:{total?:number}}>>('/me/playlists?limit=50');
 return (page.items||[]).map(p=>({id:p.id,name:p.name,image:p.images?.[0]?.url,total:p.items?.total??p.tracks?.total??0,owner:p.owner?.display_name||'Spotify'}));
}

export async function getSpotifyPlaylistTracks(playlistId:string):Promise<SpotifyTrack[]>{
 const results:SpotifyTrack[]=[];let url:string|null='/playlists/'+encodeURIComponent(playlistId)+'/items?limit=50';
 let pages=0;
 while(url&&pages<20){const page:Page<{item?:RawTrack;track?:RawTrack}>=await spotifyGet<Page<{item?:RawTrack;track?:RawTrack}>>(url);for(const entry of page.items||[]){const track=toTrack(entry.item||entry.track);if(track)results.push(track);}url=page.next||null;pages++;}
 return results;
}

export async function getSpotifySavedTracks():Promise<SpotifyTrack[]>{
 const results:SpotifyTrack[]=[];let url:string|null='/me/tracks?limit=50';let pages=0;
 while(url&&pages<20){const page:Page<{track?:RawTrack}>=await spotifyGet<Page<{track?:RawTrack}>>(url);for(const entry of page.items||[]){const track=toTrack(entry.track);if(track)results.push(track);}url=page.next||null;pages++;}
 return results;
}

export async function searchSpotifyTracks(query:string):Promise<SpotifyTrack[]>{
 const params=new URLSearchParams({q:query,type:'track',limit:'10'});
 const data=await spotifyGet<{tracks?:Page<RawTrack>}>('/search?'+params.toString());
 return (data.tracks?.items||[]).map(toTrack).filter((track):track is SpotifyTrack=>Boolean(track));
}
export function spotifyConnected(){return Boolean(readTokens());}
export function disconnectSpotify(){localStorage.removeItem(TOKEN_KEY);localStorage.removeItem(VERIFIER_KEY);localStorage.removeItem(STATE_KEY);}
