package com.gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ScreenUtils;
import com.gdx.game.GdxGame;
import com.gdx.game.audio.AudioManager;
import com.gdx.game.audio.AudioObserver;
import com.gdx.game.component.Component;
import com.gdx.game.entities.Entity;
import com.gdx.game.entities.EntityFactory;
import com.gdx.game.entities.player.PlayerInputComponent;
import com.gdx.game.manager.ResourceManager;
import com.gdx.game.map.Map;
import com.gdx.game.map.MapFactory;
import com.gdx.game.map.MapManager;
import com.gdx.game.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameScreen extends BaseScreen {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameScreen.class);

    public static class VIEWPORT {
        private static float viewportWidth;
        private static float viewportHeight;
        private static float virtualWidth;
        private static float virtualHeight;
        private static float physicalWidth;
        private static float physicalHeight;
        private static float aspectRatio;
    }

    public enum GameState {
        SAVING,
        LOADING,
        RUNNING,
        PAUSED,
        GAME_OVER
    }

    private static GameState gameState;

    protected OrthogonalTiledMapRenderer mapRenderer = null;
    protected MapManager mapManager;
    protected OrthographicCamera camera;
    private final Stage gameStage = new Stage();

    private final Json json;
    private final InputMultiplexer multiplexer;

    private final Entity player;

    private AudioObserver.AudioTypeEvent musicTheme;

    public GameScreen(GdxGame game, ResourceManager resourceManager) {
        super(game, resourceManager);
        mapManager = new MapManager();
        json = new Json();

        setGameState(GameState.RUNNING);
        setupViewport();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);

        player = EntityFactory.getEntity(EntityFactory.EntityType.PLAYER);
        mapManager.setPlayer(player);
        mapManager.setCamera(camera);

        multiplexer = new InputMultiplexer();
        assert player != null;
        multiplexer.addProcessor(player.getInputProcessor());
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public AudioObserver.AudioTypeEvent getMusicTheme() {
        return musicTheme;
    }

    @Override
    public void show() {
        ProfileManager.getInstance().addObserver(mapManager);

        setGameState(GameState.RUNNING);
        Gdx.input.setInputProcessor(multiplexer);

        if (mapRenderer == null) {
            mapRenderer = new OrthogonalTiledMapRenderer(mapManager.getCurrentTiledMap(), Map.UNIT_SCALE);
        }
    }

    @Override
    public void hide() {
        if (gameState != GameState.GAME_OVER) {
            setGameState(GameState.SAVING);
        }

        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {

        if (gameState == GameState.PAUSED) {
            player.updateInput(delta);
            return;
        }
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mapRenderer.setView(camera);

        mapRenderer.getBatch().enableBlending();
        mapRenderer.getBatch().setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        if (mapManager.hasMapChanged()) {
            mapRenderer.setMap(mapManager.getCurrentTiledMap());
            player.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(mapManager.getPlayerStartUnitScaled()));

            camera.position.set(mapManager.getPlayerStartUnitScaled().x, mapManager.getPlayerStartUnitScaled().y, 0f);
            camera.update();

            mapManager.setMapChanged(false);
        }

        mapRenderer.render();
        mapManager.updateCurrentMapEntities(mapManager, mapRenderer.getBatch(), delta);
        player.update(mapManager, mapRenderer.getBatch(), delta);

        if (((PlayerInputComponent) player.getInputProcessor()).isOption()) {
            Image screenShot = new Image(ScreenUtils.getFrameBufferTexture());
            gdxGame.setScreen(new OptionScreen(gdxGame, (BaseScreen) gdxGame.getScreen(), screenShot, resourceManager));
            ((PlayerInputComponent) player.getInputProcessor()).setOption(false);
        }

        musicTheme = MapFactory.getMapTable().get(mapManager.getCurrentMapType()).getMusicTheme();
        AudioManager.getInstance().setCurrentMusic(ResourceManager.getMusicAsset(musicTheme.getValue()));
    }

    @Override
    public void resize(int width, int height) {
        setupViewport();
        camera.setToOrtho(false, VIEWPORT.viewportWidth, VIEWPORT.viewportHeight);
    }

    @Override
    public void pause() {
        setGameState(GameState.SAVING);
    }

    @Override
    public void resume() {
        setGameState(GameState.LOADING);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (player != null) {
            player.unregisterObservers();
            player.dispose();
        }

        if (mapRenderer != null) {
            mapRenderer.dispose();
        }

        AudioManager.getInstance().dispose();
        MapFactory.clearCache();
    }

    public static void setGameState(GameState gameState) {
        switch (gameState) {
            case RUNNING:
                GameScreen.gameState = GameState.RUNNING;
                break;
            case LOADING:
                ProfileManager.getInstance().loadProfile();
                GameScreen.gameState = GameState.RUNNING;
                break;
            case SAVING:
                ProfileManager.getInstance().saveProfile();
                GameScreen.gameState = GameState.PAUSED;
                break;
            case PAUSED:
                if (GameScreen.gameState == GameState.PAUSED) {
                    GameScreen.gameState = GameState.RUNNING;
                } else if (GameScreen.gameState == GameState.RUNNING) {
                    GameScreen.gameState = GameState.PAUSED;
                }
                break;
            case GAME_OVER:
                GameScreen.gameState = GameState.GAME_OVER;
                break;
            default:
                GameScreen.gameState = GameState.RUNNING;
                break;
        }

    }

    private static void setupViewport() {
        VIEWPORT.virtualWidth = 15;
        VIEWPORT.virtualHeight = 15;
        VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
        VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
        VIEWPORT.physicalWidth = Gdx.graphics.getWidth();
        VIEWPORT.physicalHeight = Gdx.graphics.getHeight();
        VIEWPORT.aspectRatio = (VIEWPORT.virtualWidth / VIEWPORT.virtualHeight);
        if (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight >= VIEWPORT.aspectRatio) {
            VIEWPORT.viewportWidth = VIEWPORT.viewportHeight * (VIEWPORT.physicalWidth / VIEWPORT.physicalHeight);
            VIEWPORT.viewportHeight = VIEWPORT.virtualHeight;
        } else {
            VIEWPORT.viewportWidth = VIEWPORT.virtualWidth;
            VIEWPORT.viewportHeight = VIEWPORT.viewportWidth * (VIEWPORT.physicalHeight / VIEWPORT.physicalWidth);
        }

        LOGGER.debug("WorldRenderer: virtual: (" + VIEWPORT.virtualWidth + "," + VIEWPORT.virtualHeight + ")");
        LOGGER.debug("WorldRenderer: viewport: (" + VIEWPORT.viewportWidth + "," + VIEWPORT.viewportHeight + ")");
        LOGGER.debug("WorldRenderer: physical: (" + VIEWPORT.physicalWidth + "," + VIEWPORT.physicalHeight + ")");
    }
}
