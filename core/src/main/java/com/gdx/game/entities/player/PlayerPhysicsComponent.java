package com.gdx.game.entities.player;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import com.gdx.game.component.Component;
import com.gdx.game.component.ComponentObserver;
import com.gdx.game.component.PhysicsComponent;
import com.gdx.game.entities.Entity;
import com.gdx.game.map.Map;
import com.gdx.game.map.MapFactory;
import com.gdx.game.map.MapManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerPhysicsComponent extends PhysicsComponent {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerPhysicsComponent.class);

    private Entity.State state;
    private Vector3 mouseSelectCoordinates;
    private boolean isMouseSelectEnabled = false;
    private String previousDiscovery;
    private String previousEnemySpawn;

    public PlayerPhysicsComponent() {
        super.velocity = new Vector2(5f, 5f);
        boundingBoxLocation = PhysicsComponent.BoundingBoxLocation.BOTTOM_CENTER;
        initBoundingBox(0.3f, 0.5f);
        previousDiscovery = "";
        previousEnemySpawn = "0";

        mouseSelectCoordinates = new Vector3(0,0,0);
    }

    @Override
    public void dispose() {
    }

    @Override
    public void receiveMessage(String message) {
        String[] string = message.split(MESSAGE_TOKEN);

        if(string.length == 0) {
            return;
        }
        if(string.length == 2) {
            if(string[0].equalsIgnoreCase(Component.MESSAGE.INIT_START_POSITION.toString())) {
                currentEntityPosition = json.fromJson(Vector2.class, string[1]);
                nextEntityPosition.set(currentEntityPosition.x, currentEntityPosition.y);
                previousDiscovery = "";
                previousEnemySpawn = "0";
                notify(previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
            } else if(string[0].equalsIgnoreCase(Component.MESSAGE.CURRENT_STATE.toString())) {
                state = json.fromJson(Entity.State.class, string[1]);
            } else if(string[0].equalsIgnoreCase(Component.MESSAGE.CURRENT_DIRECTION.toString())) {
                currentDirection = json.fromJson(Entity.Direction.class, string[1]);
            } else if(string[0].equalsIgnoreCase(Component.MESSAGE.INIT_SELECT_ENTITY.toString())) {
                mouseSelectCoordinates = json.fromJson(Vector3.class, string[1]);
                isMouseSelectEnabled = true;
            }
        }
    }

    @Override
    public void update(Entity entity, MapManager mapMgr, float delta) {
        updateBoundingBoxPosition(nextEntityPosition);
        updatePortalLayerActivation(mapMgr);
        updateDiscoverLayerActivation(mapMgr);
        updateEnemySpawnLayerActivation(mapMgr);

        if(isMouseSelectEnabled) {
            selectMapEntityCandidate(mapMgr);
            isMouseSelectEnabled = false;
        }

        if(!isCollisionWithMapLayer(entity, mapMgr) && isCollisionWithMapEntities(entity, mapMgr) && state == Entity.State.WALKING) {
            setNextPositionToCurrent(entity);

            Camera camera = mapMgr.getCamera();
            camera.position.set(currentEntityPosition.x, currentEntityPosition.y, 0f);
            camera.update();
        } else {
            updateBoundingBoxPosition(currentEntityPosition);
        }

        calculateNextPosition(delta);
    }

    private void selectMapEntityCandidate(MapManager mapMgr) {
        tempEntities.clear();
        tempEntities.addAll(mapMgr.getCurrentMapEntities());
        tempEntities.addAll(mapMgr.getCurrentMapQuestEntities());
        mapMgr.getCamera().unproject(mouseSelectCoordinates);
        mouseSelectCoordinates.x /= Map.UNIT_SCALE;
        mouseSelectCoordinates.y /= Map.UNIT_SCALE;

        for(Entity mapEntity : tempEntities) {
            mapEntity.sendMessage(Component.MESSAGE.ENTITY_DESELECTED);
            Rectangle mapEntityBoundingBox = mapEntity.getCurrentBoundingBox();
            if(mapEntity.getCurrentBoundingBox().contains(mouseSelectCoordinates.x, mouseSelectCoordinates.y)) {
                selectionRay.set(boundingBox.x, boundingBox.y, 0.0f, mapEntityBoundingBox.x, mapEntityBoundingBox.y, 0.0f);
                float distance =  selectionRay.origin.dst(selectionRay.direction);

                if(distance <= SELECT_RAY_MAXIMUM_DISTANCE) {
                    LOGGER.debug("Selected Entity! " + mapEntity.getEntityConfig().getEntityID());
                    mapEntity.sendMessage(Component.MESSAGE.ENTITY_SELECTED);
                    notify(json.toJson(mapEntity.getEntityConfig()), ComponentObserver.ComponentEvent.LOAD_CONVERSATION);
                }
            }
        }
        tempEntities.clear();
    }

    private void updateDiscoverLayerActivation(MapManager mapMgr) {
        MapLayer mapDiscoverLayer =  mapMgr.getQuestDiscoverLayer();

        if(mapDiscoverLayer == null) {
            return;
        }

        Rectangle rectangle;

        for(MapObject object: mapDiscoverLayer.getObjects()) {
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if(boundingBox.overlaps(rectangle)) {
                    String questID = object.getName();
                    String questTaskID = (String)object.getProperties().get("taskID");
                    String val = questID + MESSAGE_TOKEN + questTaskID;

                    if(questID == null) {
                        return;
                    }

                    if(previousDiscovery.equalsIgnoreCase(val)) {
                        return;
                    } else {
                        previousDiscovery = val;
                    }

                    notify(json.toJson(val), ComponentObserver.ComponentEvent.QUEST_LOCATION_DISCOVERED);
                    LOGGER.debug("Discover Area Activated");
                    return;
                }
            }
        }
    }

    private void updateEnemySpawnLayerActivation(MapManager mapMgr) {
        MapLayer mapEnemySpawnLayer =  mapMgr.getEnemySpawnLayer();

        if(mapEnemySpawnLayer == null) {
            return;
        }

        Rectangle rectangle;

        for(MapObject object: mapEnemySpawnLayer.getObjects()) {
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if(boundingBox.overlaps(rectangle)) {
                    String enemySpawnID = object.getName();

                    if(enemySpawnID == null) {
                        return;
                    }

                    if(previousEnemySpawn.equalsIgnoreCase(enemySpawnID)) {
                        return;
                    } else {
                        LOGGER.debug("Enemy Spawn Area " + enemySpawnID + " Activated with previous Spawn value: " + previousEnemySpawn);
                        previousEnemySpawn = enemySpawnID;
                    }

                    notify(enemySpawnID, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
                    return;
                }
            }
        }

        //If no collision, reset the value
        if(!previousEnemySpawn.equalsIgnoreCase(String.valueOf(0))) {
            LOGGER.debug("Enemy Spawn Area RESET with previous value " + previousEnemySpawn);
            previousEnemySpawn = String.valueOf(0);
            notify(previousEnemySpawn, ComponentObserver.ComponentEvent.ENEMY_SPAWN_LOCATION_CHANGED);
        }

    }

    private void updatePortalLayerActivation(MapManager mapMgr) {
        MapLayer mapPortalLayer =  mapMgr.getPortalLayer();

        if(mapPortalLayer == null) {
            return;
        }

        Rectangle rectangle;

        for(MapObject object: mapPortalLayer.getObjects()) {
            if(object instanceof RectangleMapObject) {
                rectangle = ((RectangleMapObject)object).getRectangle();

                if(boundingBox.overlaps(rectangle)) {
                    String mapName = object.getName();
                    if(mapName == null) {
                        return;
                    }

                    mapMgr.setClosestStartPositionFromScaledUnits(currentEntityPosition);
                    mapMgr.loadMap(MapFactory.MapType.valueOf(mapName));

                    currentEntityPosition.x = mapMgr.getPlayerStartUnitScaled().x;
                    currentEntityPosition.y = mapMgr.getPlayerStartUnitScaled().y;
                    nextEntityPosition.x = mapMgr.getPlayerStartUnitScaled().x;
                    nextEntityPosition.y = mapMgr.getPlayerStartUnitScaled().y;

                    LOGGER.debug("Portal Activated");
                    return;
                }
            }
        }
    }

}
