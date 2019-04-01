using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject.Models
{
    public class UserDto
    {
        public string userID { get; set; }

        public string userName { get; set; }

        public ICollection<ListenedTo> songs { get; set; }
    }
}
