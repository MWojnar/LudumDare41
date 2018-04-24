package com.mwojnar.GameObjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameObjects.FixtureUserData.CarUserData;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Utils.PlaygonMath;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class Car extends Entity {
    private Body physicsBody = null, topLeftWheel = null, topRightWheel = null, bottomLeftWheel = null, bottomRightWheel = null;
    private RevoluteJoint topLeftJoint = null, topRightJoint = null;
    public boolean canMove = false;
    private float wheelAngle = 0.0f, tireAngle = 0.0f, tireRotationSpeed = (float)Math.PI / 180.0f, tireMaxRotation = (float)Math.PI / 4.0f,
    friction = 2.0f, grassFriction = 6.0f, turningFriction = 50.0f, bodyDensity = 0.5f, tireDensity = 0.5f, maxForwardSpeed = 100.0f, maxReverseSpeed = 40.0f,
    targetSpeed = 0.0f, targetRotation = 0.0f, startRotation = 0.0f;
    private int rank = 0;
    private final float PIXELS_TO_METERS = 18.0f;
    private Color color = Color.WHITE.cpy();
    private List<Road> roadCollisions = new ArrayList<Road>();

    public Car(GameWorld myWorld) {
        super(myWorld);

        setSprite(AssetLoader.spriteCar);
        setPivot(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f);
        setRotation(-90.0f);
        setDepth(50);
    }

    @Override
    public void onCreate() {
        if (!getWorld().isDebug()) {
            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("CarAttributes.txt"));
                tireRotationSpeed = Float.parseFloat((String) jsonObject.get("Tire Rotation Max Speed")) * (float) Math.PI / 180.0f;
                tireMaxRotation = Float.parseFloat((String) jsonObject.get("Tire Max Turn")) * (float) Math.PI / 180.0f;
                friction = Float.parseFloat((String) jsonObject.get("Linear Dampening (Friction)"));
                turningFriction = Float.parseFloat((String) jsonObject.get("Angular Dampening (Turning Friction)"));
                bodyDensity = Float.parseFloat((String) jsonObject.get("Car Body Density"));
                tireDensity = Float.parseFloat((String) jsonObject.get("Car Tires Density"));
                maxForwardSpeed = Float.parseFloat((String) jsonObject.get("Max Forward Force"));
                maxReverseSpeed = Float.parseFloat((String) jsonObject.get("Max Reverse Force"));
                grassFriction = Float.parseFloat((String) jsonObject.get("Grass Linear Dampening"));
            } catch (Exception e) {
                //e.printStackTrace();
            }

            BodyDef physicsBodyDef = new BodyDef();
            physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
            physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS);
            physicsBody = ((LudumDare41World) getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
            PolygonShape shape = new PolygonShape();
            shape.setAsBox((getSprite().getWidth() - 2.0f) / 2.0f / PIXELS_TO_METERS, (getSprite().getHeight() - 1.0f) / 2.0f / PIXELS_TO_METERS);
            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = bodyDensity;
            Fixture fixture = physicsBody.createFixture(fixtureDef);
            fixture.setUserData(new CarUserData(this));

            physicsBodyDef = new BodyDef();
            physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
            physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS - 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS - 23.0f / PIXELS_TO_METERS);
            topLeftWheel = ((LudumDare41World) getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
            shape = new PolygonShape();
            shape.setAsBox(2.0f / PIXELS_TO_METERS, 4.0f / PIXELS_TO_METERS);
            fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = tireDensity;
            topLeftWheel.createFixture(fixtureDef);
            RevoluteJointDef jointDef = new RevoluteJointDef();
            jointDef.bodyA = physicsBody;
            jointDef.bodyB = topLeftWheel;
            jointDef.localAnchorA.set(-20.0f / PIXELS_TO_METERS, -23.0f / PIXELS_TO_METERS);
            jointDef.collideConnected = false;
            jointDef.enableLimit = true;
            jointDef.upperAngle = 0.0f;
            jointDef.lowerAngle = 0.0f;
            topLeftJoint = (RevoluteJoint) ((LudumDare41World) getWorld()).getPhysicsWorld().createJoint(jointDef);

            physicsBodyDef = new BodyDef();
            physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
            physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS + 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS - 23.0f / PIXELS_TO_METERS);
            topRightWheel = ((LudumDare41World) getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
            shape = new PolygonShape();
            shape.setAsBox(2.0f / PIXELS_TO_METERS, 4.0f / PIXELS_TO_METERS);
            fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = tireDensity;
            topRightWheel.createFixture(fixtureDef);
            jointDef = new RevoluteJointDef();
            jointDef.bodyA = physicsBody;
            jointDef.bodyB = topRightWheel;
            jointDef.localAnchorA.set(20.0f / PIXELS_TO_METERS, -23.0f / PIXELS_TO_METERS);
            jointDef.collideConnected = false;
            jointDef.enableLimit = true;
            jointDef.upperAngle = 0.0f;
            jointDef.lowerAngle = 0.0f;
            topRightJoint = (RevoluteJoint) ((LudumDare41World) getWorld()).getPhysicsWorld().createJoint(jointDef);

            physicsBodyDef = new BodyDef();
            physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
            physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS - 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS + 23.0f / PIXELS_TO_METERS);
            bottomLeftWheel = ((LudumDare41World) getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
            shape = new PolygonShape();
            shape.setAsBox(2.0f / PIXELS_TO_METERS, 4.0f / PIXELS_TO_METERS);
            fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = tireDensity;
            bottomLeftWheel.createFixture(fixtureDef);
            jointDef = new RevoluteJointDef();
            jointDef.bodyA = physicsBody;
            jointDef.bodyB = bottomLeftWheel;
            jointDef.localAnchorA.set(-20.0f / PIXELS_TO_METERS, 23.0f / PIXELS_TO_METERS);
            jointDef.collideConnected = false;
            jointDef.enableLimit = true;
            jointDef.upperAngle = 0.0f;
            jointDef.lowerAngle = 0.0f;
            ((LudumDare41World) getWorld()).getPhysicsWorld().createJoint(jointDef);

            physicsBodyDef = new BodyDef();
            physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
            physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS + 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS + 23.0f / PIXELS_TO_METERS);
            bottomRightWheel = ((LudumDare41World) getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
            shape = new PolygonShape();
            shape.setAsBox(2.0f / PIXELS_TO_METERS, 4.0f / PIXELS_TO_METERS);
            fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.density = tireDensity;
            bottomRightWheel.createFixture(fixtureDef);
            jointDef = new RevoluteJointDef();
            jointDef.bodyA = physicsBody;
            jointDef.bodyB = bottomRightWheel;
            jointDef.localAnchorA.set(20.0f / PIXELS_TO_METERS, 23.0f / PIXELS_TO_METERS);
            jointDef.collideConnected = false;
            jointDef.enableLimit = true;
            jointDef.upperAngle = 0.0f;
            jointDef.lowerAngle = 0.0f;
            ((LudumDare41World) getWorld()).getPhysicsWorld().createJoint(jointDef);

            shape.dispose();
            physicsBody.setLinearDamping(friction);
            physicsBody.setAngularDamping(turningFriction);
            startRotation = getRotation();
        }
    }

    @Override
    public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
        super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);

        if (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() < 15) {
            physicsBody.setTransform(physicsBody.getPosition(), startRotation * (float)Math.PI / 180.0f);
            ((LudumDare41World)getWorld()).getPhysicsWorld().step(1.0f / 60.0f, 6, 2);
        }

        //if (roadCollisions.isEmpty())
            //physicsBody.setLinearDamping(grassFriction);
        //else
            physicsBody.setLinearDamping(friction);

        setPos(physicsBody.getPosition().x * PIXELS_TO_METERS,  physicsBody.getPosition().y * PIXELS_TO_METERS, true);
        setRotation(PlaygonMath.toDegrees(physicsBody.getAngle()));

        if (((LudumDare41World)getWorld()).isSim() || ((LudumDare41World)getWorld()).getFramesSinceLevelCreation() < 15) {
            if (Math.abs(targetRotation - tireAngle) < tireRotationSpeed)
                tireAngle = targetRotation;
            if (tireAngle < targetRotation)
                tireAngle += tireRotationSpeed;
            else if (tireAngle > targetRotation)
                tireAngle -= tireRotationSpeed;
            if (tireAngle < -tireMaxRotation)
                tireAngle = -tireMaxRotation;
            if (tireAngle > tireMaxRotation)
                tireAngle = tireMaxRotation;

            topLeftJoint.setLimits(tireAngle, tireAngle);
            topRightJoint.setLimits(tireAngle, tireAngle);
            if (targetSpeed != 0) {
                topLeftWheel.applyForceToCenter(PlaygonMath.getGridVector(new Vector2(targetSpeed, topLeftWheel.getAngle() - (float) Math.PI / 2.0f)), true);
                topRightWheel.applyForceToCenter(PlaygonMath.getGridVector(new Vector2(targetSpeed, topRightWheel.getAngle() - (float) Math.PI / 2.0f)), true);
            }

            ridLateralVelocity(topLeftWheel);
            ridLateralVelocity(topRightWheel);
            ridLateralVelocity(bottomLeftWheel);
            ridLateralVelocity(bottomRightWheel);
        }
    }

    private void ridLateralVelocity(Body body) {
        Vector2 impulse = getLateralVelocity(body);
        body.applyLinearImpulse(impulse, body.getWorldCenter(), true);
    }

    private Vector2 getLateralVelocity(Body body) {
        Vector2 returnVector = body.getWorldVector(new Vector2(1, 0));
        float multiplier = (returnVector.x * body.getLinearVelocity().x + returnVector.y * body.getLinearVelocity().y);
        return returnVector.set(-returnVector.x * multiplier * body.getMass(), -returnVector.y * multiplier * body.getMass());
    }

    @Override
    public void draw(GameRenderer renderer) {
        if (getSprite() != null) {
            if (((LudumDare41World)getWorld()).isBlinking(rank) && (((LudumDare41World)getWorld()).getFramesSinceLevelCreation() % 20 > 9))
                getSprite().draw(getPos(false).x, getPos(false).y, getFrame(), getScale(), getScale(), getRotation(), getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f, Color.WHITE, renderer);
            else
                getSprite().draw(getPos(false).x, getPos(false).y, getFrame(), getScale(), getScale(), getRotation(), getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f, color, renderer);
        }
        /*float x1 = topLeftWheel.getWorldCenter().x * PIXELS_TO_METERS;
        float y1 = topLeftWheel.getWorldCenter().y * PIXELS_TO_METERS;
        float x2 = x1 + 10;
        float y2 = y1 - 10;
        float x3 = x1 + 10;
        float y3 = y1 + 10;
        AssetLoader.spriteWhite.drawMonocolorTriangle(x1, y1, x2, y2, x3, y3, Color.CYAN, renderer);
        x1 = topRightWheel.getWorldCenter().x * PIXELS_TO_METERS;
        y1 = topRightWheel.getWorldCenter().y * PIXELS_TO_METERS;
        x2 = x1 + 10;
        y2 = y1 - 10;
        x3 = x1 + 10;
        y3 = y1 + 10;
        AssetLoader.spriteWhite.drawMonocolorTriangle(x1, y1, x2, y2, x3, y3, Color.CYAN, renderer);
        x1 = bottomLeftWheel.getWorldCenter().x * PIXELS_TO_METERS;
        y1 = bottomLeftWheel.getWorldCenter().y * PIXELS_TO_METERS;
        x2 = x1 + 10;
        y2 = y1 - 10;
        x3 = x1 + 10;
        y3 = y1 + 10;
        AssetLoader.spriteWhite.drawMonocolorTriangle(x1, y1, x2, y2, x3, y3, Color.CYAN, renderer);
        x1 = bottomRightWheel.getWorldCenter().x * PIXELS_TO_METERS;
        y1 = bottomRightWheel.getWorldCenter().y * PIXELS_TO_METERS;
        x2 = x1 + 10;
        y2 = y1 - 10;
        x3 = x1 + 10;
        y3 = y1 + 10;
        AssetLoader.spriteWhite.drawMonocolorTriangle(x1, y1, x2, y2, x3, y3, Color.CYAN, renderer);
        x1 = physicsBody.getWorldCenter().x * PIXELS_TO_METERS;
        y1 = physicsBody.getWorldCenter().y * PIXELS_TO_METERS;
        x2 = x1 + 10;
        y2 = y1 - 10;
        x3 = x1 + 10;
        y3 = y1 + 10;
        AssetLoader.spriteWhite.drawMonocolorTriangle(x1, y1, x2, y2, x3, y3, Color.CYAN, renderer);*/

    }

    public void setForces() {
        if (!getWorld().isDebug()) {
            targetRotation = ((LudumDare41World) getWorld()).getTargetRotation() * tireMaxRotation;
            targetSpeed = ((LudumDare41World) getWorld()).getSpeed();
        }

        if (targetSpeed > 0)
            targetSpeed *= maxForwardSpeed;
        else
            targetSpeed *= maxReverseSpeed;
    }

    public float getTargetSpeed() {
        if (targetSpeed >= 0)
            return targetSpeed / maxForwardSpeed;
        return targetSpeed / maxReverseSpeed;
    }

    public float getTargetRotation() {
        return targetRotation / tireMaxRotation;
    }

    public void setColor(Color color) {
        this.color = color.cpy();
    }

    public void setRank(int rank) {
        this.rank = rank;
        this.color = AssetLoader.playerColors[rank - 1];
    }

    public int getRank() {
        return rank;
    }

    public void addRoadCollision(Road road) {
        roadCollisions.add(road);
    }

    public void removeRoadCollision(Road road) {
        while (roadCollisions.contains(road))
            roadCollisions.remove(road);
    }

    public void startEngineNoise() {
        float pitch = 1.0f;
        if (targetSpeed > 0)
            pitch = 0.5f + 1.5f * targetSpeed / maxForwardSpeed;
        else if (targetSpeed < 0)
            pitch = 0.5f - 0.75f * targetSpeed / maxReverseSpeed;
        else
            return;
        AssetLoader.sndEngineList.get(rank - 1).loop(AssetLoader.soundVolume, pitch, 0.0f);
    }

    public void endEngineNoise() {
        AssetLoader.sndEngineList.get(rank - 1).stop();
    }
}
