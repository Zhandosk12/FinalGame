package com.gdx.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class CameraManager {

    public OrthographicCamera createCamera() {
        int displayW = Gdx.graphics.getWidth();
        int displayH = Gdx.graphics.getHeight();

        int h = displayH /(displayH /160);
        int w = displayW /(displayH / h);

        OrthographicCamera camera = new OrthographicCamera(w, h);
        camera.zoom = .4f;
        return camera;
    }

    public ControlManager insertControl(OrthographicCamera camera) {
        ControlManager controlManager = new ControlManager((int) camera.viewportWidth, camera);
        Gdx.input.setInputProcessor(controlManager);
        return controlManager;
    }
}
