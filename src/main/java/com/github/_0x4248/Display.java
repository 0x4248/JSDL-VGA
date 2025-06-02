package com.github._0x4248;


import io.github.libsdl4j.api.event.SDL_Event;
import io.github.libsdl4j.api.render.SDL_Renderer;
import io.github.libsdl4j.api.video.SDL_Window;

import java.util.concurrent.ConcurrentLinkedQueue;

import static io.github.libsdl4j.api.Sdl.SDL_Init;
import static io.github.libsdl4j.api.Sdl.SDL_Quit;
import static io.github.libsdl4j.api.SdlSubSystemConst.SDL_INIT_EVERYTHING;
import static io.github.libsdl4j.api.error.SdlError.SDL_GetError;
import static io.github.libsdl4j.api.event.SDL_EventType.*;
import static io.github.libsdl4j.api.event.SdlEvents.SDL_PollEvent;
import static io.github.libsdl4j.api.hints.SdlHintsConst.SDL_HINT_RENDER_SCALE_QUALITY;
import static io.github.libsdl4j.api.hints.SdlHints.SDL_SetHint;
import static io.github.libsdl4j.api.render.SDL_RendererFlags.SDL_RENDERER_ACCELERATED;
import static io.github.libsdl4j.api.render.SdlRender.*;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_RESIZABLE;
import static io.github.libsdl4j.api.video.SDL_WindowFlags.SDL_WINDOW_SHOWN;
import static io.github.libsdl4j.api.video.SdlVideo.SDL_CreateWindow;
import static io.github.libsdl4j.api.video.SdlVideoConst.SDL_WINDOWPOS_CENTERED;

public class Display implements Runnable {

    private SDL_Window window;
    private SDL_Renderer renderer;
    private boolean running = true;

    public volatile boolean selfUpdating = false;

    private final ConcurrentLinkedQueue<Runnable> renderQueue = new ConcurrentLinkedQueue<>();

    public Display(int width, int height) {
        System.setProperty("jna.platform.library.path", "/opt/homebrew/lib/");
        if (SDL_Init(SDL_INIT_EVERYTHING) != 0) {
            throw new IllegalStateException("SDL_Init error: " + SDL_GetError());
        }

        SDL_SetHint(SDL_HINT_RENDER_SCALE_QUALITY, "0");

        window = SDL_CreateWindow("JVGA", SDL_WINDOWPOS_CENTERED, SDL_WINDOWPOS_CENTERED, width, height, SDL_WINDOW_SHOWN | SDL_WINDOW_RESIZABLE);
        if (window == null) throw new IllegalStateException("Window creation failed: " + SDL_GetError());

        renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
        if (renderer == null) throw new IllegalStateException("Renderer creation failed: " + SDL_GetError());

        SDL_RenderSetIntegerScale(renderer, true);
        SDL_RenderSetLogicalSize(renderer, width, height);
    }

    public void setPixel(int x, int y) {
        renderQueue.add(() -> {
            SDL_RenderDrawPoint(renderer, x, y);
        });
    }

    public void setColour(int r, int g, int b) {
        renderQueue.add(() -> {
           SDL_SetRenderDrawColor(renderer, (byte) r, (byte) g, (byte) b, (byte) 255);
        });
    }

    public void clear() {
        renderQueue.add(() -> {
            SDL_RenderClear(renderer);
        });
    }

    public void update() {
        renderQueue.add(() -> SDL_RenderPresent(renderer));
    }

    @Override
    public void run() {
        SDL_Event evt = new SDL_Event();
        long frameDelay = 1000 / 60;

        while (running) {
            while (SDL_PollEvent(evt) != 0) {
                if (evt.type == SDL_QUIT) {
                    running = false;  // <- triggers shutdown
                    break;
                }
            }

            Runnable task;
            while ((task = renderQueue.poll()) != null) {
                task.run();
            }

            if (selfUpdating) {
                SDL_RenderPresent(renderer);
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
}
