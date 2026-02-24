import processing.core.PApplet;
import javax.sound.midi.*;
import java.util.List;

public class Display extends PApplet {
    private MidiSequenceLoader kickMidiLoader;
    private List<Beat> kicks;
    private AudioAnalyzer audioAnalyzer;
    private long animationStartTimeMs = 0;
    private long currentTimeMs = 0;
    private long lastBeatTimeMs = -1000;
    private float lastBeatVelocity = 0;
    
    public static void main(String[] args) {
        PApplet.main("Display");
    }

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(0);
        
        // Load MIDI file
        String midiPath = sketchPath("midi/kick.mid");
        kickMidiLoader = new MidiSequenceLoader(midiPath);
        kickMidiLoader.repeatSequence(4);
        kicks = kickMidiLoader.getBeats();
        
        println("Loaded " + kicks.size() + " kicks from MIDI");
        for (Beat kick : kicks) {
            println("  " + kick);
        }
        
        // Load audio file for volume visualization
        String audioPath = sketchPath("audio/arpymog_2.wav");
        audioAnalyzer = new AudioAnalyzer(audioPath);
        
        animationStartTimeMs = System.currentTimeMillis();
    }

    public void draw() {
        background(0);
        
        // Update current time
        currentTimeMs = System.currentTimeMillis() - animationStartTimeMs;
        
        // Display current time
        fill(255);
        textSize(16);
        text("Time: " + currentTimeMs + "ms", 10, 20);
        
        // Draw upcoming beats
        List<Beat> upcomingBeats = kickMidiLoader.getBeatsInRange(currentTimeMs, currentTimeMs + 2000);
        fill(100, 200, 100);
        textSize(14);
        text("Upcoming beats: " + upcomingBeats.size(), 10, 50);
        
        // Detect new beat and record it
        if (upcomingBeats.size() > 0 && upcomingBeats.get(0).timeMs <= currentTimeMs + 10 && upcomingBeats.get(0).timeMs > lastBeatTimeMs) {
            lastBeatTimeMs = upcomingBeats.get(0).timeMs;
            lastBeatVelocity = upcomingBeats.get(0).velocity;
        }
        
        // Get current volume from audio
        float currentVolume = audioAnalyzer.getAverageVolume(currentTimeMs - 20, currentTimeMs + 20);
        
        // Draw volume-responsive circle
        fill(100, 150, 255);
        float circleSize = map(currentVolume, 0, 1, 20, 150);
        ellipse(width / 2, height / 2, circleSize, circleSize);
        
        // Draw circle info
        fill(200);
        textSize(12);
        text("Volume: " + String.format("%.2f", currentVolume), 10, 80);
        
        // Draw a triangle that bounces on beats with easing
        fill(255);
        float bounceAmount = 0;
        long timeSinceBeat = currentTimeMs - lastBeatTimeMs;
        float animationDuration = 150; // Duration of bounce effect in ms
        
        if (timeSinceBeat < animationDuration) {
            float progress = timeSinceBeat / animationDuration;  // 0 to 1
            float bounceMax = map(lastBeatVelocity, 0, 127, 0, 50);
            
            // Ease in-out cubic: smooth acceleration and deceleration
            float eased = progress < 0.5f 
                ? 4 * progress * progress * progress 
                : 1 - pow(-2 * progress + 2, 3) / 2;
            
            bounceAmount = bounceMax * (1.0f - eased);
        }
        
        System.out.println(lastBeatTimeMs);
        pushMatrix();
        translate(bounceAmount, 0);
        triangle(200, 200, 200, 300, 300, 250);
        popMatrix();
        
        rect(350, 150, 50, 200);
    }
}
