Triangle Keyframe Demo
======================


This small demo shows a white triangle on a black background moving between
two positions driven by keyframe times taken from a MIDI file. The MIDI parsing
is handled by `midi_conversion.midi_to_dict`, which maps event times (seconds)
to note values; the times are used as animation keyframe times.

Run

```bash
pip install -r requirements.txt
python display.py midi/kick.mid
```

Options:
- `--width` and `--height`: window size
- `--easing`: easing function (choices: `linear`, `in_quad`, `out_quad`, `in_out_quad`)

Notes:
- This version uses `p5.py` for drawing so you can practice with the p5 API.

Files:
- [display.py](display.py): main animation/demo and reusable animation classes (p5-based)
- [midi_conversion.py](midi_conversion.py): MIDI -> times mapping used by the demo
