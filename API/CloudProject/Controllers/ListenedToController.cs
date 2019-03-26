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

        // POST api/values
        [HttpPost]
        public async Task<IActionResult> Post([FromBody] ListenedTo value)
        {
            ListenedTo newListenedTo = new ListenedTo()
            {
                listenedToID = Guid.NewGuid().ToString(),
                fk_songID = value.fk_songID,
                fk_userID = value.fk_userID
            };

            await _context.ListenedTos.AddAsync(newListenedTo);
            await _context.SaveChangesAsync();

            return Ok(newListenedTo);
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
    }
}
