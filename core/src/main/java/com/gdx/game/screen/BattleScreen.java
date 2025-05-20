package com.gdx.game.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.MoveToAction;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.gdx.game.GdxGame;
import com.gdx.game.animation.AnimatedImage;
import com.gdx.game.audio.AudioObserver;
import com.gdx.game.battle.BattleObserver;
import com.gdx.game.battle.BattleState;
import com.gdx.game.battle.BattleUI;
import com.gdx.game.entities.Entity;
import com.gdx.game.entities.EntityConfig;
import com.gdx.game.entities.EntityFactory;
import com.gdx.game.entities.player.PlayerHUD;
import com.gdx.game.manager.ResourceManager;
import com.gdx.game.map.MapManager;
import com.gdx.game.profile.ProfileManager;
import com.gdx.game.screen.transition.effects.FadeOutTransitionEffect;
import com.gdx.game.screen.transition.effects.TransitionEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static com.gdx.game.audio.AudioObserver.AudioTypeEvent.BATTLE_THEME;

public class BattleScreen extends BaseScreen implements BattleObserver {

    private static final Logger LOGGER = LoggerFactory.getLogger(BattleScreen.class);

    private TextureRegion[]  textureRegions;
    private final OrthographicCamera camera;
    private final Stage battleStage;
    private final MapManager mapManager;
    private final AnimatedImage playerImage;
    private final Entity player;
    private final AnimatedImage opponentImage;
    private final Entity enemy;
    private final BattleUI battleUI;
    private final PlayerHUD playerHUD;
    private final int enemyWidth = 50;
    private final int enemyHeight = 50;
    private final int playerWidth = 50;
    private final int playerHeight = 50;

    private final BattleState battleState;
    private final Label dmgPlayerValLabel;
    private final Label dmgOpponentValLabel;

    private final float battleTimer = 0;
    private final float checkTimer = 1;

    /*private ShakeCamera battleShakeCam = null;
    private Array<ParticleEffect> effects;*/

    private float origDmgPlayerValLabelY = 0;
    private float origDmgOpponentValLabelY = 0;
    private final Table dmgPlayerLabelTable = new Table();
    private final Table dmgOpponentLabelTable = new Table();
    private final Vector2 currentOpponentImagePosition = new Vector2(0,0);
    private final Vector2 currentPlayerImagePosition = new Vector2(0,0);
    private Vector2 playerPosition;

    public BattleScreen(GdxGame gdxGame, PlayerHUD playerHUD, MapManager mapManager, ResourceManager resourceManager) {
        super(gdxGame, resourceManager);
        super.musicTheme = BATTLE_THEME;
        this.mapManager = mapManager;
        this.playerHUD = playerHUD;

        battleState = new BattleState();
        battleState.addObserver(this);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        viewport = new StretchViewport(camera.viewportWidth, camera.viewportHeight, camera);
        battleStage = new Stage(viewport, gdxGame.getBatch());

        player = mapManager.getPlayer();
        playerImage = new AnimatedImage();
        playerImage.setTouchable(Touchable.disabled);
        enemy = EntityFactory.getInstance().getEntityByName(mapManager.getPlayer().getEntityEncounteredType());
        opponentImage = new AnimatedImage();
        opponentImage.setTouchable(Touchable.disabled);
        battleState.setPlayer(player);
        battleState.setCurrentOpponent(enemy);

        dmgPlayerValLabel = new Label("0", ResourceManager.skin);
        dmgPlayerValLabel.setVisible(false);
        origDmgPlayerValLabelY = dmgPlayerValLabel.getY() + playerHeight;
        dmgOpponentValLabel = new Label("0", ResourceManager.skin);
        dmgOpponentValLabel.setVisible(false);
        origDmgOpponentValLabelY = dmgOpponentValLabel.getY() + enemyHeight;

        battleUI = new BattleUI(battleState);
        battleUI.setMovable(false);
        battleUI.setVisible(true);
        battleUI.setKeepWithinStage(false);
        battleUI.setPosition(battleStage.getWidth() / 10, 2 * battleStage.getHeight() / 3);
        battleUI.setWidth(battleStage.getWidth() / 2);
        battleUI.setHeight(battleStage.getHeight() / 4);

        battleUI.validate();

        dmgPlayerLabelTable.add(dmgPlayerValLabel).padLeft(playerWidth / 2).padBottom(playerHeight * 4);
        dmgPlayerLabelTable.setPosition(currentPlayerImagePosition.x, currentPlayerImagePosition.y);
        dmgOpponentLabelTable.add(dmgOpponentValLabel).padLeft(enemyWidth / 2).padBottom(enemyHeight * 4);
        dmgOpponentLabelTable.setPosition(currentOpponentImagePosition.x, currentOpponentImagePosition.y);
    }

