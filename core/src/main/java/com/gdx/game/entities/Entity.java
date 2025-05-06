package com.gdx.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.gdx.game.Enums.ENTITYSTATE;
import com.gdx.game.Enums.ENTITYTYPE;
import com.gdx.game.map.Chunk;
import com.gdx.game.map.Tile;

public class Entity implements Comparable<Entity> {
    private final Vector3 pos3;
    private Texture texture;
    private final Texture shadow;
    private final float width;
    private final float height;
    public ENTITYTYPE type;
    public ENTITYSTATE state;
    public Body body;
    public int hashcode;
    public Body sensor;

    public Boolean ticks;
    public float time;
    public Vector3 destVec;
    public Tile currentTile;
    public float coolDown;

    public Entity(Texture texture, Texture shadow, float width, float height) {
        this.pos3 = new Vector3();
        this.texture = texture;
        this.shadow = shadow;
        this.width = width;
        this.height = height;
    }

    public Vector3 getPos3() {
        return pos3;
    }
    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getDirectionX() {
        return (float) 0;
    }

    public float getDirectionY() {
        return (float) 0;
    }

    public void draw(SpriteBatch batch) {
        if(shadow != null) batch.draw(shadow, pos3.x, pos3.y, width, height);
        if(texture != null) batch.draw(texture, pos3.x, pos3.y, width, height);
    }

    public void tick(float delta) {
        time += delta;
    }

    public void tick(float delta, Chunk chunk) {
    }

    public void getVector(Vector3 dest) {
        float dx = dest.x - getPos3().x;
        float dy = dest.y - getPos3().y;
        double h = Math.sqrt(dx * dx + dy * dy);
        float dn = (float)(h / 1.4142135623730951);

        destVec = new Vector3(dx / dn, dy / dn, 0);
    }

    public void updatePositions() {
        getPos3().x = body.getPosition().x - getWidth()/2;
        getPos3().y = body.getPosition().y - getHeight()/4;
    }

    public void collision(Entity entity, boolean begin){}

    @Override
    public int compareTo(Entity entity) {
        float tempY =  entity.getPos3().y;
        float compareY = getPos3().y;

        return Float.compare(tempY, compareY);
    }
}
