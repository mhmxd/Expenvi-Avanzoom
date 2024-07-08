package tool;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class Sounder {
    private static Clip errorClip, hitClip, missClip, taskEndClip;

    static {
        try {
            final File hitFile = new File(Resources.WAV_HIT_URI);
            final File missFile = new File(Resources.WAV_ERROR_URI);
            final File errorFile = new File(Resources.WAV_ERROR_URI);
            final File taskEndFile = new File(Resources.WAV_END_URI);

            errorClip = AudioSystem.getClip();
//            errorClip.open(AudioSystem.getAudioInputStream(errorFile));

            hitClip = AudioSystem.getClip();
            hitClip.open(AudioSystem.getAudioInputStream(hitFile));

            missClip = AudioSystem.getClip();
            missClip.open(AudioSystem.getAudioInputStream(missFile));

            taskEndClip = AudioSystem.getClip();
            taskEndClip.open(AudioSystem.getAudioInputStream(taskEndFile));

        } catch (NullPointerException | IOException | UnsupportedAudioFileException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    public static void playError() {
        errorClip.setMicrosecondPosition(0); // Reset to the start of the file
        errorClip.start();
    }

    public static void playHit() {
        hitClip.setMicrosecondPosition(0); // Reset to the start of the file
        hitClip.start();
    }

    public static void playMiss() {
        missClip.setMicrosecondPosition(0); // Reset to the start of the file
        missClip.start();
    }

    public static void playTaskEnd() {
        taskEndClip.setMicrosecondPosition(0); // Reset to the start of the file
        taskEndClip.start();
    }
}
