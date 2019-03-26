using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using System.Linq;
using System.Threading.Tasks;

namespace CloudProject.Models
{
    public class User
    {
        [Key]
        public string userID { get; set; }

        public string userName { get; set; }
        
        [ForeignKey("fk_userID")]
        public ICollection<ListenedTo> songs { get; set; }
    }
}
