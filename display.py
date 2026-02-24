import sys
import argparse
import math
import time
from typing import Callable, Dict, List, Tuple, Any

from p5 import setup, draw, size, background, triangle, fill, no_stroke, run as p5_run, set_frame_rate

from midi_conversion import midi_to_dict


# Easing functions (take t in [0,1] -> eased t)
def linear(t: float) -> float:
	return t


def ease_in_quad(t: float) -> float:
	return t * t


def ease_out_quad(t: float) -> float:
	return t * (2 - t)


def ease_in_out_quad(t: float) -> float:
	if t < 0.5:
		return 2 * t * t
	return -1 + (4 - 2 * t) * t


EASINGS: Dict[str, Callable[[float], float]] = {
	"linear": linear,
	"in_quad": ease_in_quad,
	"out_quad": ease_out_quad,
	"in_out_quad": ease_in_out_quad,
}


class Keyframe:
	def __init__(self, time_s: float, value: Any, easing: str = "linear"):
		self.time = float(time_s)
		self.value = value
		self.easing = EASINGS.get(easing, linear)


class Animation:
	"""Generic timeline animation.

	Stores keyframes ordered by time and interpolates between them using easing.
	The `lerp` function must be provided for the value type (e.g., numbers, tuples).
	"""

	def __init__(self, keyframes: List[Keyframe], lerp: Callable[[Any, Any, float], Any]):
		assert keyframes, "At least one keyframe required"
		# sort keyframes by time
		self.keyframes = sorted(keyframes, key=lambda k: k.time)
		self.lerp = lerp

	def value_at(self, t: float) -> Any:
		if t <= self.keyframes[0].time:
			return self.keyframes[0].value
		if t >= self.keyframes[-1].time:
			return self.keyframes[-1].value

		# find segment
		for a, b in zip(self.keyframes, self.keyframes[1:]):
			if a.time <= t <= b.time:
				span = b.time - a.time
				if span == 0:
					return b.value
				local_t = (t - a.time) / span
				eased = b.easing(local_t)
				return self.lerp(a.value, b.value, eased)


def lerp_tuple(a: Tuple[float, float], b: Tuple[float, float], t: float) -> Tuple[float, float]:
	return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t)


# Globals used by the p5 callbacks
_midi_file = "midi/kick.mid"
_width = 800
_height = 600
_easing_choice = "in_out_quad"
_anim: Animation = None  # type: ignore
_tri_size = 80
_start_time = 0.0


def build_triangle_animation_from_midi(midi_file: str, pos_a: Tuple[float, float], pos_b: Tuple[float, float],
									   easing: str = "in_out_quad") -> Animation:
	"""
	Create a position animation for a triangle that toggles between `pos_a` and `pos_b`
	at each MIDI event time. Uses easing provided.
	"""
	events = midi_to_dict(midi_file)
	times = sorted(events.keys())

	if not times:
		# fallback: simple 0..1s movement
		kf = [Keyframe(0.0, pos_a, easing), Keyframe(1.0, pos_b, easing)]
		return Animation(kf, lerp_tuple)

	keyframes: List[Keyframe] = []
	toggle = False
	for t in times:
		val = pos_b if toggle else pos_a
		keyframes.append(Keyframe(t, val, easing))
		toggle = not toggle

	# ensure last keyframe stays for a tiny moment
	last_t = keyframes[-1].time
	keyframes.append(Keyframe(last_t + 0.0001, keyframes[-1].value, easing))

	return Animation(keyframes, lerp_tuple)


def _triangle_points(center: Tuple[float, float], size: float) -> Tuple[Tuple[float, float], Tuple[float, float], Tuple[float, float]]:
	cx, cy = center
	h = size * math.sqrt(3) / 2
	return ((cx, cy - 2 * h / 3), (cx - size / 2, cy + h / 3), (cx + size / 2, cy + h / 3))


def setup():
	global _start_time
	size(_width, _height)
	#set_frame_rate(60)
	no_stroke()
	_start_time = time.time()


def draw():
	global _anim
	background(0)
	current_time = time.time() - _start_time
	pos = _anim.value_at(current_time)
	p1, p2, p3 = _triangle_points(pos, _tri_size)
	fill(255)
	triangle(p1[0], p1[1], p2[0], p2[1], p3[0], p3[1])


def main():
	global _midi_file, _width, _height, _easing_choice, _anim
	parser = argparse.ArgumentParser(description="Triangle keyframe demo driven by MIDI times (p5.py)")
	parser.add_argument("midi", nargs="?", default="midi/kick.mid", help="Path to a MIDI file")
	parser.add_argument("--width", type=int, default=800)
	parser.add_argument("--height", type=int, default=600)
	parser.add_argument("--easing", choices=list(EASINGS.keys()), default="in_out_quad")
	args = parser.parse_args()

	_midi_file = args.midi
	_width = args.width
	_height = args.height
	_easing_choice = args.easing

	pos_a = (_width * 0.25, _height * 0.5)
	pos_b = (_width * 0.75, _height * 0.5)
	_anim = build_triangle_animation_from_midi(_midi_file, pos_a, pos_b, easing=_easing_choice)

	try:
		p5_run()
	except FileNotFoundError:
		print(f"MIDI file not found: {_midi_file}")
		sys.exit(1)


if __name__ == "__main__":
	main()