    public BattleState getCurrentState(){
        return battleState;
    }

    private void removeEntities() {
        Array<Entity> entities = mapManager.getCurrentMapEntities();
        for(Entity entity: entities) {
            if(entity.getEntityConfig().getEntityID().equals(mapManager.getPlayer().getEntityEncounteredType().toString())) {
                mapManager.removeMapEntity(entity);
            }
        }
        mapManager.getPlayer().setEntityEncounteredType(null);
    }

    private void setupGameOver() {
        dmgOpponentValLabel.setVisible(false);
        dmgPlayerValLabel.setVisible(false);
        battleUI.setVisible(false);
        ArrayList<TransitionEffect> effects = new ArrayList<>();
        effects.add(new FadeOutTransitionEffect(1f));
        setScreenWithTransition((BaseScreen) gdxGame.getScreen(), new GameOverScreen(gdxGame, mapManager, resourceManager), effects);
    }

    @Override
    public void onNotify(Entity entity, BattleEvent event) {
        switch(event) {
            case PLAYER_TURN_START:
                LOGGER.debug("Player turn start");
                battleUI.setVisible(false);
                battleUI.setTouchable(Touchable.disabled);
                break;
            case PLAYER_ADDED:
                playerImage.setEntity(entity);
                playerImage.setCurrentAnimation(Entity.AnimationType.WALK_RIGHT);
                playerImage.setSize(playerWidth, playerHeight);
                playerImage.setPosition(0, 200);
                playerImage.addAction(Actions.moveTo(200, 200, 2));

                currentPlayerImagePosition.set(((MoveToAction) playerImage.getActions().get(0)).getX(), playerImage.getY());
                LOGGER.debug("Player added on battle map");
                break;
            case OPPONENT_ADDED:
                opponentImage.setEntity(entity);
                opponentImage.setCurrentAnimation(Entity.AnimationType.IMMOBILE);
                opponentImage.setSize(enemyWidth, enemyHeight);
                opponentImage.setPosition(600, 200);

                currentOpponentImagePosition.set(opponentImage.getX(), opponentImage.getY());
                LOGGER.debug("Opponent added on battle map");
                /*if( battleShakeCam == null ){
                    battleShakeCam = new ShakeCamera(currentImagePosition.x, currentImagePosition.y, 30.0f);
                }*/

                //Gdx.app.debug(TAG, "Image position: " + _image.getX() + "," + _image.getY() );

                //this.getTitleLabel().setText("Level " + battleState.getCurrentZoneLevel() + " " + entity.getEntityConfig().getEntityID());
                break;
            case PLAYER_HIT_DAMAGE:
                int damagePlayer = Integer.parseInt(player.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ENTITY_HIT_DAMAGE_TOTAL.toString()));
                dmgPlayerValLabel.setText(String.valueOf(damagePlayer));
                dmgPlayerValLabel.setY(origDmgPlayerValLabelY);
                //battleShakeCam.startShaking();
                dmgPlayerValLabel.setVisible(true);
                playerHUD.onNotify(player, BattleEvent.PLAYER_HIT_DAMAGE);
                break;
            case OPPONENT_HIT_DAMAGE:
                int damageEnemy = Integer.parseInt(entity.getEntityConfig().getPropertyValue(EntityConfig.EntityProperties.ENTITY_HIT_DAMAGE_TOTAL.toString()));
                dmgOpponentValLabel.setText(String.valueOf(damageEnemy));
                dmgOpponentValLabel.setY(origDmgOpponentValLabelY);
                //battleShakeCam.startShaking();
                dmgOpponentValLabel.setVisible(true);
                LOGGER.debug("Opponent deals " + damageEnemy + " damages");
                break;
            case OPPONENT_DEFEATED:
                dmgOpponentValLabel.setVisible(false);
                dmgOpponentValLabel.setY(origDmgOpponentValLabelY);
                //opponentImage.setVisible(false);
                playerHUD.onNotify(enemy, BattleEvent.OPPONENT_DEFEATED);
                LOGGER.debug("Opponent is defeated");

                ProfileManager.getInstance().saveProfile();
                setScreenWithTransition((BaseScreen) gdxGame.getScreen(), gdxGame.getGameScreen(), new ArrayList<>());
                removeEntities();
                break;
            case OPPONENT_TURN_DONE:
                battleUI.setVisible(true);
                battleUI.setTouchable(Touchable.enabled);
                LOGGER.debug("Opponent turn done");

                if(GameScreen.getGameState() == GameScreen.GameState.GAME_OVER) {
                    setupGameOver();
                }
                break;
            case PLAYER_TURN_DONE:
                battleState.opponentAttacks();
                LOGGER.debug("Player turn done");
                break;
            /*case PLAYER_USED_MAGIC:
                float x = currentImagePosition.x + (enemyWidth/2);
                float y = currentImagePosition.y + (enemyHeight/2);
                //effects.add(ParticleEffectFactory.getParticleEffect(ParticleEffectFactory.ParticleEffectType.WAND_ATTACK, x,y));
                break;*/
            default:
                break;
        }
    }

    @Override
    public void show() {
        battleStage.addActor(opponentImage);
        battleStage.addActor(playerImage);
        battleStage.addActor(battleUI);
        battleStage.addActor(dmgPlayerLabelTable);
        battleStage.addActor(dmgOpponentLabelTable);
        Gdx.input.setInputProcessor(battleStage);

        notify(AudioObserver.AudioCommand.MUSIC_LOAD, BATTLE_THEME);
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, BATTLE_THEME);
    }

    @Override
    public void render(float delta) {
        gdxGame.getBatch().setProjectionMatrix(camera.combined);

        gdxGame.getBatch().begin();
        gdxGame.getBatch().draw(resourceManager.battleBackgroundMeadow, 0,0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if(textureRegions != null) {
            gdxGame.getBatch().draw(textureRegions[1], 150, 175, textureRegions[1].getRegionWidth()*3f, textureRegions[1].getRegionHeight()*3f);
        }
        if(dmgPlayerValLabel.isVisible() && dmgPlayerValLabel.getY() < this.battleStage.getHeight()) {
            dmgPlayerValLabel.setY(dmgPlayerValLabel.getY()+5);
        }
        if(dmgOpponentValLabel.isVisible() && dmgOpponentValLabel.getY() < this.battleStage.getHeight()) {
            dmgOpponentValLabel.setY(dmgOpponentValLabel.getY()+5);
        }

        gdxGame.getBatch().end();
        battleStage.act(Gdx.graphics.getDeltaTime());
        battleStage.draw();

        if (playerImage.getActions().size == 0 && !playerImage.getCurrentAnimationType().equals(Entity.AnimationType.LOOK_RIGHT)) {
            playerImage.setCurrentAnimation(Entity.AnimationType.LOOK_RIGHT);
        }

        //box2d.tick(getBattleCam(), controlManager);
    }

    @Override
    public void dispose() {
        super.dispose();
        battleUI.remove();
        dmgPlayerLabelTable.remove();
        dmgOpponentLabelTable.remove();
        playerImage.remove();
        opponentImage.remove();
    }
}
