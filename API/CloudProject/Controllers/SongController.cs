using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using CloudProject.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CloudProject.Controllers
{
    [Route("api/[controller]")]
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

        // GET api/values/5
        [HttpGet("{id}")]
        public async Task<IActionResult> Get(string id)
        {
            Song Song = await _context.Songs.Where(u => u.songID == id).SingleOrDefaultAsync();
            return Ok(Song);
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
    }
}
