/**
 * Represents a single beat/note event from a MIDI file
 */
public class Beat {
    public long timeMs;      // Time in milliseconds from start
    public int velocity;     // MIDI velocity (0-127)
    public int noteNumber;   // MIDI note number (0-127)
    
    public Beat(long timeMs, int velocity, int noteNumber) {
        this.timeMs = timeMs;
        this.velocity = velocity;
        this.noteNumber = noteNumber;
    }
    
    @Override
    public String toString() {
        return String.format("Beat[time=%dms, velocity=%d, note=%d]", timeMs, velocity, noteNumber);
    }
}
