package com.mwojnar.GameObjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.mwojnar.Assets.AssetLoader;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.Mask;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Utils.Method;
import com.playgon.Utils.Pair;

public class CarMarker extends Entity {
    private int place = 1;
    private Color color = Color.WHITE;

    public CarMarker(GameWorld myWorld) {
        super(myWorld);
        setSprite(AssetLoader.spriteCar);
        setPlace(1);
        setPivot(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f);
        setMask(new Mask(this, getSprite().getWidth(), getSprite().getHeight()));
        setDepth(50);
        setRotation(-90.0f);
    }

    @Override
    public void loadLevelEditorMethods() {
        super.loadLevelEditorMethods();

        levelEditorMethods.add(new Pair<Method, Method>(new Method(CarMarker.class, "setPlace", "Rank", (int)1), new Method(CarMarker.class, "getPlace")));
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
        if (place - 1 >= 0 && place - 1 < AssetLoader.playerColors.length)
            color = AssetLoader.playerColors[place - 1];
        else
            color = Color.WHITE;
    }

    @Override
    public void draw(GameRenderer renderer) {
        if (getSprite() != null)
            getSprite().draw(getPos(false).x, getPos(false).y, getFrame(), getScale(), getScale(), getRotation(), getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f, color, renderer);
    }
}
