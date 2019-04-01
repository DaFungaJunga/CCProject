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

            foreach(User u in users)
            {
                u.password = null;
            }
            return Ok(users);
        }

        // GET api/values/5
        [HttpGet("{id}")]
        public async Task<IActionResult> Get(string id)
        {
            User user = await _context.Users.Where(u => u.userID == id).Include(u => u.songs).ThenInclude(l => l.song).SingleOrDefaultAsync();
            user.password = null;
            return Ok(user);
        }

        /// <summary>
        /// login
        /// </summary>
        /// <param name="userName"></param>
        /// <param name="value"></param>
        /// <returns></returns>
        [HttpPost("{userName}")]
        public async Task<IActionResult> Login(string userName, User value)
        {
            User user = await _context.Users.Where(u => u.userName == userName).SingleOrDefaultAsync();

            if(user == null)
            {
                return BadRequest("user not found");
            }

            if(user.password != value.password)
            {
                return BadRequest("Incorrect password");
            }
            else
            {
                user.password = null;
                return Ok(user);
            }

        }

        // POST api/values
        [HttpPost]
        public async Task<IActionResult> Post([FromBody] User value)
        {
            User newUser = new User()
            {
                userID = Guid.NewGuid().ToString(),
                userName = value.userName,
                password = value.password
            };

            await _context.Users.AddAsync(newUser);
            await _context.SaveChangesAsync();

            newUser.password = null;
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

            newUser.password = null;
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
