package com.github._0x4248.JSDLVGA;


import java.util.concurrent.ConcurrentLinkedQueue;


import com.sun.jna.Memory;
import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.video.SDL_Window;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.render.SDL_Texture;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.hints.SdlHints.SDL_SetHint;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;

import static io.github.libsdl4j.api.video.SdlVideo.*;
import static io.github.libsdl4j.api.render.SdlRender.*;

import static io.github.libsdl4j.api.event.SDL_EventType.SDL_QUIT;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.render.SDL_RendererFlags.SDL_RENDERER_ACCELERATED;
import static io.github.libsdl4j.api.hints.SdlHintsConst.SDL_HINT_RENDER_SCALE_QUALITY;
import static io.github.libsdl4j.api.pixels.SDL_PixelFormatEnum.SDL_PIXELFORMAT_ARGB8888;
import static io.github.libsdl4j.api.render.SDL_TextureAccess.SDL_TEXTUREACCESS_STREAMING;


public class Display implements Runnable {
    private SDL_Window window;
    private SDL_Renderer renderer;
    private SDL_Texture framebufferTexture;
    private Memory framebuffer;
    private final int width, height;
    private boolean running = true;

    public volatile boolean selfUpdating = false;
    private final ConcurrentLinkedQueue<Runnable> renderQueue = new ConcurrentLinkedQueue<>();

    public Display(int width, int height) {
        this.width = width;
        this.height = height;

        System.setProperty("jna.platform.library.path", "/opt/homebrew/lib/");

        if (SDL_Init(SDL_INIT_EVERYTHING) != 0) {
            throw new IllegalStateException("SDL_Init error: " + SDL_GetError());
        }

        SDL_SetHint(SDL_HINT_RENDER_SCALE_QUALITY, "0");

        window = SDL_CreateWindow("JVGA", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, width, height,
                SDL_WINDOW_SHOWN | SDL_WINDOW_RESIZABLE);
        if (window == null) {
            throw new IllegalStateException("Window creation failed: " + SDL_GetError());
        }

        renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
        if (renderer == null) {
            throw new IllegalStateException("Renderer creation failed: " + SDL_GetError());
        }

        SDL_RenderSetLogicalSize(renderer, width, height);
        SDL_RenderSetIntegerScale(renderer, true);

        framebufferTexture = SDL_CreateTexture(renderer,
                SDL_PIXELFORMAT_ARGB8888,
                SDL_TEXTUREACCESS_STREAMING,
                width, height);
        if (framebufferTexture == null) {
            throw new IllegalStateException("Texture creation failed: " + SDL_GetError());
        }

        framebuffer = new com.sun.jna.Memory(width * height * 4L); // 4 bytes per pixel (ARGB)
        clear(0,0,0);

    }

    public void setPixel(int x, int y, int r, int g, int b) {
        if (x < 0 || y < 0 || x >= width || y >= height) return;
        long offset = (long)(y * width + x) * 4;
        framebuffer.setByte(offset, (byte) b); // Blue
        framebuffer.setByte(offset + 1, (byte) g); // Green
        framebuffer.setByte(offset + 2, (byte) r); // Red
        framebuffer.setByte(offset + 3, (byte) 255); // Alpha
    }


    public void clear(int r, int g, int b) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                setPixel(x, y, r, g, b);
            }
        }
    }

    public void update() {
        renderQueue.add(() -> {
            SDL_UpdateTexture(framebufferTexture, null, framebuffer, width * 4);
            SDL_RenderClear(renderer);
            SDL_RenderCopy(renderer, framebufferTexture, null, null);
            SDL_RenderPresent(renderer);
        });
    }

    @Override
    public void run() {
        SDL_Event evt = new SDL_Event();
        long frameDelay = 1000 / 60;

        while (running) {
            while (SDL_PollEvent(evt) != 0) {
                if (evt.type == SDL_QUIT) {
                    running = false;
                    break;
                }
            }

            Runnable task;
            while ((task = renderQueue.poll()) != null) {
                task.run();
            }

            if (selfUpdating) {
                update(); // auto-present the buffer
                try {
                    Thread.sleep(frameDelay);
                } catch (InterruptedException ignored) {
                }
            }
        }

        SDL_Quit();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        this.running = false;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
