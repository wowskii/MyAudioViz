import javax.sound.sampled.*;
import java.io.File;
import java.util.*;

/**
 * Analyzes an audio file and extracts volume/amplitude data over time
 */
public class AudioAnalyzer {
    private List<VolumeFrame> volumeFrames;
    private float sampleRate;
    
    public AudioAnalyzer(String filePath) {
        volumeFrames = new ArrayList<>();
        analyzeAudio(filePath);
    }
    
    private void analyzeAudio(String filePath) {
        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
            AudioFormat format = audioStream.getFormat();
            
            sampleRate = format.getSampleRate();
            int bytesPerFrame = format.getFrameSize();
            int channels = format.getChannels();
            
            // Read audio data in chunks
            byte[] audioData = new byte[4096];
            int bytesRead;
            long currentFramePosition = 0;
            
            // Get the maximum possible amplitude based on bit depth
            int sampleSizeInBits = format.getSampleSizeInBits();
            float maxAmplitude = (float) Math.pow(2, sampleSizeInBits - 1);
            
            while ((bytesRead = audioStream.read(audioData)) != -1) {
                // Sample every N frames to reduce data points
                int frameSamples = bytesRead / bytesPerFrame;
                float[] sampledAmplitudes = extractAmplitudes(audioData, bytesRead, channels, maxAmplitude);
                
                for (float amp : sampledAmplitudes) {
                    long timeMs = (long) (currentFramePosition / sampleRate * 1000);
                    volumeFrames.add(new VolumeFrame(timeMs, amp));
                    currentFramePosition++;
                }
            }
            
            audioStream.close();
            System.out.println("Analyzed audio: " + volumeFrames.size() + " volume frames");
            
        } catch (Exception e) {
            System.err.println("Error analyzing audio file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private float[] extractAmplitudes(byte[] audioData, int bytesRead, int channels, float maxAmplitude) {
        List<Float> amplitudes = new ArrayList<>();
        
        // Sample every 512 bytes to reduce data density
        int samplingInterval = 512;
        
        for (int i = 0; i < bytesRead; i += samplingInterval) {
            float totalAmplitude = 0;
            int sampleCount = 0;
            
            // Average amplitudes across sampling interval
            for (int j = i; j < Math.min(i + samplingInterval, bytesRead); j += 2) {
                if (j + 1 < bytesRead) {
                    // Convert byte pair to signed short
                    short sample = (short) ((audioData[j + 1] << 8) | (audioData[j] & 0xFF));
                    totalAmplitude += Math.abs(sample);
                    sampleCount++;
                }
            }
            
            if (sampleCount > 0) {
                float avgAmplitude = (totalAmplitude / sampleCount) / maxAmplitude;
                amplitudes.add(Math.min(1.0f, avgAmplitude));
            }
        }
        
        float[] result = new float[amplitudes.size()];
        for (int i = 0; i < amplitudes.size(); i++) {
            result[i] = amplitudes.get(i);
        }
        return result;
    }
    
    /**
     * Get the volume at a specific time
     */
    public float getVolumeAt(long timeMs) {
        if (volumeFrames.isEmpty()) return 0;
        
        // Binary search for nearest frame
        int index = Collections.binarySearch(volumeFrames, 
            new VolumeFrame(timeMs, 0), 
            Comparator.comparingLong(f -> f.timeMs));
        
        if (index < 0) {
            index = -(index + 1);
        }
        
        if (index >= volumeFrames.size()) {
            return volumeFrames.get(volumeFrames.size() - 1).amplitude;
        }
        
        return volumeFrames.get(index).amplitude;
    }
    
    /**
     * Get average volume over a time range (useful for smoothing)
     */
    public float getAverageVolume(long startMs, long endMs) {
        float sum = 0;
        int count = 0;
        
        for (VolumeFrame frame : volumeFrames) {
            if (frame.timeMs >= startMs && frame.timeMs <= endMs) {
                sum += frame.amplitude;
                count++;
            }
        }
        
        return count > 0 ? sum / count : 0;
    }
    
    public List<VolumeFrame> getVolumeFrames() {
        return new ArrayList<>(volumeFrames);
    }
}
