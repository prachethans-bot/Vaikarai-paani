export type ITunesTrack = {
  trackId: number;
  trackName: string;
  artistName: string;
  previewUrl: string;
  artworkUrl100: string;
  trackTimeMillis: number;
  primaryGenreName: string;
  country: 'IND' | 'USA';
};

type ITunesResponse = { resultCount: number; results: Array<Partial<ITunesTrack>> };

const endpoint = 'https://itunes.apple.com/search';
function normalize(item: Partial<ITunesTrack>, country: 'IND' | 'USA'): ITunesTrack | null {
  if (!item.trackId || !item.trackName || !item.artistName || !item.previewUrl || !item.artworkUrl100) return null;
  return {
    trackId: item.trackId, trackName: item.trackName, artistName: item.artistName,
    previewUrl: item.previewUrl, artworkUrl100: item.artworkUrl100,
    trackTimeMillis: item.trackTimeMillis || 0, primaryGenreName: item.primaryGenreName || 'Music', country
  };
}
function originalVersionScore(track: ITunesTrack): number {
  const text = (track.trackName + ' ' + track.artistName).toLowerCase();
  let score = 0;
  if (/lullaby|rockabye baby|marimba|music box|karaoke|instrumental|cover version|tribute/.test(text)) score -= 30;
  if (/original|from the original motion picture|official soundtrack/.test(text)) score += 3;
  if (track.artistName.toLowerCase().includes('rockabye baby')) score -= 50;
  return score;
}
export async function searchITunesSongs(query: string): Promise<ITunesTrack[]> {
  const term = query.trim();
  if (!term) return [];
  const makeUrl = (country: 'IN' | 'US') => {
    return endpoint + '?term=' + encodeURIComponent(term) + '&entity=song&country=' + country + '&limit=15';
  };
  const [india, usa] = await Promise.allSettled([
    fetch(makeUrl('IN')).then(async response => { if (!response.ok) throw new Error('India search failed'); return response.json() as Promise<ITunesResponse>; }),
    fetch(makeUrl('US')).then(async response => { if (!response.ok) throw new Error('US search failed'); return response.json() as Promise<ITunesResponse>; })
  ]);
  if (india.status === 'rejected' && usa.status === 'rejected') throw new Error("Couldn't search songs. Check your internet connection and try again.");
  const merged = new Map<number, ITunesTrack>();
  if (india.status === 'fulfilled') for (const raw of india.value.results || []) { const track = normalize(raw, 'IND'); if (track) merged.set(track.trackId, track); }
  if (usa.status === 'fulfilled') for (const raw of usa.value.results || []) { const track = normalize(raw, 'USA'); if (track && !merged.has(track.trackId)) merged.set(track.trackId, track); }
  return [...merged.values()].sort((a,b) => {
    if (a.country !== b.country) return a.country === 'IND' ? -1 : 1;
    const queryText = term.toLowerCase();
    const aExact = a.trackName.toLowerCase().includes(queryText) ? 2 : 0;
    const bExact = b.trackName.toLowerCase().includes(queryText) ? 2 : 0;
    return (bExact + originalVersionScore(b)) - (aExact + originalVersionScore(a));
  });
}
export function artwork600(url: string): string { return url.replace('100x100', '600x600'); }
export function formatTrackDuration(ms: number): string {
  const seconds = Math.floor(ms / 1000);
  return Math.floor(seconds / 60) + ':' + String(seconds % 60).padStart(2, '0');
}
