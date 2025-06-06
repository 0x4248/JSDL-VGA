package com.github._0x4248.JSDLVGA;

public class Text {
    private final Display display;

    public Text(Display display) {
        this.display = display;
    }

    public void render_char(int x, int y, char c, int r, int g, int b) {
        int[][] font = FontArray.charFont8x8Basic;
        int charIndex = (int) c;
        if (charIndex >= font.length) return;

        int[] bitmap = font[charIndex];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if ((bitmap[row] & (1 << col)) != 0) {
                    display.setPixel(x + col, y + row, r, g, b);
                }
            }
        }
    }

    public void render_string(int x, int y, String str) {
        int spacing = 1;
        int charWidth = 8 + spacing;

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            render_char(x + i * charWidth, y, c, 255, 255, 255); // white text
        }

        display.update();
    }
}
