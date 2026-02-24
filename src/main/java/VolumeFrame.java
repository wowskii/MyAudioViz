/**
 * Represents a volume sample at a specific point in time
 */
public class VolumeFrame {
    public long timeMs;        // Time in milliseconds from start
    public float amplitude;    // Volume level (0.0 to 1.0)
    
    public VolumeFrame(long timeMs, float amplitude) {
        this.timeMs = timeMs;
        this.amplitude = Math.max(0, Math.min(1, amplitude)); // Clamp to 0-1
    }
    
    @Override
    public String toString() {
        return String.format("VolumeFrame[time=%dms, amplitude=%.2f]", timeMs, amplitude);
    }
}
