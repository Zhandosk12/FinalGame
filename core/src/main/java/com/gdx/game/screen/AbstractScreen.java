package com.gdx.game.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.gdx.game.GdxGame;
import com.gdx.game.manager.CameraManager;

public class AbstractScreen implements Screen {
    protected final GdxGame gdxGame;
    protected OrthographicCamera cam;
    protected Viewport viewport;
    protected Stage stage;

    public AbstractScreen(GdxGame gdxGame) {
        this.gdxGame = gdxGame;

        CameraManager cameraManager = new CameraManager();
        cam = cameraManager.createCamera();
        viewport = new StretchViewport(cam.viewportWidth, cam.viewportHeight, cam);
        stage = new Stage(viewport, gdxGame.getBatch());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    public OrthographicCamera getCam() {
        return cam;
    }

}
