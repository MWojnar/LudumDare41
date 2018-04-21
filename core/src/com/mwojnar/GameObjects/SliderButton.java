package com.mwojnar.GameObjects;

import com.mwojnar.Interfaces.ButtonSubscriber;
import com.playgon.GameEngine.AbsoluteEntity;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.Mask;
import com.playgon.GameEngine.Sprite;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameWorld;

import java.awt.Button;
import java.util.ArrayList;
import java.util.List;

public class SliderButton extends AbsoluteEntity {
    private List<ButtonSubscriber> subscriberList = new ArrayList<ButtonSubscriber>();
    private int id = 0;
    private TouchEvent grabTouch = null;

    public SliderButton(GameWorld myWorld, int id) {
        super(myWorld);
        this.id = id;
    }

    @Override
    public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
        super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);

        if (grabTouch == null) {
            for (TouchEvent touchEvent : touchEventList)
                if (touchEvent.type == TouchEvent.Type.TOUCH_DOWN && getMask().collidingWithPoint(touchEvent.pointOnScreen, true)) {
                    grabTouch = touchEvent;
                    break;
                }
        } else {
            if (grabTouch.type == TouchEvent.Type.TOUCH_UP || grabTouch.type == TouchEvent.Type.DEAD) {
                if (getMask().collidingWithPoint(grabTouch.pointOnScreen, true))
                    alertSubscribers();
                grabTouch = null;
            }
        }
    }

    @Override
    public Entity setSprite(Sprite sprite) {
        super.setSprite(sprite);
        if (sprite != null) {
            setPivot(getSprite().getWidth() / 2.0f, getSprite().getWidth() / 2.0f);
            setMask(new Mask(this, getSprite().getWidth(), getSprite().getHeight()));
        }
        return this;
    }

    private void alertSubscribers() {
        for (ButtonSubscriber subscriber : subscriberList)
            subscriber.notify(id);
    }

    public void subscribe(ButtonSubscriber subscriber) {
        subscriberList.add(subscriber);
    }

}
