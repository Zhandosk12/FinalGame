package com.gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.gdx.game.GdxGame;
import com.gdx.game.Media;
import com.gdx.game.box2d.Box2dWorld;
import com.gdx.game.entities.Hero;
import com.gdx.game.manager.AnimationManager;
import com.gdx.game.manager.CameraManager;
import com.gdx.game.manager.ControlManager;

public class BattleScreen extends AbstractScreen {

    private final Box2dWorld box2d;
    private ControlManager controlManager;
    private Hero hero;
    private final AnimationManager animationManager = new AnimationManager();
    private TextureRegion[]  textureRegions;
    private final Texture battleBackground = Media.battleBackgroundMeadow;
    private final Stage battleStage;
    private float lifeTime;

    public BattleScreen(GdxGame gdxGame) {
        super(gdxGame);

        this.viewport = new StretchViewport(getBattleCam().viewportWidth, getBattleCam().viewportHeight, getBattleCam());
        battleStage = new Stage(viewport, gdxGame.getBatch());

        box2d = new Box2dWorld();
        handleEntities();
    }

    private void handleEntities() {
        if(gdxGame.getEntityMap() != null) {
            hero = (Hero) gdxGame.getEntityMap().get("hero");
            hero.setTexture(Media.heroWalkRight);
            textureRegions = animationManager.setTextureRegions(hero.getTexture(), 32, 37);
        }
    }

    @Override
    public void show() {
        CameraManager cameraManager = new CameraManager();
        controlManager = cameraManager.insertControl(getBattleCam());

        Gdx.input.setInputProcessor(battleStage);
    }

    @Override
    public void render(float delta) {
        gdxGame.getBatch().setProjectionMatrix(getBattleCam().combined);

        gdxGame.getBatch().begin();
        gdxGame.getBatch().draw(battleBackground, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if(textureRegions != null) {
            gdxGame.getBatch().draw(textureRegions[1], 150, 175, textureRegions[1].getRegionWidth()*3, textureRegions[1].getRegionHeight()*3);
        }

        gdxGame.getBatch().end();
        lifeTime += Gdx.graphics.getDeltaTime();
        long delay = 3L;
        if (lifeTime > delay) {
            hero.collision(hero.getEntityCollision(), false);
            gdxGame.setScreen(gdxGame.getGameScreen());
        }

        box2d.tick(getBattleCam(), controlManager);
    }
}
