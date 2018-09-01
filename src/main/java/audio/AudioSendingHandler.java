package audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioSendingHandler implements AudioSendHandler
{
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    public AudioSendingHandler(AudioPlayer audioPlayer)
    {
        this.audioPlayer = audioPlayer;
    }

    @Override
    public boolean canProvide()
    {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public byte[] provide20MsAudio()
    {
        return lastFrame.getData();
    }

    @Override
    public boolean isOpus()
    {
        return true;
    }
}
