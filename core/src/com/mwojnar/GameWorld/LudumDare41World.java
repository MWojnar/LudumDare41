package com.mwojnar.GameWorld;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.mwojnar.Game.LudumDare41Game;
import com.mwojnar.GameObjects.Car;
import com.mwojnar.GameObjects.Dashboard;
import com.mwojnar.GameObjects.Slider;
import com.mwojnar.GameObjects.SliderButton;
import com.mwojnar.GameObjects.SteeringWheel;
import com.mwojnar.GameObjects.Title;
import com.mwojnar.GameWorld.LudumDare41World.Mode;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.Interfaces.ButtonSubscriber;
import com.playgon.GameEngine.Background;
import com.playgon.GameEngine.BackgroundTemplate;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.MusicTemplate;
import com.playgon.GameEngine.Sprite;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Utils.LoadingThread;
import com.playgon.Utils.Pair;
import com.playgon.Utils.PlaygonMath;

public class LudumDare41World extends GameWorld implements ButtonSubscriber {

	public enum Mode { MENU, GAME, HIGHSCORE }
	
	private Mode mode = Mode.GAME;
	private LoadingThread loadingThread = null;
	private boolean showFPS = true, paused = false, started = false;
	private float nextSpawnPos = 0.0f, dashWidth = 400.0f, dashHeight = 150.0f;
	private long framesSinceLevelCreation = 0;
	private int lastChoice = -1, timerBonus = 0, rawScore = 0;
	private FileHandle levelToLoad = null;
	private Random rand = new Random();
	private Background mainBackground;
	private World physicsWorld = null;
	private SteeringWheel wheel = null;
	private Slider slider = null;
	
	public LudumDare41World() {
		
		super();
		setUsingRegions(false);
		
	}

	@Override
	public void initialize() {
		
		setFPS(60);
		initializeLevelEditorLists();
		getRenderer().setUsingIntegerViewPosition(false);
		getRenderer().setClearColor(Color.BLACK);
		Preferences preferences = Gdx.app.getPreferences("LudumDare41 Prefs");
		physicsWorld = new World(new Vector2(), true);
		//START PLAYING MUSIC
		//AssetLoader.musicHandler.startMusic(AssetLoader.mainMusic);
		
		startMenu();
		
		addBackgrounds();
		
		//setLudumDare41View();
		/*if (loadMenus) {
			
			startMenu();
			
		}*/
		
	}
	
	public void startGame() {
		
		AssetLoader.debugFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		nextSpawnPos = 0.0f;
		clearWorld();
		setCamPos(new Vector2(getGameDimensions().x / 2.0f, getGameDimensions().y / 2.0f));
		mode = Mode.GAME;
		framesSinceLevelCreation = 0;
		started = false;
		
	}
	
	private void addBackgrounds() {

		//Add backgrounds.
		
	}
	
	public void startMenu() {
		

		AssetLoader.debugFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		clearWorld();
		setCamPos(new Vector2(getGameDimensions().x / 2.0f, getGameDimensions().y / 2.0f));
		mode = Mode.MENU;
		/*Title title = new Title(this);
		createEntity(title);*/
		Dashboard dashboard = new Dashboard(this);
		dashboard.setPos(getGameDimensions().x / 2.0f - dashWidth / 2.0f, getGameDimensions().y - dashHeight, false);
		dashboard.setWidth(dashWidth);
		dashboard.setHeight(dashHeight);
		createEntity(dashboard);
		SliderButton doneButton = new SliderButton(this, 0);
		doneButton.setSprite(AssetLoader.spriteDoneButton);
		doneButton.setPos(dashboard.getPos(false).x + 10.0f, dashboard.getPos(false).y + dashboard.getHeight() - doneButton.getSprite().getHeight() - 10.0f, false);
		createEntity(doneButton);
		wheel = new SteeringWheel(this);
		wheel.setPos(dashboard.getPos(false).x + doneButton.getSprite().getWidth() + 50.0f, dashboard.getPos(false).y + 10.0f, false);
		createEntity(wheel);
		slider = new Slider(this);
		slider.setPos(dashboard.getPos(false).x + dashboard.getWidth() - slider.getSprite().getWidth() * 2.0f - 10.0f, dashboard.getPos(false).y + 10.0f, false);
		createEntity(slider);
		Car car = new Car(this);
		car.setPos(100.0f, 100.0f, false);
		car.canMove = true;
		createEntity(car);
		setViewEntity(car);
		setViewOffset(new Vector2(0.0f, 0.0f));
		setViewSpeed(0.0f);
		car = new Car(this);
		car.setPos(200.0f, 200.0f, false);
		createEntity(car);
		Background background = new Background(AssetLoader.backgroundCheckered);
		background.setTilingY(true);
		background.setTilingX(true);
		addBackground(background);
		
	}

	public static void initializeLevelEditorLists() {
		
		levelEditorLists.clear();
		levelEditorListClasses.clear();
		addToLevelEditorLists(new ArrayList<Pair<String, ?>>(AssetLoader.spriteList), Sprite.class);
		addToLevelEditorLists(new ArrayList<Pair<String, ?>>(AssetLoader.backgroundList), BackgroundTemplate.class);
		addToLevelEditorLists(new ArrayList<Pair<String, ?>>(AssetLoader.musicList), MusicTemplate.class);
		
	}
	
