package com.github._0x4248;

import com.github._0x4248.JSDLVGA.Display;
import com.github._0x4248.JSDLVGA.Text;
import com.github._0x4248.JSDLVGA.VgaGraphics;

public class Main {
    public static void main(String[] args) {
        Display display = new Display(100,100);
        display.selfUpdating = true;

        Thread mainThread = new Thread(new MainProcess(display));
        mainThread.start();

        display.run();
    }
}

class MainProcess implements Runnable {

    private final Display display;

    public MainProcess(Display display) {
        this.display = display;
    }

    @Override
    public void run() {
        VgaGraphics vga = new VgaGraphics(display);
        Text text = new Text(display);
        vga.clear(0);

        int i = 0;
        int posX = 0;
        int posY = 0;
        int colour = 1;
        try {
            while (display.isRunning()) {
                for (int j = 0; j < 20; j++) {
                    for (int k = 0; k < 20; k++) {
                        vga.setPixel(posX + j, posY + k, colour);
                    }
                }

                Thread.sleep(100);
                posX += 20;
                if (posX >= display.getWidth()) {
                    posX = 0;
                    posY += 20;
                }
                colour += 1;

                if (colour >= 16) {
                    posX = 0;
                    posY = 0;
                    colour = 1;
                    text.render_string(0, 75, "Hello World! " + i);
                    Thread.sleep(1000);
                    vga.clear(0);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
