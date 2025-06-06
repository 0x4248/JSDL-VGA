package com.github._0x4248.JSDLVGA;

public class VgaGraphics {
    private final Display display;
    private final int[] palette = new int[256];

    public VgaGraphics(Display display) {
        this.display = display;
        initDefaultPalette();
    }

    // Set RGB values in the palette (0-255) for an 8-bit color index
    public void setPaletteColor(int index, int r, int g, int b) {
        if (index < 0 || index >= 256) return;
        palette[index] = (r & 0xFF) << 16 | (g & 0xFF) << 8 | (b & 0xFF);
    }

    // Set a pixel using 8-bit color index
    public void setPixel(int x, int y, int colorIndex) {
        if (colorIndex < 0 || colorIndex >= 256) return;

        int color = palette[colorIndex];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        display.setPixel(x, y, r, g, b);
    }

    // Optional: fill the screen with a VGA color index
    public void clear(int colorIndex) {
        int color = palette[colorIndex];
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        display.clear(r, g, b);
    }

    // Initializes a 16-color CGA/VGA-like palette
    private void initDefaultPalette() {
        int[] defaultColors = {
                0x000000, 0x0000AA, 0x00AA00, 0x00AAAA,
                0xAA0000, 0xAA00AA, 0xAA5500, 0xAAAAAA,
                0x555555, 0x5555FF, 0x55FF55, 0x55FFFF,
                0xFF5555, 0xFF55FF, 0xFFFF55, 0xFFFFFF
        };
        for (int i = 0; i < defaultColors.length; i++) {
            palette[i] = defaultColors[i];
        }
    }
}
