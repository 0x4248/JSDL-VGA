package com.github._0x4248;

public class Main {
    public static void main(String[] args) {
        Display display = new Display(200,200);
        display.selfUpdating = true;

        Thread logicThread = new Thread(new MainProcess(display), "MainProcess");
        logicThread.start();

        display.run();
    }
}
