using System;
using System.Collections.Generic;
using System.Linq;
using System.Net.Http;
using System.Threading.Tasks;
using CloudProject.Models;
using Google.Apis.Services;
using Google.Apis.YouTube.v3;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CloudProject.Controllers
{
    [Route("cloud/[controller]")]
    [ApiController]
    public class SongController : ControllerBase
    {
        private readonly cloudContext _context;

        public SongController(cloudContext context)
        {
            _context = context;
        }

        // GET api/values
        [HttpGet]
        public async Task<IActionResult> Get()
        {
            IList<Song> Songs = await _context.Songs.ToListAsync();

            return Ok(Songs);
        }

        /// <summary>
        /// get song recommendations
        /// </summary>
        /// <param name="userID"></param>
        /// <returns></returns>
        [HttpGet("{userID}")]
        public async Task<IActionResult> RecommendSongs(string userID)
        {
            User user = await _context.Users.Where(u => u.userID == userID).Include(u => u.songs).ThenInclude(l => l.song).SingleOrDefaultAsync();
            List<Song> recommendations = new List<Song>();

            if(user == null)
            {
                return NoContent();
            }

            //list of all songs user has listened to
            List<ListenedTo> songsLT = user.songs.OrderByDescending(l => l.dateListened).ToList();

            //recommendation by genre


            List<string> genres = songsLT.Select(l => l.song.genre).Take(20).ToList();
           
            string maxGenre = (from i in genres
                        group i by i into grp
                        orderby grp.Count() descending
                        select grp.Key).First();

            List<ListenedTo> pots = await _context.ListenedTos.Where(l => l.fk_userID != userID && l.song.genre == maxGenre && l.dateListened > DateTime.Now.AddDays(-30)).Include(l => l.song).ToListAsync();

            List<Song> sgSongs = pots.Select(l => l.song).ToList();

            if(sgSongs.Count > 0)
            {

                Song maxGSong = (from i in sgSongs
                                 group i by i into grp
                                 orderby grp.Count() descending
                                 select grp.Key).First();

                recommendations.Add(maxGSong);
            }

            //recommendation by similar users
            List<string> songNamesLt = songsLT.Select(l => l.fk_songID).Distinct().ToList();

            List<ListenedTo> similarLts = await _context.ListenedTos.Where(l => l.fk_userID != userID && songNamesLt.Contains(l.fk_songID)).ToListAsync();
            List<string> similarUsers = similarLts.Select(l => l.fk_userID).Distinct().ToList();
            List<ListenedTo> similarLts2 = await _context.ListenedTos.Where(l => similarUsers.Contains(l.fk_userID)).Include(l => l.song).ToListAsync();

            List<Song> suSongs = similarLts2.Select(l => l.song).ToList();

            if(suSongs.Count > 0)
            {
                Song maxUSong = (from i in suSongs
                                 group i by i into grp
                                 orderby grp.Count() descending
                                 select grp.Key).First();

                recommendations.Add(maxUSong);
            }

            //recommendation by album

            using (HttpClient client = new HttpClient())
            {
                try
                {
                    Song maxASong = (from i in songsLT
                                     group i by i into grp
                                     orderby grp.Count() descending
                                     select grp.Key).First().song;


                    string APIKEY = "451bb3863ad571f75d87164152f909ba";
                    client.BaseAddress = new Uri("http://api.onemusicapi.com");
                    HttpResponseMessage response = await client.GetAsync($"/20151208/release?user_key={APIKEY}&title={maxASong.songName}&artist={maxASong.artist}");
                    response.EnsureSuccessStatusCode();

                    string stringResult = await response.Content.ReadAsStringAsync();

                    List<Release> rawSong = Newtonsoft.Json.JsonConvert.DeserializeObject<List<Release>>(stringResult);

                    Release release = rawSong.FirstOrDefault();

                    Random rand = new Random();
                    string newTitle = release.media.First().tracks.Where(t => t.title != maxASong.songName).ElementAt(rand.Next(release.media.First().tracks.Count())).title;
                    Song albumSong = await _context.Songs.Where(s => s.artist == maxASong.artist && s.songName == newTitle).SingleOrDefaultAsync();

                    recommendations.Add(albumSong);

                }
                catch (Exception e)
                {
                    var rawSong = "rip";
                }

            }
            List<SongURL> songRecs = new List<SongURL>();

            foreach(Song s in recommendations)
            {
                songRecs.Add(youtube(s).Result);
            }
            
            return Ok(songRecs);
        }

        // POST api/values
        [HttpPost]
        public async Task<IActionResult> Post([FromBody] Song value)
        {
            Song newSong = new Song()
            {
                songID = Guid.NewGuid().ToString(),
                songName = value.songName,
                artist = value.artist,
                genre = value.genre
            };

            await _context.Songs.AddAsync(newSong);
            await _context.SaveChangesAsync();

            return Ok(newSong);
        }

        // PUT api/values/5
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(string id, [FromBody] Song value)
        {
            Song newSong = await _context.Songs.FindAsync(id);

            if (newSong == null)
            {
                return NoContent();
            }
            
            newSong.songName = value.songName;
            newSong.artist = value.artist;
            newSong.genre = value.genre;

            await _context.SaveChangesAsync();

            return Ok(newSong);

        }

        // DELETE api/values/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            Song Song = await _context.Songs.FindAsync(id);

            if (Song == null)
            {
                return BadRequest();
            }

            _context.Songs.Remove(Song);
            await _context.SaveChangesAsync();

            return Ok();
        }


        private async Task<SongURL> youtube(Song song)
        {
            YouTubeService youtubeService = new YouTubeService(new BaseClientService.Initializer()
            {
                ApiKey = "AIzaSyAj8ncAKqK6Stj611qntKmoWUK1Q8lq8mI",
                ApplicationName = "CloudProject"
            });

            var searchListRequest = youtubeService.Search.List("snippet");
            searchListRequest.Q = $"{song.songName} {song.artist}";
            searchListRequest.MaxResults = 1;
            searchListRequest.Type = "video";


            var searchListResponse = await searchListRequest.ExecuteAsync();

            SongURL songurl = new SongURL();
            songurl.song = song;
            songurl.videoID = "https://www.youtube.com/watch?v=" + searchListResponse.Items.First().Id.VideoId ;
            songurl.videoTitle = searchListResponse.Items.First().Snippet.Title;


            return songurl;
        } 
    }
}
