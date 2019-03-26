using CloudProject.Models;
using Microsoft.EntityFrameworkCore;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject
{
    public class cloudContext : DbContext
    {
        public cloudContext(DbContextOptions<cloudContext> options) : base(options)
        {
        }

        public DbSet<User> Users { get; set; }

        public DbSet<Song> Songs { get; set; }

        public DbSet<ListenedTo> ListenedTos { get; set; }
    }
}
