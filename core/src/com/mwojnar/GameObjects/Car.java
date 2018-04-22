package com.mwojnar.GameObjects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.mwojnar.Assets.AssetLoader;
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
import java.util.List;

public class Car extends Entity {
    private Body physicsBody = null, topLeftWheel = null, topRightWheel = null, bottomLeftWheel = null, bottomRightWheel = null;
    private RevoluteJoint topLeftJoint = null, topRightJoint = null;
    public boolean canMove = false;
    private float wheelAngle = 0.0f, tireAngle = 0.0f, tireRotationSpeed = (float)Math.PI / 200.0f, tireMaxRotation = (float)Math.PI / 4.0f,
    friction = 1.0f, turningFriction = 2.0f, bodyDensity = 0.1f, tireDensity = 0.1f, maxForwardSpeed = 25.0f, maxReverseSpeed = 5.0f;
    private final float PIXELS_TO_METERS = 18.0f;

    public static Car globalTest = null;

    public Car(GameWorld myWorld) {
        super(myWorld);

        setSprite(AssetLoader.spriteCar);
        setPivot(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f);
    }

    @Override
    public void onCreate() {
        JSONParser jsonParser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject)jsonParser.parse(new FileReader("CarAttributes.txt"));
            tireRotationSpeed = Float.parseFloat((String)jsonObject.get("Tire Rotation Max Speed")) * (float)Math.PI / 180.0f;
            tireMaxRotation = Float.parseFloat((String)jsonObject.get("Tire Max Turn")) * (float)Math.PI / 180.0f;
            friction = Float.parseFloat((String)jsonObject.get("Linear Dampening (Friction)"));
            turningFriction = Float.parseFloat((String)jsonObject.get("Angular Dampening (Turning Friction)"));
            bodyDensity = Float.parseFloat((String)jsonObject.get("Car Body Density"));
            tireDensity = Float.parseFloat((String)jsonObject.get("Car Tires Density"));
            maxForwardSpeed = Float.parseFloat((String)jsonObject.get("Max Forward Force"));
            maxReverseSpeed = Float.parseFloat((String)jsonObject.get("Max Reverse Force"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        BodyDef physicsBodyDef = new BodyDef();
        physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
        physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS);
        physicsBody = ((LudumDare41World)getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(getSprite().getWidth() / 2.0f / PIXELS_TO_METERS, getSprite().getHeight() / 2.0f / PIXELS_TO_METERS);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = bodyDensity;
        physicsBody.createFixture(fixtureDef);

        physicsBodyDef = new BodyDef();
        physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
        physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS - 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS - 23.0f / PIXELS_TO_METERS);
        topLeftWheel = ((LudumDare41World)getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
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
        topLeftJoint = (RevoluteJoint)((LudumDare41World)getWorld()).getPhysicsWorld().createJoint(jointDef);

        physicsBodyDef = new BodyDef();
        physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
        physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS + 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS - 23.0f / PIXELS_TO_METERS);
        topRightWheel = ((LudumDare41World)getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
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
        topRightJoint = (RevoluteJoint)((LudumDare41World)getWorld()).getPhysicsWorld().createJoint(jointDef);

        physicsBodyDef = new BodyDef();
        physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
        physicsBodyDef.position.set(getPos(false).x - 20.0f / PIXELS_TO_METERS, getPos(false).y + 23.0f / PIXELS_TO_METERS);
        bottomLeftWheel = ((LudumDare41World)getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
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
        ((LudumDare41World)getWorld()).getPhysicsWorld().createJoint(jointDef);

        physicsBodyDef = new BodyDef();
        physicsBodyDef.type = BodyDef.BodyType.DynamicBody;
        physicsBodyDef.position.set(getPos(false).x / PIXELS_TO_METERS + 20.0f / PIXELS_TO_METERS, getPos(false).y / PIXELS_TO_METERS + 23.0f / PIXELS_TO_METERS);
        bottomRightWheel = ((LudumDare41World)getWorld()).getPhysicsWorld().createBody(physicsBodyDef);
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
        ((LudumDare41World)getWorld()).getPhysicsWorld().createJoint(jointDef);

        shape.dispose();
        physicsBody.setLinearDamping(friction);
        physicsBody.setAngularDamping(turningFriction);
    }

    @Override
    public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
        super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);

        if (canMove)
            globalTest = this;

        setPos(physicsBody.getPosition().x * PIXELS_TO_METERS,  physicsBody.getPosition().y * PIXELS_TO_METERS, false);
        setRotation(PlaygonMath.toDegrees(physicsBody.getAngle()));

        float targetRotation = ((LudumDare41World)getWorld()).getTargetRotation() * tireMaxRotation;
        float speed = ((LudumDare41World)getWorld()).getSpeed();
        if (speed > 0)
            speed *= maxForwardSpeed;
        else
            speed *= maxReverseSpeed;

        if (canMove) {
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
                topLeftWheel.applyForceToCenter(PlaygonMath.getGridVector(new Vector2(speed, topLeftWheel.getAngle() - (float)Math.PI / 2.0f)), true);
                topRightWheel.applyForceToCenter(PlaygonMath.getGridVector(new Vector2(speed, topRightWheel.getAngle() - (float)Math.PI / 2.0f)), true);
                //physicsBody.applyForce(PlaygonMath.getGridVector(new Vector2(500, PlaygonMath.toRadians(getRotation() - 90 + wheelAngle))), physicsBody.getWorldPoint(new Vector2(0.0f, -23.0f)), true);
        }

        ridLateralVelocity(topLeftWheel);
        ridLateralVelocity(topRightWheel);
        ridLateralVelocity(bottomLeftWheel);
        ridLateralVelocity(bottomRightWheel);
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
        super.draw(renderer);
    }
}
