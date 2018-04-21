package com.mwojnar.GameObjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mwojnar.Assets.AssetLoader;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.Mask;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Utils.PlaygonMath;

import java.util.List;

public class SteeringWheel extends Entity {
    private float currentRotation = 0.0f, maxTurns = 3.0f;
    private TouchEvent grabTouch = null;
    private Vector2 previousPoint = null;

    public SteeringWheel(GameWorld myWorld) {
        super(myWorld);
        setSprite(AssetLoader.spriteSteeringWheel);
        setPivot(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f);
        setMask(new Mask(this, new Vector2(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f), getSprite().getWidth() / 2.0f));
    }

    @Override
    public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
        super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);

        if (grabTouch == null) {
            for (TouchEvent touchEvent : touchEventList)
                if (touchEvent.type == TouchEvent.Type.TOUCH_DOWN && getMask().collidingWithPoint(touchEvent.point, true)) {
                    grabTouch = touchEvent;
                    break;
                }
        } else {
            if (grabTouch.type == TouchEvent.Type.TOUCH_DRAG && grabTouch.previousPoint != null && !grabTouch.point.equals(grabTouch.previousPoint) && !grabTouch.point.equals(previousPoint)) {
                float currentAngle = PlaygonMath.direction(getPos(true), grabTouch.previousPoint);
                float nextAngle = PlaygonMath.direction(getPos(true), grabTouch.point);
                float angleDiff = nextAngle - currentAngle;

                while (angleDiff > Math.PI / 2.0f)
                    angleDiff -= (float)Math.PI;
                while (angleDiff < -Math.PI / 2.0f)
                    angleDiff += (float)Math.PI;

                currentRotation += angleDiff;
                previousPoint = grabTouch.point.cpy();
            }
            if (grabTouch.type == TouchEvent.Type.TOUCH_UP || grabTouch.type == TouchEvent.Type.DEAD)
                grabTouch = null;
        }

        if (currentRotation > Math.PI * maxTurns)
            currentRotation = (float)Math.PI * maxTurns;
        else if (currentRotation < -Math.PI * maxTurns)
            currentRotation = -(float)Math.PI * maxTurns;
        setRotation(currentRotation * 180.0f / (float)Math.PI);
    }
}