package com.mwojnar.GameObjects;

import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameObjects.FixtureUserData.FinishUserData;
import com.mwojnar.GameObjects.FixtureUserData.RoadUserData;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameWorld.GameWorld;

import java.util.List;

public class FinishLine extends Wall {
    Body physicsBody = null;
    private final float PIXELS_TO_METERS = 18.0f;

    public FinishLine(GameWorld myWorld) {
        super(myWorld);
        setSprite(AssetLoader.spriteFinish);
    }

    @Override
    public void setPolygonDefinition(List<Vector2> polygonDefinition) {
        super.setPolygonDefinition(polygonDefinition);

        if (getWorld() instanceof LudumDare41World) {
            EarClippingTriangulator earClippingTriangulator = new EarClippingTriangulator();
            float[] floatArray = Wall.vector2ToFloatArray(polygonDefinition);
            short[] result = earClippingTriangulator.computeTriangles(floatArray).toArray();
            for (int i = 0; i < result.length; i += 3) {
                BodyDef physicsBodyDef = new BodyDef();
                physicsBodyDef.type = BodyDef.BodyType.StaticBody;
                physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS);
                physicsBody = ((LudumDare41World) getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
                PolygonShape shape = new PolygonShape();

                float[] vertices = new float[6];
                vertices[0] = floatArray[result[i] * 2] / PIXELS_TO_METERS;
                vertices[1] = floatArray[result[i] * 2 + 1] / PIXELS_TO_METERS;
                vertices[2] = floatArray[result[i + 1] * 2] / PIXELS_TO_METERS;
                vertices[3] = floatArray[result[i + 1] * 2 + 1] / PIXELS_TO_METERS;
                vertices[4] = floatArray[result[i + 2] * 2] / PIXELS_TO_METERS;
                vertices[5] = floatArray[result[i + 2] * 2 + 1] / PIXELS_TO_METERS;

                shape.set(vertices);
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.shape = shape;
                fixtureDef.isSensor = true;
                Fixture fixture = physicsBody.createFixture(fixtureDef);
                fixture.setUserData(new FinishUserData(this));
            }
        }
    }
}
