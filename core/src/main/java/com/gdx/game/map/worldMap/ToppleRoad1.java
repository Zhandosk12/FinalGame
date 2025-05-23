package com.gdx.game.map.worldMap;

import com.badlogic.gdx.math.Vector2;
import com.gdx.game.audio.AudioObserver;
import com.gdx.game.component.Component;
import com.gdx.game.entities.Entity;
import com.gdx.game.entities.EntityConfig;
import com.gdx.game.entities.EntityFactory;
import com.gdx.game.map.Map;
import com.gdx.game.map.MapFactory;
import com.gdx.game.profile.ProfileManager;

import static com.gdx.game.audio.AudioObserver.AudioTypeEvent.TEST_THEME;

public class ToppleRoad1 extends Map {

    private static final String mapPath = "asset/map/Topple_Road_1.tmx";

    public ToppleRoad1() {
        super(MapFactory.MapType.TOPPLE_ROAD_1, mapPath);

        Entity rabite = EntityFactory.getInstance().getEntityByName(EntityFactory.EntityName.RABITE);
        initSpecialEntityPosition(rabite);
        mapEntities.add(rabite);

    }

    @Override
    public AudioObserver.AudioTypeEvent getMusicTheme() {
        return TEST_THEME;
    }

    @Override
    public void unloadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_STOP, TEST_THEME);
    }

    @Override
    public void loadMusic() {
        notify(AudioObserver.AudioCommand.MUSIC_LOAD, TEST_THEME);
        notify(AudioObserver.AudioCommand.MUSIC_PLAY_LOOP, TEST_THEME);
    }

    private void initSpecialEntityPosition(Entity entity) {
        Vector2 position = new Vector2(0,0);

        if(enemyStartPositions.containsKey(entity.getEntityConfig().getEntityID())) {
            position = enemyStartPositions.get(entity.getEntityConfig().getEntityID());
        }
        entity.sendMessage(Component.MESSAGE.INIT_START_POSITION, json.toJson(position));
        EntityConfig entityConfig = ProfileManager.getInstance().getProperty(entity.getEntityConfig().getEntityID(), EntityConfig.class);
        if(entityConfig != null ) {
            entity.setEntityConfig(entityConfig);
        }
    }

    @Override
    public void removeObserver(AudioObserver audioObserver) {

    }

    @Override
    public void removeAllObservers() {

    }
}
