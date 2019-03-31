using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using CloudProject.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Net.Http;
using Newtonsoft.Json;

namespace CloudProject.Controllers
{
    [Route("cloud/[controller]")]
    [ApiController]
    public class ListenedToController : ControllerBase
    {
        private readonly cloudContext _context;

        public ListenedToController(cloudContext context)
        {
            _context = context;
        }

        // GET api/values
        [HttpGet]
        public async Task<IActionResult> Get()
        {
            IList<ListenedTo> ListenedTos = await _context.ListenedTos.ToListAsync();

            return Ok(ListenedTos);
        }

        // GET api/values/5
        [HttpGet("{id}")]
        public async Task<IActionResult> Get(string id)
        {
            ListenedTo ListenedTo = await _context.ListenedTos.Where(u => u.listenedToID == id).SingleOrDefaultAsync();
            return Ok(ListenedTo);
        }

        /// <summary>
        /// Insert a new ListenedTo Object for a user & song/artist combination
        /// </summary>
        /// <param name="userID"></param>
        /// <param name="songName"></param>
        /// <param name="artistName"></param>
        /// <returns></returns>
        [HttpPost]
        public async Task<IActionResult> Post(string userID, string songName, string artistName)
        {
            Song ltSong = await _context.Songs.Where(s => s.songName == songName && s.artist == artistName).SingleOrDefaultAsync();

            if(ltSong == null)
            {
                using (HttpClient client = new HttpClient())
                {
                    try
                    {
                        string APIKEY = "42e54ad1938a778f19d2e21ee2c31a60";
                        client.BaseAddress = new Uri("http://api.onemusicapi.com");
                        HttpResponseMessage response = await client.GetAsync($"/20151208/release?user_key={APIKEY}&title={songName}&artist={artistName}");
                        response.EnsureSuccessStatusCode();

                        string stringResult = await response.Content.ReadAsStringAsync();

                        List<Release> rawSong = Newtonsoft.Json.JsonConvert.DeserializeObject<List<Release>>(stringResult);
                        
                        Release release = rawSong.FirstOrDefault();
                        ListenedTo newLT = null;
                        if ( release != null)
                        {
                            ltSong = _addSong(release.title, release.artist, release.genre).Result;
                            
                            newLT = new ListenedTo()
                            {
                                listenedToID = Guid.NewGuid().ToString(),
                                fk_songID = ltSong.songID,
                                fk_userID = userID,
                                dateListened = DateTime.Now
                            };

                            await _context.ListenedTos.AddAsync(newLT);
                            await _context.SaveChangesAsync();

                            foreach(Track t in release.media.FirstOrDefault().tracks)
                            {
                               await _addSong(t.title, release.artist, release.genre);
                            }
                        }

                        return Ok(newLT);
                    }
                    catch (Exception e)
                    {
                        var rawSong = "rip";
                    }

                }

            }
            else
            {
                ListenedTo newLT = new ListenedTo()
                {
                    listenedToID = Guid.NewGuid().ToString(),
                    fk_songID = ltSong.songID,
                    fk_userID = userID,
                    dateListened = DateTime.Now
                };

                await _context.ListenedTos.AddAsync(newLT);
                await _context.SaveChangesAsync();

                return Ok(newLT);
            }

            return NoContent();
        }


        // PUT api/values/5
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(string id, [FromBody] ListenedTo value)
        {
            ListenedTo newListenedTo = await _context.ListenedTos.FindAsync(id);

            if (newListenedTo == null)
            {
                return NoContent();
            }

            newListenedTo.fk_userID = value.fk_userID;
            newListenedTo.fk_songID = value.fk_songID;
            newListenedTo.dateListened = value.dateListened;

            await _context.SaveChangesAsync();

            return Ok(newListenedTo);

        }

        // DELETE api/values/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            ListenedTo ListenedTo = await _context.ListenedTos.FindAsync(id);

            if (ListenedTo == null)
            {
                return BadRequest();
            }

            _context.ListenedTos.Remove(ListenedTo);
            await _context.SaveChangesAsync();

            return Ok();
        }

        private async Task<Song> _addSong(string title, string artist, string genre)
        {
            Song newSong = await _context.Songs.Where(s => s.songName == title && s.artist == artist).SingleOrDefaultAsync();

            if(newSong == null)
            {
                newSong = new Song()
                {
                    songID = Guid.NewGuid().ToString(),
                    songName = title,
                    artist = artist,
                    genre = genre.ToLower(),
                };

                await _context.Songs.AddAsync(newSong);
                await _context.SaveChangesAsync();

            }

            return newSong;
        }
    }
}
