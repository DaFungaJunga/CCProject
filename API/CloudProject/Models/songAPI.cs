using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject.Models
{

    public class Release
    {
        public string title { get; set; }

        public string artist { get; set; }

        public string year { get; set; }

        public string genre { get; set; }

        public IEnumerable<Media> media { get; set; }
    }

    public class Media
    {
        public IEnumerable<Track> tracks { get; set; }
    }
    public class Track
    {
        public string title { get; set; }

        public string artist { get; set; }
    }
}
