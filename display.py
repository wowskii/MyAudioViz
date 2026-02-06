
# Minimal p5 sketch to verify runtime import and basic draw loop.
from p5 import setup, draw, size, background, fill, rect, run, triangle
from midi_conversion import midi_to_dict
import time

# Physics params for a punchy kick (spring-damper)
_SPRING_K = 800.0
_DAMPING_C = 40.0
_IMPULSE = 400.0

# animation state
tri_pos = 0.0
tri_vel = 0.0

# timing state (filled after loading midi)
event_times = []
next_event_idx = 0
start_time = None
last_time = None


def setup():
    global start_time, last_time, event_times
    size(480, 320)
    # prepare timing
    mid = midi_to_dict('midi/kick.mid')
    event_times = sorted(mid.keys())
    start_time = time.time()
    last_time = start_time


def draw():
    global tri_pos, tri_vel, next_event_idx, last_time

    now = time.time()
    dt = now - last_time if last_time is not None else 1.0 / 60.0
    last_time = now

    # check for midi events and trigger impulse when time passes
    if next_event_idx < len(event_times):
        elapsed = now - start_time
        # trigger all events that have passed (in case of frame drops)
        while next_event_idx < len(event_times) and elapsed >= event_times[next_event_idx]:
            tri_vel += _IMPULSE
            next_event_idx += 1

    # spring-damper integration for punchy motion
    a = -_SPRING_K * tri_pos - _DAMPING_C * tri_vel
    tri_vel += a * dt
    tri_pos += tri_vel * dt

    # small threshold to zero out tiny oscillations
    if abs(tri_pos) < 0.25 and abs(tri_vel) < 1.0:
        tri_pos = 0.0
        tri_vel = 0.0

    background(0, 0, 0)
    fill(255, 255, 255)
    rect(100, 100, 50, 20)
    # apply vertical offset to triangle points
    triangle(125, 100 + tri_pos, 100, 50 + tri_pos, 150, 50 + tri_pos)


if __name__ == "__main__":
    run()