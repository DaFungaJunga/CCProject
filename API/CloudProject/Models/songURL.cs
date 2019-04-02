using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject.Models
{
    public class SongURL
    {
        public string videoID { get; set; }

        public string videoTitle { get; set; }

        public Song song { get; set; }
    }
}
