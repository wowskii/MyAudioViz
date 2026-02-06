import mido
mid = mido.MidiFile('midi/kick.mid')
for track in mid.tracks:
    print(f'Track: {track}')
    for msg in track:
        print(msg)