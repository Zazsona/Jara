package commands.toys;

import commands.Command;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;

import java.util.Random;

public class Timecard extends Command
{
    @Override
    public void run(GuildMessageReceivedEvent msgEvent, String... parameters)
    {
        String[] Timecards = { "https://www.dafont.com/img/illustration/s/o/some_time_later.png", "https://i.ytimg.com/vi/td-CfSEI8Rk/maxresdefault.jpg", "http://78.media.tumblr.com/tumblr_m7dg905ipA1r6f921o1_500.png", "http://media.tumblr.com/tumblr_lhb8kgH5HV1qzz55x.png", "http://3.bp.blogspot.com/-ZV8mQpRxS6E/U8dVpHpdiAI/AAAAAAAABNw/XRFoyEFnoow/s1600/Tomorrow+For+Sure.png", "https://i.imgur.com/89FPHlL.jpg", "https://i.imgur.com/TpKZIF4.jpg", "https://i.imgur.com/YzYjaYR.jpg", "https://i.imgur.com/gqFvqPg.jpg", "https://i.imgur.com/sYDIWWS.jpg", "https://i.imgur.com/KU6K3d8.jpg", "https://i.imgur.com/ZcqI9KQ.jpg", "https://i.imgur.com/Z8dsm9J.jpg", "https://i.imgur.com/meTcvwA.jpg", "https://i.imgur.com/dVUbDoH.jpg", "https://i.imgur.com/OwGIdqX.jpg", "https://i.imgur.com/Sluf5bJ.jpg", "https://i.imgur.com/Genr8PP.jpg", "https://i.imgur.com/vi14kWO.jpg", "https://i.imgur.com/iUJU3GB.jpg", "https://i.imgur.com/U37SVGF.jpg", "https://i.imgur.com/iM1v7hR.jpg", "https://i.imgur.com/PMvEalE.jpg", "https://i.imgur.com/uDkkDXJ.jpg", "https://i.imgur.com/W8TaZ0h.jpg", "https://i.imgur.com/vgk3MTB.png", "https://i.imgur.com/yvfJp3J.png", "https://i.imgur.com/yiNmZge.png", "https://i.imgur.com/oBCzMzH.jpg", "https://i.imgur.com/RvSnP4u.png", "https://i.imgur.com/mk9a0kO.png", "https://i.imgur.com/HzDtFHE.png", "https://i.imgur.com/iNi2Oq1.png", "https://i.imgur.com/kEQzDsS.png"};
        Random r = new Random();
        msgEvent.getChannel().sendMessage(Timecards[r.nextInt(Timecards.length)]).queue();
    }
}
