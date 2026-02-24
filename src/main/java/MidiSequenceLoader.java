import javax.sound.midi.*;
import java.io.File;
import java.util.*;

/**
 * Loads a MIDI file and extracts beat information
 */
public class MidiSequenceLoader {
    private List<Beat> beats;
    
    public MidiSequenceLoader(String filePath) {
        beats = new ArrayList<>();
        loadSequence(filePath);
    }
    
    private void loadSequence(String filePath) {
        try {
            File midiFile = new File(filePath);
            Sequence sequence = MidiSystem.getSequence(midiFile);
            
            // Get tempo information (microseconds per beat)
            float divisionType = sequence.getDivisionType();
            int resolution = sequence.getResolution();
            
            // Extract beats from all tracks
            for (Track track : sequence.getTracks()) {
                processMidiTrack(track, divisionType, resolution);
            }
            
            // Sort beats by time
            beats.sort(Comparator.comparingLong(b -> b.timeMs));
            
            System.out.println("Loaded " + beats.size() + " beats from MIDI file");
            
        } catch (Exception e) {
            System.err.println("Error loading MIDI file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void processMidiTrack(Track track, float divisionType, int resolution) {
        long currentTempo = 500000; // Default tempo: 120 BPM in microseconds per beat
        
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage message = event.getMessage();
            long tick = event.getTick();
            
            // Check for tempo change
            if (message instanceof MetaMessage) {
                MetaMessage metaMessage = (MetaMessage) message;
                if (metaMessage.getType() == 0x51) { // Tempo change
                    byte[] data = metaMessage.getData();
                    currentTempo = ((data[0] & 0xff) << 16) | 
                                   ((data[1] & 0xff) << 8) | 
                                   (data[2] & 0xff);
                }
            }
            
            // Extract note-on events
            if (message instanceof ShortMessage) {
                ShortMessage shortMessage = (ShortMessage) message;
                
                // 0x90 = Note On, 0x80 = Note Off
                if (shortMessage.getCommand() == 0x90 && shortMessage.getData2() > 0) {
                    long timeMs = ticksToMs(tick, resolution, currentTempo);
                    int velocity = shortMessage.getData2();
                    int noteNumber = shortMessage.getData1();
                    
                    beats.add(new Beat(timeMs, velocity, noteNumber));
                }
            }
        }
    }
    
    private long ticksToMs(long tick, int resolution, long tempoMicros) {
        // Convert ticks to milliseconds using tempo and resolution
        double beatTime = (double) tempoMicros / 1000.0; // Convert microseconds to milliseconds
        double timeMs = (tick / (double) resolution) * beatTime;
        return Math.round(timeMs);
    }
    
    /**
     * Get all beats from the loaded MIDI file
     */
    public List<Beat> getBeats() {
        return new ArrayList<>(beats);
    }
    
    /**
     * Get beats within a specific time range (useful for animation playback)
     */
    public List<Beat> getBeatsInRange(long startMs, long endMs) {
        return beats.stream()
                .filter(b -> b.timeMs >= startMs && b.timeMs <= endMs)
                .toList();
    }

    public void repeatSequence(int times) {
        if (beats.isEmpty()) return;
        
        long sequenceDuration = beats.get(beats.size() - 1).timeMs;
        List<Beat> repeatedBeats = new ArrayList<>();
        
        for (int i = 0; i < times; i++) {
            long offset = i * sequenceDuration;
            for (Beat beat : beats) {
                repeatedBeats.add(new Beat(beat.timeMs + offset, beat.velocity, beat.noteNumber));
            }
        }
        
        beats = repeatedBeats;
    }
}
