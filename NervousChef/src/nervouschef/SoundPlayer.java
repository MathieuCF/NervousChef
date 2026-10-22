package nervouschef;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SoundPlayer {

    private static final int VOICES = 3;   // copies per sound so fast swipes can overlap

    private final List<Clip[]> sounds = new ArrayList<>();
    private final int[] nextVoice;
    private final Random random = new Random();
    private int lastPlayed = -1;
    private final float volumeDb;

    public SoundPlayer(String folder, String prefix, int count, float volumeDb) {
        this.volumeDb = volumeDb;
        nextVoice = new int[count];

        for (int i = 1; i <= count; i++) {
            String path = folder + prefix + i + ".wav";
            Clip[] voices = new Clip[VOICES];

            for (int v = 0; v < VOICES; v++) {
                voices[v] = loadClip(path);
            }
            sounds.add(voices);
        }
    }

    private Clip loadClip(String path) {
        try {
            URL url = SoundPlayer.class.getResource(path);
            if (url == null) {
                System.out.println("Sound not found: " + path);
                return null;
            }
            AudioInputStream stream = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {  // lower volume, default sound a bit too loud
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), volumeDb)));
            }
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("Failed to load " + path + ": " + e.getMessage());
            return null;
        }
    }

    // plays a random sound, never the same one twice in a row
    public void playRandom() {
        if (sounds.isEmpty()) return;

        int index;
        do {
            index = random.nextInt(sounds.size());
        } while (index == lastPlayed && sounds.size() > 1);
        lastPlayed = index;

        Clip[] voices = sounds.get(index);
        Clip clip = voices[nextVoice[index]];
        nextVoice[index] = (nextVoice[index] + 1) % VOICES;

        if (clip == null) return;

        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }
}