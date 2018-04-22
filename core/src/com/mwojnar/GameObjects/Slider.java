package com.mwojnar.GameObjects;

import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.Interfaces.ButtonSubscriber;
import com.playgon.GameEngine.AbsoluteEntity;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.Mask;
import com.playgon.GameEngine.Sprite;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

import java.util.ArrayList;
import java.util.List;

public class Slider extends AbsoluteEntity implements ButtonSubscriber {
    private Sprite topSprite = null, bottomSprite = null, bodySprite = null;
    private float bodyLength = 100.0f, percentageMoved = 0.5f, horBuffer = 10.0f;
    private List<Float> notches = new ArrayList<Float>();
    private TouchEvent grabTouch = null;

    public Slider(GameWorld myWorld) {
        super(myWorld);
        setSprite(AssetLoader.spriteSlider);
        setPivot(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f);
        topSprite = AssetLoader.spriteSliderTop;
        bottomSprite = AssetLoader.spriteSliderBottom;
        bodySprite = AssetLoader.spriteSliderBody;
        setBodyLength(100.0f);
        notches.add(0.0f);
        notches.add(0.5f);
        notches.add(1.0f);
        setDepth(1000);
    }

    @Override
    public void onCreate() {
        SliderButton button = new SliderButton(getWorld(), 0);
        button.setSprite(AssetLoader.spriteSliderFaster);
        button.setPos(getPos(false).x + topSprite.getWidth() + horBuffer, getPos(false).y, false);
        button.subscribe(this);
        getWorld().createEntity(button);
        button = new SliderButton(getWorld(), 1);
        button.setSprite(AssetLoader.spriteSliderPause);
        button.setPos(getPos(false).x + topSprite.getWidth() + horBuffer, getPos(false).y + topSprite.getHeight() + bodyLength / 2.0f - button.getSprite().getHeight() / 2.0f, false);
        button.subscribe(this);
        getWorld().createEntity(button);
        button = new SliderButton(getWorld(), 2);
        button.setSprite(AssetLoader.spriteSliderSlower);
        button.setPos(getPos(false).x + topSprite.getWidth() + horBuffer, getPos(false).y + topSprite.getHeight() + bodyLength + bottomSprite.getHeight() - button.getSprite().getHeight(), false);
        button.subscribe(this);
        getWorld().createEntity(button);
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
            if (grabTouch.type == TouchEvent.Type.TOUCH_DRAG)
                percentageMoved = (grabTouch.pointOnScreen.y - getPos(false).y - topSprite.getHeight()) / bodyLength;
            else if (grabTouch.type == TouchEvent.Type.TOUCH_UP || grabTouch.type == TouchEvent.Type.DEAD)
                grabTouch = null;
        }
        if (percentageMoved < 0)
            percentageMoved = 0;
        if (percentageMoved > 1)
            percentageMoved = 1;
    }

    @Override
    public void draw(GameRenderer renderer) {
        topSprite.drawAbsolute(getPos(false).x, getPos(false).y, 0, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, renderer);
        bodySprite.drawTiled(0, getWorldPos(false).x, getWorldPos(false).y + topSprite.getHeight(), bodySprite.getWidth(), bodyLength, renderer);
        bottomSprite.drawAbsolute(getPos(false).x, getPos(false).y + topSprite.getHeight() + bodyLength, 0, 1.0f, 1.0f, 0.0f, bottomSprite.getWidth() / 2.0f, bottomSprite.getHeight() / 2.0f, renderer);
        getSprite().drawAbsolute(getPos(false).x + topSprite.getWidth() / 2.0f - getSprite().getWidth() / 2.0f, getPos(false).y + topSprite.getHeight() + percentageMoved * bodyLength - getSprite().getHeight() / 2.0f,
                0, 1.0f, 1.0f, 0.0f, getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f, renderer);
    }

    public Sprite getTopSprite() {
        return topSprite;
    }

    public void setTopSprite(Sprite topSprite) {
        this.topSprite = topSprite;
    }

    public Sprite getBottomSprite() {
        return bottomSprite;
    }

    public void setBottomSprite(Sprite bottomSprite) {
        this.bottomSprite = bottomSprite;
    }

    public Sprite getBodySprite() {
        return bodySprite;
    }

    public void setBodySprite(Sprite bodySprite) {
        this.bodySprite = bodySprite;
    }

    public float getBodyLength() {
        return bodyLength;
    }

    public void setBodyLength(float bodyLength) {
        this.bodyLength = bodyLength;
        setMask(new Mask(this, topSprite.getWidth(), topSprite.getHeight() + bodyLength + bottomSprite.getHeight()));
    }

    @Override
    public void notify(int id) {
        if (id < notches.size())
            percentageMoved = notches.get(id);
    }

    public float getValue() {
        return percentageMoved;
    }

    public float getSpeed() {
        if (percentageMoved <= 0.5f)
            return (0.5f - percentageMoved) * 50.0f;
        return -((percentageMoved - 0.5f) * 10.0f);
    }
}
