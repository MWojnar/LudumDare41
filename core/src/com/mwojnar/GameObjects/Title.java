package com.mwojnar.GameObjects;

import java.util.List;

import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

public class Title extends Entity {
	
	public Title(GameWorld myWorld) {
		
		super(myWorld);
		
	}
	
	@Override
	public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
		
		super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);
		
		if (keysFirstUp.contains(com.badlogic.gdx.Input.Keys.SPACE))
			((LudumDare41World)getWorld()).startGame();
		
	}
	
	@Override
	public void draw(GameRenderer renderer) {
		
		super.draw(renderer);
		
	}
	
}