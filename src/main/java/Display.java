import processing.core.PApplet;

public class Display extends PApplet {
    public static void main(String[] args) {
        PApplet.main("Display");
    }

    public void settings() {
        size(640, 480);
    }

    public void setup() {
        background(0);
    }

    public void draw() {
        fill(255);
        ellipse(mouseX, mouseY, 50, 50);
    }
}
