using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.Logging;

namespace CloudProject
{
    public class Program
    {
        public static void Main(string[] args)
        {
            // custom webhostbuilder to set custom port number (as to not interfere with other API on ec2 VM)
            var host = new WebHostBuilder()
                    .UseKestrel()
                    .UseContentRoot(Directory.GetCurrentDirectory())
                    .UseUrls("http://localhost:5050", "http://localhost:5051")
                    .UseIISIntegration()
                    .UseStartup<Startup>();

            host.Build().Run();

        }
    }
}
