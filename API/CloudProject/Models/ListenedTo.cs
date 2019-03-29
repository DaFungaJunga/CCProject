using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject.Models
{
    public class ListenedTo
    {
        [Key]
        public string listenedToID { get; set; }

        public string fk_userID { get; set; }

        public string fk_songID { get; set; }

        public DateTime dateListened { get; set; }

        [ForeignKey("fk_songID")]
        public Song song { get; set; }
    }
}
