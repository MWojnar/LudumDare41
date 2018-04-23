package com.mwojnar.GameWorld;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.mwojnar.GameObjects.Car;
import com.mwojnar.GameObjects.CarMarker;
import com.mwojnar.GameObjects.Dashboard;
import com.mwojnar.GameObjects.FixtureUserData.CarUserData;
import com.mwojnar.GameObjects.FixtureUserData.FixtureUserData;
import com.mwojnar.GameObjects.RectWall;
import com.mwojnar.GameObjects.Road;
import com.mwojnar.GameObjects.Slider;
import com.mwojnar.GameObjects.SliderButton;
import com.mwojnar.GameObjects.SteeringWheel;
import com.mwojnar.GameObjects.Title;
import com.mwojnar.Game.LudumDare41Game;
import com.mwojnar.Assets.AssetLoader;
import com.mwojnar.GameObjects.Wall;
import com.mwojnar.Interfaces.ButtonSubscriber;
import com.playgon.GameEngine.AbsoluteEntity;
import com.playgon.GameEngine.Background;
import com.playgon.GameEngine.BackgroundTemplate;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.MusicTemplate;
import com.playgon.GameEngine.Sprite;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameWorld;
import com.playgon.Utils.LoadingThread;
import com.playgon.Utils.Pair;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class LudumDare41World extends GameWorld implements ButtonSubscriber {

	public enum Mode { MENU, GAME, HIGHSCORE }
	
	private Mode mode = Mode.GAME;
	private LoadingThread loadingThread = null;
	private boolean showFPS = true, paused = false, started = false;
	private float dashWidth = 400.0f, dashHeight = 150.0f;
	private long framesSinceLevelCreation = 0;
	private int simTimer = 0, simTime = 10, carTurn = 0, transTimer = 0, transTimerMax = 60,
				transCamTimerMax = 30, transToCarMax = 30, transBackTimer = 0;
	private FileHandle levelToLoad = null;
	private Random rand = new Random();
	private Background mainBackground;
	private World physicsWorld = null;
	private SteeringWheel wheel = null;
	private Slider slider = null;
	private List<Car> carList = new ArrayList<Car>();
	
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
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject)jsonParser.parse(new FileReader("CarAttributes.txt"));
			simTime = Integer.parseInt((String)jsonObject.get("Seconds of simulation each round"));
		} catch (Exception e) {
			//e.printStackTrace();
		}
		//START PLAYING MUSIC
		AssetLoader.musicHandler.startMusic(AssetLoader.musicMain);

		/*boolean loadMenus = true;
		if (LudumDare41Game.args != null) {

			for (int i = 0; i < LudumDare41Game.args.length; i++) {

				if (LudumDare41Game.args[i].equals("-loadLevel")) {

					if (i + 1 < LudumDare41Game.args.length) {

						clearWorld();
						trueLoadLevel(Gdx.files.absolute(LudumDare41Game.args[i + 1]));
						startGameAlt();
						loadMenus = false;
						break;

					}

				}

			}

		}
		if (loadMenus)
			startMenu();*/
		clearWorld();
		trueLoadLevel(Gdx.files.internal("Level1.ple"));
		startGameAlt();
		
		addBackgrounds();
		
		//setLudumDare41View();
		/*if (loadMenus) {
			
			startMenu();
			
		}*/
		
	}
	
	public void startGame() {
		
		AssetLoader.debugFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		clearWorld();
		setCamPos(new Vector2(getGameDimensions().x / 2.0f, getGameDimensions().y / 2.0f));
		mode = Mode.GAME;
		framesSinceLevelCreation = 0;
		carList.clear();
		endSim();
		carTurn = 0;

		physicsWorld.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				// Check to see if the collision is between the second sprite and the bottom of the screen
				// If so apply a random amount of upward force to both objects... just because
				int test = 0;
			}

			@Override
			public void endContact(Contact contact) {
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

		Dashboard dashboard = new Dashboard(this);
		dashboard.setPos(getGameDimensions().x / 2.0f - dashWidth / 2.0f, getGameDimensions().y - dashHeight, false);
		dashboard.setWidth(dashWidth);
		dashboard.setHeight(dashHeight);
		createEntity(dashboard);
		SliderButton doneButton = new SliderButton(this, 0);
		doneButton.setSprite(AssetLoader.spriteDoneButton);
		doneButton.setPos(dashboard.getPos(false).x + 10.0f, dashboard.getPos(false).y + dashboard.getHeight() - doneButton.getSprite().getHeight() - 10.0f, false);
		doneButton.subscribe(this);
		createEntity(doneButton);
		wheel = new SteeringWheel(this);
		wheel.setPos(dashboard.getPos(false).x + doneButton.getSprite().getWidth() + 50.0f, dashboard.getPos(false).y + 10.0f, false);
		createEntity(wheel);
		slider = new Slider(this);
		slider.setPos(dashboard.getPos(false).x + dashboard.getWidth() - slider.getSprite().getWidth() * 2.0f - 10.0f, dashboard.getPos(false).y + 10.0f, false);
		createEntity(slider);
		for (int i = 0; i < 2; i++) {
			Car car = new Car(this);
			car.setPos(100.0f * (i + 1), 100.0f * (i + 1), false);
			car.setColor(AssetLoader.playerColors[i]);
			createEntity(car);
			carList.add(car);
		}
		RectWall wall = new RectWall(this, new Vector2(-30000, 590), new Vector2(10000, 600));
		createEntity(wall);
		wall = new RectWall(this, new Vector2(-30000, -100), new Vector2(10000, -90));
		createEntity(wall);
		moveViewToEntity(carList.get(0), transToCarMax);

		Background background = new Background(AssetLoader.backgroundGrass);
		background.setTilingY(true);
		background.setTilingX(true);
		addBackground(background);
	}

	private void startGameAlt() {
		AssetLoader.debugFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		setCamPos(new Vector2(getGameDimensions().x / 2.0f, getGameDimensions().y / 2.0f));
		mode = Mode.GAME;
		framesSinceLevelCreation = 0;
		carList.clear();
		endSim();
		carTurn = 0;

		physicsWorld.setContactListener(new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				if (contact.getFixtureA().getUserData() != null && contact.getFixtureB().getUserData() != null) {
					FixtureUserData a = (FixtureUserData) contact.getFixtureA().getUserData();
					FixtureUserData b = (FixtureUserData) contact.getFixtureB().getUserData();
					if (a.parent instanceof Car && b.parent instanceof Road)
						((Car)a.parent).addRoadCollision((Road)b.parent);
					else if (a.parent instanceof Road && b.parent instanceof Car)
						((Car)b.parent).addRoadCollision((Road)a.parent);
				}
			}

			@Override
			public void endContact(Contact contact) {
				if (contact.getFixtureA().getUserData() != null && contact.getFixtureB().getUserData() != null) {
					FixtureUserData a = (FixtureUserData) contact.getFixtureA().getUserData();
					FixtureUserData b = (FixtureUserData) contact.getFixtureB().getUserData();
					if (a.parent instanceof Car && b.parent instanceof Road)
						((Car) a.parent).removeRoadCollision((Road) b.parent);
					else if (a.parent instanceof Road && b.parent instanceof Car)
						((Car) b.parent).removeRoadCollision((Road) a.parent);
				}
			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {
			}
		});

		Dashboard dashboard = new Dashboard(this);
		dashboard.setPos(getGameDimensions().x / 2.0f - dashWidth / 2.0f, getGameDimensions().y - dashHeight, false);
		dashboard.setWidth(dashWidth);
		dashboard.setHeight(dashHeight);
		createEntity(dashboard);
		SliderButton doneButton = new SliderButton(this, 0);
		doneButton.setSprite(AssetLoader.spriteDoneButton);
		doneButton.setPos(dashboard.getPos(false).x + 10.0f, dashboard.getPos(false).y + dashboard.getHeight() - doneButton.getSprite().getHeight() - 10.0f, false);
		doneButton.subscribe(this);
		createEntity(doneButton);
		wheel = new SteeringWheel(this);
		wheel.setPos(dashboard.getPos(false).x + doneButton.getSprite().getWidth() + 50.0f, dashboard.getPos(false).y + 10.0f, false);
		createEntity(wheel);
		slider = new Slider(this);
		slider.setPos(dashboard.getPos(false).x + dashboard.getWidth() - slider.getSprite().getWidth() * 2.0f - 10.0f, dashboard.getPos(false).y + 10.0f, false);
		createEntity(slider);
		for (Entity entity : getEntityList()) {
			if (entity instanceof CarMarker && ((CarMarker)entity).getPlace() == 1) {
				CarMarker carMarker = ((CarMarker)entity);
				Car car = new Car(this);
				car.setColor(AssetLoader.playerColors[0]);
				car.setPos(carMarker.getPos(true), false);
				car.setRotation(carMarker.getRotation());
				carList.add(car);
				createEntity(car);
				destroyEntity(carMarker);
				break;
			}
		}
		for (Entity entity : getEntityList()) {
			if (entity instanceof CarMarker && ((CarMarker)entity).getPlace() == 2) {
				CarMarker carMarker = ((CarMarker)entity);
				Car car = new Car(this);
				car.setColor(AssetLoader.playerColors[1]);
				car.setPos(carMarker.getPos(true), false);
				car.setRotation(carMarker.getRotation());
				carList.add(car);
				createEntity(car);
				destroyEntity(carMarker);
				break;
			}
		}
		for (Entity entity : getEntityList()) {
			if (entity instanceof CarMarker && ((CarMarker)entity).getPlace() == 3) {
				CarMarker carMarker = ((CarMarker)entity);
				Car car = new Car(this);
				car.setColor(AssetLoader.playerColors[2]);
				car.setPos(carMarker.getPos(true), false);
				car.setRotation(carMarker.getRotation());
				carList.add(car);
				createEntity(car);
				destroyEntity(carMarker);
				break;
			}
		}
		for (Entity entity : getEntityList()) {
			if (entity instanceof CarMarker && ((CarMarker)entity).getPlace() == 4) {
				CarMarker carMarker = ((CarMarker)entity);
				Car car = new Car(this);
				car.setColor(AssetLoader.playerColors[3]);
				car.setPos(carMarker.getPos(true), false);
				car.setRotation(carMarker.getRotation());
				carList.add(car);
				createEntity(car);
				destroyEntity(carMarker);
				break;
			}
		}
		moveViewToEntity(carList.get(0), transToCarMax);
	}

	private void startSim() {
		transTimer = transTimerMax;
		setPhysicsView(transCamTimerMax);
		for (Entity entity : getEntityList())
			if (entity instanceof AbsoluteEntity) {
				entity.setVisible(false);
				entity.setActive(false);
			}
	}

	private void endSim() {
		simTimer = 0;
		for (Entity entity : getEntityList())
			if (entity instanceof AbsoluteEntity) {
				entity.setVisible(true);
				entity.setActive(true);
			}
		carTurn = 0;
		getRenderer().setCamDimensions(800, 480);
	}
	
	private void addBackgrounds() {

		//Add backgrounds.
		
	}
	
	public void startMenu() {
		

		AssetLoader.debugFont.setColor(1.0f, 1.0f, 1.0f, 1.0f);
		clearWorld();
		setCamPos(new Vector2(getGameDimensions().x / 2.0f, getGameDimensions().y / 2.0f));
		mode = Mode.MENU;
		Title title = new Title(this);
		createEntity(title);
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
				if (transTimer > 0) {
					transTimer--;
					if (transTimer <= 0)
						simTimer = simTime * 60;
				}
				if (transBackTimer > 0) {
					transBackTimer--;
					if (transBackTimer <= 0)
						endSim();
				}
				if (simTimer > 0) {
					physicsWorld.step(1.0f / 60.0f, 6, 2);
					physicsWorld.clearForces();
					simTimer--;
					if (simTimer <= 0) {
						transBackTimer = transToCarMax + 2;
						if (!carList.isEmpty()) {
							getForces(0);
							moveViewToEntity(carList.get(0), transToCarMax);
						}
					}
					setPhysicsView(-1);
				}
				
			}
			super.update(delta);
			
		}
		
	}

	private void setPhysicsView(int timerMax) {
		setViewEntity(null);
		float gameHeight = 480;
		float gameWidth = 800;
		float minX, maxX, minY, maxY;
		minX = carList.get(0).getPos(true).x;
		maxX = minX;
		minY = carList.get(0).getPos(true).y;
		maxY = minY;
		for (int i = 1; i < carList.size(); i++) {
			if (carList.get(i).getPos(true).x < minX)
				minX = carList.get(i).getPos(true).x;
			if (carList.get(i).getPos(true).x > maxX)
				maxX = carList.get(i).getPos(true).x;
			if (carList.get(i).getPos(true).y < minY)
				minY = carList.get(i).getPos(true).y;
			if (carList.get(i).getPos(true).y > maxY)
				maxY = carList.get(i).getPos(true).y;
		}
		minX -= 100;
		maxX += 100;
		minY -= 100;
		maxY += 100;
		float initWidth = maxX - minX;
		float initHeight = maxY - minY;
		float width = initWidth;
		float height = initHeight;
		if (width / height > gameWidth / gameHeight)
			height = width * gameHeight / gameWidth;
		else
			width = height * gameWidth / gameHeight;
		minX += (initWidth - width) / 2.0f;
		minY += (initHeight - height) / 2.0f;

		if (timerMax > 0)
			getRenderer().smoothViewSizeTransitionAbsolute(width, height, timerMax, minX + width / 2.0f, minY + height / 2.0f);
		else {
			getRenderer().setCamDimensions(width, height);
			getRenderer().setCamPos(new Vector2(minX + width / 2.0f, minY + height / 2.0f));
			//getRenderer().forceViewSizeTransitionAbsolute(width, height, minX + width / 2.0f, minY + height / 2.0f);
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
			nextTurn();
	}

	private void nextTurn() {
		if (!carList.isEmpty()) {
			carList.get(carTurn).setForces();
			carTurn++;
			if (carTurn >= carList.size()) {
				carTurn = 0;
				startSim();
			} else {
				getForces(carTurn);
				moveViewToEntity(carList.get(carTurn), transToCarMax);
			}
		}
	}

	private void moveViewToEntity(Entity entity, int frames) {
		getRenderer().smoothViewSizeTransitionAbsolute(800.0f, 480.0f, frames, entity.getPos(true).x, entity.getPos(true).y);
	}

	private void getForces(int turn) {
		wheel.setTargetRotation(carList.get(turn).getTargetRotation());
		slider.setSpeed(carList.get(turn).getTargetSpeed());
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

	public boolean isSim() {
		return simTimer > 0;
	}
}