package com.github._0x4248;

import com.github._0x4248.JSDLVGA.Display;

public class Main {
    public static void main(String[] args) {
        Display display = new Display(100,100);
        display.selfUpdating = true;

        Thread logicThread = new Thread(new MainProcess(display), "MainProcess");
        logicThread.start();

        display.run();
    }
}
