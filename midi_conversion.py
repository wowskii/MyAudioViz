import mido


def midi_to_dict(midi_file):
    """Return a dict mapping event times (in seconds) -> note (or 0 for note_off).

    Uses a merged track and converts delta ticks to seconds handling tempo changes.
    """
    mid = mido.MidiFile(midi_file)
    merged = mido.merge_tracks(mid.tracks)
    ticks_per_beat = mid.ticks_per_beat

    events = {}
    current_tempo = 500000  # default microseconds per beat
    current_seconds = 0.0

    for msg in merged:
        # convert delta ticks to seconds using current tempo
        if msg.time:
            delta_seconds = mido.tick2second(msg.time, ticks_per_beat, current_tempo)
            current_seconds += delta_seconds

        if msg.type == 'set_tempo':
            current_tempo = msg.tempo

        if msg.type == 'note_on' and getattr(msg, 'velocity', 0) > 0:
            events[current_seconds] = msg.note
        elif msg.type == 'note_off' or (msg.type == 'note_on' and getattr(msg, 'velocity', 0) == 0):
            events[current_seconds] = 0

    return events
