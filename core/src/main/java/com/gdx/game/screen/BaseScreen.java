package com.gdx.game.screen;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.gdx.game.GdxGame;
import com.gdx.game.audio.AudioManager;
import com.gdx.game.audio.AudioObserver;
import com.gdx.game.audio.AudioSubject;
import com.gdx.game.manager.ResourceManager;
import com.gdx.game.screen.transition.effects.TransitionEffect;

import java.util.ArrayList;
import java.util.List;

public class BaseScreen implements Screen, AudioSubject {
    protected final GdxGame gdxGame;
    protected ResourceManager resourceManager;
    protected OrthographicCamera gameCam;
    protected OrthographicCamera battleCam;
    protected Viewport viewport;
    protected Stage stage;
    protected AudioObserver.AudioTypeEvent musicTheme;

    private final Array<AudioObserver> observers;

    public BaseScreen(GdxGame gdxGame, ResourceManager resourceManager) {
        this.gdxGame = gdxGame;
        this.resourceManager = resourceManager;

        observers = new Array<>();
        this.addObserver(AudioManager.getInstance());
    }

    public AudioObserver.AudioTypeEvent getMusicTheme() {
        return musicTheme;
    }

    public void setScreenWithTransition(BaseScreen current, BaseScreen next, List<TransitionEffect> transitionEffect) {
        ArrayList<TransitionEffect> effects = new ArrayList<>(transitionEffect);

        Screen transitionScreen = new TransitionScreen(gdxGame, current, next, effects);
        gdxGame.setScreen(transitionScreen);
    }

    public void createButton(String buttonName, float posX, float posY, Table table) {
        TextureRegion[][] playButtons = resourceManager.button;

        BitmapFont pixel10 = resourceManager.pixel10;

        TextureRegionDrawable imageUp = new TextureRegionDrawable(playButtons[0][0]);
        TextureRegionDrawable imageDown = new TextureRegionDrawable(playButtons[1][0]);

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle(imageUp, imageDown, null, pixel10);
        TextButton button = new TextButton(buttonName, buttonStyle);
        button.getLabel().setColor(new Color(79 / 255.f, 79 / 255.f, 117 / 255.f, 1));

        table.add(button).padLeft(posX).padTop(posY);
        table.row();
    }

    @Override
    public void addObserver(AudioObserver audioObserver) {
        observers.add(audioObserver);
    }


    @Override
    public void notify(AudioObserver.AudioCommand command, AudioObserver.AudioTypeEvent event) {
        for(AudioObserver observer: observers){
            observer.onNotify(command, event);
        }
    }

    @Override
    public void show() {
        // Nothing
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // Nothing
    }

    @Override
    public void pause() {
        // Nothing
    }

    @Override
    public void resume() {
        // Nothing
    }

    @Override
    public void hide() {
        // Nothing
    }

    @Override
    public void dispose() {
        //stage.dispose();
    }

    public OrthographicCamera getGameCam() {
        return gameCam;
    }

    public OrthographicCamera getBattleCam() {
        return battleCam;
    }

    public Stage getStage() {
        return stage;
    }
}
