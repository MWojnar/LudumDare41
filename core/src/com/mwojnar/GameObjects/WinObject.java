package com.mwojnar.GameObjects;

import com.badlogic.gdx.utils.Align;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameEngine.AbsoluteEntity;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

import java.util.List;

public class WinObject extends AbsoluteEntity {
    private int winner = 0;

    public WinObject(GameWorld myWorld, int winner) {
        super(myWorld);
        AssetLoader.winFont.setColor(AssetLoader.playerColors[winner - 1]);
        this.winner = winner;
        setDepth(10000);
    }

    @Override
    public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
        super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);

        for (TouchEvent touchEvent : touchEventList)
            if (touchEvent.type == TouchEvent.Type.TOUCH_UP || touchEvent.type == TouchEvent.Type.DEAD)
                ((LudumDare41World)getWorld()).startMenu();
    }

    @Override
    public void draw(GameRenderer renderer) {
        AssetLoader.winFont.draw(renderer.getBatcher(), "Player " + winner + " Wins!", getWorld().getCamPos(false).x + getWorld().getGameDimensions().x / 2.0f, getWorld().getCamPos(false).x + getWorld().getGameDimensions().y / 2.0f, 0.0f, Align.center, false);
    }
}
