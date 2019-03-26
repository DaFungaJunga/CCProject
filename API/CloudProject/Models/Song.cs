using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject.Models
{
    public class Song
    {
        [Key]
        public string songID { get; set; }

        public string songName { get; set; }
        
        public string artist { get; set; }

        public string genre { get; set; }

    }
}
