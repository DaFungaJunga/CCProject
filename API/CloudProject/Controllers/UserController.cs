using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using CloudProject.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace CloudProject.Controllers
{
    [Route("cloud/[controller]")]
    [ApiController]
    public class UserController : ControllerBase
    {
        private readonly cloudContext _context;

        public UserController(cloudContext context)
        {
            _context = context;
        }

        // GET api/values
        [HttpGet]
        public async Task<IActionResult> Get()
        {
            IList<User> users = await _context.Users.Include(u => u.songs).ThenInclude(l => l.song).ToListAsync();

            return Ok(users);
        }

        // GET api/values/5
        [HttpGet("{id}")]
        public async Task<IActionResult> Get(string id)
        {
            User user = await _context.Users.Where(u => u.userID == id).Include(u => u.songs).ThenInclude(l => l.song).SingleOrDefaultAsync();
            return Ok(user);
        }

        // POST api/values
        [HttpPost]
        public async Task<IActionResult> Post([FromBody] User value)
        {
            User newUser = new User()
            {
                userID = Guid.NewGuid().ToString(),
                userName = value.userName
            };

            await _context.Users.AddAsync(newUser);
            await _context.SaveChangesAsync();

            return Ok(newUser);
        }

        // PUT api/values/5
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(string id, [FromBody] User value)
        {
            User newUser = await _context.Users.FindAsync(id);

            if(newUser == null)
            {
                return NoContent();
            }

            newUser.userID = value.userID;
            newUser.userName = value.userName;
            
            await _context.SaveChangesAsync();

            return Ok(newUser);

        }

        // DELETE api/values/5
        [HttpDelete("{id}")]
        public async Task<IActionResult> Delete(string id)
        {
            User user = await _context.Users.FindAsync(id);

            if(user == null)
            {
                return BadRequest();
            }

            _context.Users.Remove(user);
            await _context.SaveChangesAsync();

            return Ok();
        }
    }
}
