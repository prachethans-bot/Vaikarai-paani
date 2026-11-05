import {afterEach,describe,expect,it,vi} from 'vitest';
import {searchITunesSongs,artwork600,formatTrackDuration} from './itunes';

const song=(trackId:number,trackName:string,artistName:string,previewUrl='https://audio-ssl.itunes.apple.com/preview.m4a')=>({
  trackId,trackName,artistName,previewUrl,
  artworkUrl100:'https://is1-ssl.mzstatic.com/image/thumb/test/100x100bb.jpg',
  trackTimeMillis:185000,primaryGenreName:'Tamil'
});
afterEach(()=>vi.unstubAllGlobals());

describe('iTunes dual-region search',()=>{
  it('merges India and US, de-duplicates, skips missing previews and keeps India first',async()=>{
    const fetchMock=vi.fn((input:RequestInfo|URL)=>{
      const country=new URL(String(input)).searchParams.get('country');
      const results=country==='IN'
        ? [song(10,'Hukum','Anirudh'),song(11,'Hukum Lullaby','Kids',''),song(12,'Song','Artist','')]
        : [song(10,'Hukum','Anirudh US'),song(20,'Blinding Lights','The Weeknd')];
      return Promise.resolve(new Response(JSON.stringify({resultCount:results.length,results}),{status:200}));
    });
    vi.stubGlobal('fetch',fetchMock);
    const results=await searchITunesSongs('Hukum song');
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(String(fetchMock.mock.calls[0][0])).toContain('term=Hukum%20song');
    expect(results.map(track=>track.trackId)).toEqual([10,20]);
    expect(results[0].country).toBe('IND');
    expect(results[0].artistName).toBe('Anirudh');
    expect(results[1].country).toBe('USA');
  });
  it('keeps successful region when the other request fails',async()=>{
    vi.stubGlobal('fetch',vi.fn((input:RequestInfo|URL)=>{
      if(new URL(String(input)).searchParams.get('country')==='US')return Promise.reject(new Error('offline'));
      return Promise.resolve(new Response(JSON.stringify({resultCount:1,results:[song(1,'Song','Artist')]}),{status:200}));
    }));
    const results=await searchITunesSongs('song');
    expect(results).toHaveLength(1);
    expect(results[0].country).toBe('IND');
  });
  it('reports a useful error when both regions fail',async()=>{
    vi.stubGlobal('fetch',vi.fn().mockRejectedValue(new Error('offline')));
    await expect(searchITunesSongs('song')).rejects.toThrow("Couldn't search songs. Check your internet connection and try again.");
  });
});
describe('music display helpers',()=>{
  it('upscales artwork and formats duration',()=>{
    expect(artwork600('https://example.com/100x100bb.jpg')).toBe('https://example.com/600x600bb.jpg');
    expect(formatTrackDuration(207647)).toBe('3:27');
  });
});
