package com.gdx.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.gdx.game.Media;
import com.gdx.game.box2d.Box2dHelper;
import com.gdx.game.box2d.Box2dWorld;
import com.gdx.game.manager.AnimationManager;
import java.util.Optional;

public class Rabite extends Entity {
    private TextureRegion tRegion;
    private final AnimationManager animationManager = new AnimationManager();

    public Rabite(Vector3 pos3, Box2dWorld box2d, EntityEnums.ENTITYSTATE state) {
        super(Media.rabite,null,8, 8);
        this.type = EntityEnums.ENTITYTYPE.ENEMY;
        this.getPos3().x = pos3.x;
        this.getPos3().y = pos3.y;
        this.body = Box2dHelper.createBody(box2d.getWorld(), getWidth()/2, getHeight()/2, getWidth()/4 + 0.1f, +2, pos3, null, BodyDef.BodyType.StaticBody);
        this.sensor = Box2dHelper.createSensor(box2d.getWorld(), getWidth()*.6f, getHeight()*.6f,getWidth()/4 - 0.3f, 1.6f, pos3, null, BodyDef.BodyType.StaticBody);
        this.hashcode = sensor.getFixtureList().get(0).hashCode();
        this.state = state;
        this.ticks = true;
    }

    @Override
    public void draw(SpriteBatch batch) {
        state = EntityEnums.ENTITYSTATE.WALKING_DOWN;
        setRabiteTextureRegion();
        batch.draw(Media.heroShadow, getPos3().x + 0.1f, getPos3().y, getWidth(), getHeight()/2);
        Optional.ofNullable(tRegion)
                .ifPresent(t -> batch.draw(t, getPos3().x, getPos3().y, getWidth(), getHeight()*(float)1.2));
    }

    private void setRabiteTextureRegion() {
        if(isWalkingUp()) {
            tRegion = animationManager.setTextureRegion(animation(textureRegions(Media.heroWalkUp)), time);
        } else if(isWalkingDown()) {
            tRegion = animationManager.setTextureRegion(animation(textureRegions(Media.rabiteWalkDown)), time);
        } else if(isWalkingRight()) {
            tRegion = animationManager.setTextureRegion(animation(textureRegions(Media.heroWalkRight)), time);
        } else if(isWalkingLeft()) {
            tRegion = animationManager.setTextureRegion(animation(textureRegions(Media.heroWalkLeft)), time);
        } else if(isLookingUp()) {
            tRegion = textureRegions(Media.heroWalkUp)[1];
        } else if(isLookingDown()) {
            tRegion = textureRegions(Media.rabiteWalkDown)[3];
        } else if(isLookingRight()) {
            tRegion = textureRegions(Media.heroWalkRight)[1];
        } else if(isLookingLeft()) {
            tRegion = textureRegions(Media.heroWalkLeft)[1];
        }
    }

    private TextureRegion[] textureRegions(Texture texture) {
        return animationManager.setTextureRegions(texture, 27,31);
    }

    private Animation<TextureRegion> animation(TextureRegion[] textureRegions) {
        return animationManager.setAnimation(textureRegions);
    }
    private boolean isWalkingUp(){
        return state == EntityEnums.ENTITYSTATE.WALKING_UP;
    }

    private boolean isWalkingDown(){
        return state == EntityEnums.ENTITYSTATE.WALKING_DOWN;
    }

    private boolean isWalkingRight(){
        return state == EntityEnums.ENTITYSTATE.WALKING_RIGHT;
    }

    private boolean isWalkingLeft(){
        return state == EntityEnums.ENTITYSTATE.WALKING_LEFT;
    }

    private boolean isLookingUp() {
        return state == EntityEnums.ENTITYSTATE.LOOK_UP;
    }

    private boolean isLookingDown() {
        return state == EntityEnums.ENTITYSTATE.LOOK_DOWN;
    }

    private boolean isLookingRight() {
        return state == EntityEnums.ENTITYSTATE.LOOK_RIGHT;
    }

    private boolean isLookingLeft() {
        return state == EntityEnums.ENTITYSTATE.LOOK_LEFT;
    }

}
