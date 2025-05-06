package com.gdx.game.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import static com.badlogic.gdx.Input.Keys;

public class ControlManager extends InputAdapter implements InputProcessor {
    private final OrthographicCamera camera;

    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;
    private final Vector2 mouseClickPos = new Vector2();
    private final Vector2 mapClickPos = new Vector2();
    private boolean debug;

    private int screenHeight;

    public boolean isUp() {
        return up;
    }

    public boolean isDown() {
        return down;
    }

    public boolean isLeft() {
        return left;
    }

    public boolean isRight() {
        return right;
    }

    public boolean isDebug() {
        return debug;
    }

    public ControlManager(int screenWidth, OrthographicCamera camera) {
        this.camera = camera;
    }

    private void setMouseClickedPos(int screenX, int screenY) {
        mouseClickPos.set(screenX, screenHeight - screenY);
        mapClickPos.set(getMapCoords(mouseClickPos));
    }

    public Vector2 getMapCoords(Vector2 mouseCoords) {
        Vector3 v3 = new Vector3(mouseCoords.x, screenHeight - mouseCoords.y, 0);
        this.camera.unproject(v3);
        return new Vector2(v3.x, v3.y);
    }

    @Override
    public boolean keyDown(int keycode) {
        switch (keycode) {
            case Keys.DOWN:
            case Keys.S:
                down = true;
                break;
            case Keys.UP:
            case Keys.W:
                up = true;
                break;
            case Keys.LEFT:
            case Keys.A:
                left = true;
                break;
            case Keys.RIGHT:
            case Keys.D:
                right = true;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Keys.DOWN:
            case Keys.S:
                down = false;
                break;
            case Keys.UP:
            case Keys.W:
                up = false;
                break;
            case Keys.LEFT:
            case Keys.A:
                left = false;
                break;
            case Keys.RIGHT:
            case Keys.D:
                right = false;
                break;
            case Keys.ESCAPE:
                Gdx.app.exit();
                break;
            case Keys.BACKSPACE:
                debug = !debug;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        setMouseClickedPos(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {

        setMouseClickedPos(screenX, screenY);
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        setMouseClickedPos(screenX, screenY);
        return false;
    }
}
