package com.mwojnar.GameObjects;

import java.util.List;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

public class Title extends Entity {
	private int timer = 15;

	public Title(GameWorld myWorld) {
		
		super(myWorld);
		
	}
	
	@Override
	public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
		
		super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);

		timer--;
		if (timer < 0)
			timer = 0;

		int players = 0;
		for (TouchEvent touchEvent : touchEventList)
			if (touchEvent.type == TouchEvent.Type.TOUCH_UP || touchEvent.type == TouchEvent.Type.DEAD)
				if (touchEvent.pointOnScreen.y > 240)
					if (touchEvent.pointOnScreen.x < 400) {
						if (touchEvent.pointOnScreen.y < 360)
							players = 1;
						else
							players = 3;
					} else {
						if (touchEvent.pointOnScreen.y < 360)
							players = 2;
						else
							players = 4;
					}
		if (players > 0)
			((LudumDare41World)getWorld()).startLevel1(players);
		
	}
	
	@Override
	public void draw(GameRenderer renderer) {
		
		AssetLoader.spriteTitle.drawAbsolute(getWorld().getGameDimensions().x / 2.0f - AssetLoader.spriteTitle.getWidth() / 2.0f - 800.0f * timer / 15.0f, 50.0f,
				1, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, renderer);
		AssetLoader.titleFont.draw(renderer.getBatcher(), "1 Player", getWorld().getCamPos(false).x + getWorld().getGameDimensions().x / 4.0f + 800.0f * timer / 15.0f, getWorld().getCamPos(false).y + 300.0f, 0, Align.center, false);
		AssetLoader.titleFont.draw(renderer.getBatcher(), "2 Player", getWorld().getCamPos(false).x + getWorld().getGameDimensions().x * 3.0f / 4.0f + 800.0f * timer / 15.0f, getWorld().getCamPos(false).y + 300.0f, 0, Align.center, false);
		AssetLoader.titleFont.draw(renderer.getBatcher(), "3 Player", getWorld().getCamPos(false).x + getWorld().getGameDimensions().x / 4.0f + 800.0f * timer / 15.0f, getWorld().getCamPos(false).y + 400.0f, 0, Align.center, false);
		AssetLoader.titleFont.draw(renderer.getBatcher(), "4 Player", getWorld().getCamPos(false).x + getWorld().getGameDimensions().x * 3.0f / 4.0f + 800.0f * timer / 15.0f, getWorld().getCamPos(false).y + 400.0f, 0, Align.center, false);
		
	}
	
}