	@Override
	public void update(float delta) {
		
		if (AssetLoader.loaded) {
			
			if (!paused) {
				
				framesSinceLevelCreation++;
				physicsWorld.step(delta, 6, 2);
				physicsWorld.clearForces();
				
			}
			super.update(delta);
			
		}
		
	}
	
	
	@Override
	protected void updateMain(float delta) {

		if (paused) {

			float scale = getGameDimensions().y / 240.0f;
			for (TouchEvent touchEvent : getCurrentTouchEventList()) {

				Rectangle rect2 = new Rectangle(getGameDimensions().x - 10.0f - 9.0f * 15.0f, getGameDimensions().y - 30.0f, 9.0f * 15.0f, 20.0f);
				if (touchEvent.type == TouchEvent.Type.TOUCH_UP) {

					setPaused(false);
					//AssetLoader.soundUIUnpausing.play(AssetLoader.soundVolume);
					return;

				}

			}

		} else if (levelToLoad == null) {

			super.updateMain(delta);
			if (mode == Mode.GAME) {

				//game logic

			} else if (mode == Mode.MENU) {

				//setCamPos(new Vector2(getCamPos(true).x, getCamPos(true).y - 15));

			}

		} else {

			trueLoadLevel(levelToLoad);
			levelToLoad = null;

		}

	}

	public boolean isPaused() {
		
		return paused;
		
	}
	
	public void setPaused(boolean paused) {
		
		this.paused = paused;
		if (paused) {
			
			AssetLoader.musicHandler.setVolume(AssetLoader.musicVolume * 0.25f);
			
		} else {
			
			AssetLoader.musicHandler.setVolume(AssetLoader.musicVolume);
			
		}
		
	}
	
	public void loadLevel(FileHandle file) {
		
		levelToLoad = file;
		
	}
	
	public void trueLoadLevel(FileHandle file) {
		
		AssetLoader.musicHandler.unload();
		AssetLoader.musicHandler.stopMusic();
		
//		if (loadingThread == null) {
//			
//			loadingThread = new LoadingThread(this, file, levelEditorListClasses, levelEditorLists);
//			loadingThread.start();
//			
//		}
		
		load(file, levelEditorListClasses, levelEditorLists);
		
		addEntities();
		
		/*BackgroundShape backgroundShape = new BackgroundShape(this, getCamPos(false));
		backgroundShape.setPos(dribbleEntity.getPos(true), false);
		backgroundShape.width = 200.0f;
		backgroundShape.height = 100.0f;
		createEntity(backgroundShape);*/
		
		framesSinceLevelCreation = 0;
		
	}
	
	@Override
	protected void loadFromMultiThread() {
		
		super.loadFromMultiThread();
		
		addEntities();
		
		/*BackgroundShape backgroundShape = new BackgroundShape(this, getCamPos(false));
		backgroundShape.setPos(dribbleEntity.getPos(true), false);
		backgroundShape.width = 200.0f;
		backgroundShape.height = 100.0f;
		createEntity(backgroundShape);*/
		
		framesSinceLevelCreation = 0;
		loadingThread = null;
		
	}
	
	public void clearWorld() {
		
		for (Entity entity : getEntityList()) {
			
			entity.destroy();
			
		}
		resetEntities();
		releaseEntities();
		getEntityList().clear();
		getActiveEntityList().clear();
		
	}

	public boolean isLoading() {
		
		return (loadingThread != null);
		
	}

	public boolean isShowFPS() {
		
		return showFPS;
		
	}

	public void setShowFPS(boolean showFPS) {
		
		this.showFPS = showFPS;
		
	}
	
	@Override
	public void restartLevel() {
		
		super.restartLevel();
		
	}
	
	public long getFramesSinceLevelCreation() {
		
		return framesSinceLevelCreation;
		
	}
	
	public void endLevel() {
		
		clear();
		
	}
	
	public void setLudumDare41View() {
		
		setViewOffset(new Vector2(0.0f, 0.0f));
		setViewSpeed(0.0f);
		setViewYield(new Vector2(0.0f, 0.0f));
		toggleViewPredictPath(true);
		toggleViewAccelerateToPoint(true);
		
	}

	public FileHandle getLevelToLoad() {
		
		return levelToLoad;
		
	}
	
	public Random getRandom() {
		
		return rand;
		
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		
		if (!this.started) {
			
			framesSinceLevelCreation = 0;
			
		}
		this.started = started;
		
	}

	@Override
	public void notify(int id) {
		if (id == 0)
			return; //TODO: Add logic for pressing "done" button
	}

	public World getPhysicsWorld() {
		return physicsWorld;
	}

	public float getTargetRotation() {
		if (wheel == null)
			return 0.0f;
		return (wheel.getTargetRotation());
	}

	public float getSpeed() {
		if (slider == null)
			return 0.0f;
		return (slider.getSpeed());
	}
}