package com.mwojnar.GameObjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameEngine.Entity;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

public class RectWall extends Entity {
    private float width = 0.0f, height = 0.0f;
    private final float PIXELS_TO_METERS = 18.0f;
    private PolygonShape shape = null;
    private Body physicsBody = null;

    public RectWall(GameWorld myWorld, Vector2 startPos, Vector2 endPos) {
        super(myWorld);

        width = endPos.x - startPos.x;
        height = endPos.y - startPos.y;
        setPos(startPos, false);
    }

    @Override
    public void onCreate() {
        BodyDef physicsBodyDef = new BodyDef();
        physicsBodyDef.type = BodyDef.BodyType.StaticBody;
        physicsBodyDef.gravityScale = 0.0f;
        physicsBodyDef.position.set((getPos(false).x + width / 2.0f) / PIXELS_TO_METERS, (getPos(false).y + height / 2.0f) / PIXELS_TO_METERS);
        physicsBody = ((LudumDare41World)getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
        shape = new PolygonShape();
        shape.setAsBox(width / 2.0f / PIXELS_TO_METERS, height / 2.0f / PIXELS_TO_METERS);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        physicsBody.createFixture(fixtureDef);
    }

    @Override
    public void draw(GameRenderer renderer) {
        setPos(physicsBody.getPosition().x * PIXELS_TO_METERS - width / 2.0f, physicsBody.getPosition().y * PIXELS_TO_METERS - height / 2.0f, false);
        AssetLoader.spriteWhite.drawMonocolorTriangle(getPos(false).x, getPos(false).y, getPos(false).x, getPos(false).y + height, getPos(false).x + width, getPos(false).y, Color.BLACK, renderer);
        AssetLoader.spriteWhite.drawMonocolorTriangle(getPos(false).x + width, getPos(false).y + height, getPos(false).x, getPos(false).y + height, getPos(false).x + width, getPos(false).y, Color.BLACK, renderer);
       /* Vector2 p1 = new Vector2(), p2 = new Vector2(), p3 = new Vector2(), p4 = new Vector2();
        shape.getVertex(0, p1);
        shape.getVertex(1, p2);
        shape.getVertex(2, p3);
        shape.getVertex(3, p4);
        p1.set(getPos(false).x + width / 2.0f + p1.x * PIXELS_TO_METERS, getPos(false).y + height / 2.0f + p1.y * PIXELS_TO_METERS);
        p2.set(getPos(false).x + width / 2.0f + p2.x * PIXELS_TO_METERS, getPos(false).y + height / 2.0f + p2.y * PIXELS_TO_METERS);
        p3.set(getPos(false).x + width / 2.0f + p3.x * PIXELS_TO_METERS, getPos(false).y + height / 2.0f + p3.y * PIXELS_TO_METERS);
        p4.set(getPos(false).x + width / 2.0f + p4.x * PIXELS_TO_METERS, getPos(false).y + height / 2.0f + p4.y * PIXELS_TO_METERS);
        AssetLoader.spriteWhite.drawMonocolorTriangle(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y, Color.BLACK, renderer);
        AssetLoader.spriteWhite.drawMonocolorTriangle(p1.x, p1.y, p4.x, p4.y, p3.x, p3.y, Color.BLACK, renderer);*/
    }
}
