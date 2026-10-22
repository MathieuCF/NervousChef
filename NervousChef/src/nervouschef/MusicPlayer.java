package nervouschef;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MusicPlayer {

    private static final double SMOOTH_SECONDS = 0.25;   // how long muffle/volume changes take

    private byte[] data;
    private AudioFormat format;
    private Thread thread;
    private volatile boolean running;

    private final double normalVolume;
    private final double quietVolume;
    private double muffledAlpha;

    // the audio thread eases toward these values
    private volatile double targetAlpha = 1.0;
    private volatile double targetVolume;

    public MusicPlayer(String path, float normalDb, float quietDb, double cutoffHz) {
        normalVolume = Math.pow(10, normalDb / 20.0);
        quietVolume = Math.pow(10, quietDb / 20.0);
        targetVolume = quietVolume;

        try {
            URL url = MusicPlayer.class.getResource(path);
            if (url == null) {
                System.out.println("Music not found: " + path);
                return;
            }
            AudioInputStream in = AudioSystem.getAudioInputStream(url);
            AudioFormat base = in.getFormat();

            // force 16-bit signed little endian PCM so the sample maths is simple
            format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(),
                    16, base.getChannels(), base.getChannels() * 2, base.getSampleRate(), false);  // thanks stackoverflow
            data = AudioSystem.getAudioInputStream(format, in).readAllBytes();
            data = java.util.Arrays.copyOf(data, data.length - data.length % format.getFrameSize());

            muffledAlpha = 1.0 - Math.exp(-2 * Math.PI * cutoffHz / format.getSampleRate());  // I barely passed signals and systems
        } catch (UnsupportedAudioFileException | IOException e) {
            System.out.println("Failed to load music " + path + ": " + e.getMessage());
            data = null;
        }
    }

    public void play() {
        if (data == null || thread != null) return;
        running = true;
        thread = new Thread(this::run, "music");
        thread.setDaemon(true);   // won't keep the program alive on exit
        thread.start();
    }

    // true = muffled and quieter, false = full sound
    public void setMuffled(boolean muffled) {
        targetAlpha = muffled ? muffledAlpha : 1.0;
        targetVolume = muffled ? quietVolume : normalVolume;
    }

    private void run() {
        int frameSize = format.getFrameSize();
        int channels = format.getChannels();
        double rate = 1.0 / (format.getSampleRate() * SMOOTH_SECONDS);

        double alpha = targetAlpha;
        double volume = targetVolume;
        double[] state = new double[channels];   // filter memory per channel
        byte[] buf = new byte[frameSize * 512];
        int pos = 0;

        try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
            line.open(format, 8192);   // small buffer = changes are heard quickly
            line.start();

            while (running) {
                for (int i = 0; i < buf.length; i += frameSize) {
                    alpha += (targetAlpha - alpha) * rate;
                    volume += (targetVolume - volume) * rate;

                    for (int c = 0; c < channels; c++) {
                        int idx = pos + c * 2;
                        short sample = (short) ((data[idx + 1] << 8) | (data[idx] & 0xFF));

                        state[c] += alpha * (sample - state[c]);   // low-pass filter

                        int out = (int) Math.round(state[c] * volume);
                        out = Math.max(-32768, Math.min(32767, out));
                        buf[i + c * 2] = (byte) out;
                        buf[i + c * 2 + 1] = (byte) (out >> 8);
                    }

                    pos += frameSize;
                    if (pos >= data.length) pos = 0;   // loop
                }
                line.write(buf, 0, buf.length);   // blocks until there's room, which paces the loop
            }
        } catch (LineUnavailableException e) {
            System.out.println("Music line unavailable: " + e.getMessage());
        }
    }
}