package com.mwojnar.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameWorld.LudumDare41Renderer;
import com.mwojnar.GameWorld.LudumDare41World;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Helpers.InputHandler;

public class GameScreen implements Screen {

	private GameWorld world;
	private GameRenderer renderer;
	private float runTime;
	
	public GameScreen() {
		
		AssetLoader.load();
		float screenWidth = Gdx.graphics.getWidth();
		float screenHeight = Gdx.graphics.getHeight();
		float gameHeight = 720;
		float gameWidth = 1080;
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(false);
		
		world = new LudumDare41World();
		Gdx.input.setInputProcessor(new InputHandler(world, gameWidth, gameHeight));
		renderer = new LudumDare41Renderer(world, (int) gameWidth, (int) gameHeight);
		world.setRenderer(renderer);
		
	}
	
	@Override
	public void render(float delta) {
		runTime += delta;
		world.update(delta);
		renderer.render(delta, runTime);
	}
	
	@Override
	public void resize(int width, int height) {}

	@Override
	public void show() {}

	@Override
	public void hide() {}

	@Override
	public void pause() {}

	@Override
	public void resume() {}

	@Override
	public void dispose() {
		
		AssetLoader.dispose();
		
	}

}