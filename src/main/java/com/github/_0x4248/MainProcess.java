package com.github._0x4248;


public class MainProcess implements Runnable {

    private final Display display;

    public MainProcess(Display display) {
        this.display = display;
    }

    @Override
    public void run() {

        int i = 0;
        try {
            while (display.isRunning()) {
                display.setPixel(i , i );
                i++;

                Thread.sleep(100);
            }
        } catch (InterruptedException ignored) {}
    }

}
