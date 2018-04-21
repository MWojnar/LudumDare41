package com.mwojnar.GameObjects;

import com.mwojnar.Assets.AssetLoader;
import com.playgon.GameEngine.AbsoluteEntity;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

public class Dashboard extends AbsoluteEntity {
    private float width = 100.0f, height = 100.0f;

    public Dashboard(GameWorld myWorld) {
        super(myWorld);
    }

    @Override
    public void draw(GameRenderer renderer) {
        AssetLoader.spriteDashTopLeft.drawAbsolute(getWorldPos(false).x, getWorldPos(false).y, 0, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, renderer);
        AssetLoader.spriteDashTopRight.drawAbsolute(getWorldPos(false).x + width - AssetLoader.spriteDashTopRight.getWidth(), getWorldPos(false).y, 0, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, renderer);
        AssetLoader.spriteDashTop.drawTiled(0, getWorldPos(false).x + AssetLoader.spriteDashTopLeft.getWidth(), getWorldPos(false).y,
                width - AssetLoader.spriteDashTopLeft.getWidth() - AssetLoader.spriteDashTopRight.getWidth(), AssetLoader.spriteDashTop.getHeight(), renderer);
        AssetLoader.spriteDashLeft.drawTiled(0, getWorldPos(false).x, getWorldPos(false).y + AssetLoader.spriteDashTopLeft.getHeight(),
                AssetLoader.spriteDashLeft.getWidth(), height - AssetLoader.spriteDashTopLeft.getHeight(), renderer);
        AssetLoader.spriteDashRight.drawTiled(0, getWorldPos(false).x + width - AssetLoader.spriteDashRight.getWidth(), getWorldPos(false).y + AssetLoader.spriteDashTopRight.getHeight(),
                AssetLoader.spriteDashRight.getWidth(), height - AssetLoader.spriteDashTopRight.getHeight(), renderer);
        float x1 = getWorldPos(false).x + AssetLoader.spriteDashTopLeft.getWidth(),
              y1 = getWorldPos(false).y + AssetLoader.spriteDashTopLeft.getHeight(),
              x2 = getWorldPos(false).x + width - AssetLoader.spriteDashTopRight.getWidth(),
              y2 = y1,
              x3 = x1,
              y3 = getWorldPos(false).y + height,
              x4 = x2,
              y4 = y3;
        AssetLoader.spriteWhite.drawMonocolorTriangle(x1, y1, x2, y2, x3, y3, AssetLoader.dashColor, renderer);
        AssetLoader.spriteWhite.drawMonocolorTriangle(x4, y4, x2, y2, x3, y3, AssetLoader.dashColor, renderer);
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}
