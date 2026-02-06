
# Minimal p5 sketch to verify runtime import and basic draw loop.
from p5 import setup, draw, size, background, fill, ellipse, run

def setup():
    size(480, 320)
    # no-op; existing imports and initialization exercise package import paths

def draw():
    background(30, 30, 40)
    fill(200, 60, 80)
    ellipse((240, 160), 120, 120)

if __name__ == "__main__":
    run()