package com.mwojnar.Assets;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFileChooser;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.mwojnar.Game.LudumDare41Game;
import com.mwojnar.GameObjects.Car;
import com.mwojnar.GameObjects.CarMarker;
import com.mwojnar.GameObjects.FinishLine;
import com.mwojnar.GameObjects.PolyWall;
import com.mwojnar.GameObjects.Road;
import com.playgon.GameEngine.Background;
import com.playgon.GameEngine.BackgroundTemplate;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.MaskSurface;
import com.playgon.GameEngine.MusicTemplate;
import com.playgon.GameEngine.Sound;
import com.playgon.GameEngine.SoundGroup;
import com.playgon.GameEngine.Sprite;
import com.playgon.Helpers.MusicHandler;
import com.playgon.Utils.Pair;

public class AssetLoader {

	public static boolean loaded = false;
	public static AssetManager assetManager;
	private static TextureAtlas atlas;
	public static Texture wojworksTexture;
	public static TextureRegion steeringWheelTexture, sliderTopTexture, sliderBottomTexture,
			sliderBodyTexture, sliderFasterTexture, sliderPauseTexture, sliderSlowerTexture,
			sliderTexture, dashLeftTexture, dashTopLeftTexture, dashTopTexture, dashTopRightTexture,
			dashRightTexture, whiteTexture, doneButtonTexture, carTexture, grassTexture, roadTexture,
			finishTexture, wallTexture, titleTexture;
	public static TextureRegion checkeredBackgroundTexture;
	public static Sprite spriteSteeringWheel, spriteSliderTop, spriteSliderBottom, spriteSliderBody,
			spriteSliderFaster, spriteSliderPause, spriteSliderSlower, spriteSlider, spriteDashLeft,
			spriteDashTopLeft, spriteDashTop, spriteDashTopRight, spriteDashRight, spriteWhite,
			spriteDoneButton, spriteCar, spriteRoad, spriteFinish, spriteWall, spriteTitle;
	public static BackgroundTemplate backgroundCheckered, backgroundGrass, backgroundRoad;
	public static MusicTemplate musicMain;
	public static Sound sndCrash;
	public static List<Sound> sndEngineList = new ArrayList<Sound>();
	public static MusicHandler musicHandler;
	public static List<Class<? extends Entity>> classList = new ArrayList<Class<? extends Entity>>(Arrays.asList(CarMarker.class, Road.class, PolyWall.class, FinishLine.class));
	public static List<Pair<String, Sprite>> spriteList = new ArrayList<Pair<String, Sprite>>();
	public static List<Pair<String, ?>> spriteWallBackgroundList = new ArrayList<Pair<String, ?>>();
	public static List<Pair<String, BackgroundTemplate>> backgroundList = new ArrayList<Pair<String, BackgroundTemplate>>();
	public static List<Pair<String, MusicTemplate>> musicList = new ArrayList<Pair<String, MusicTemplate>>();
	public static List<Class<? extends MaskSurface>> surfaceClassList = new ArrayList<Class<? extends MaskSurface>>();
	public static BitmapFont debugFont = new BitmapFont(true), titleFont = new BitmapFont(true), winFont = new BitmapFont(true);
	public static Color dashColor = new Color(119 / 255.0f, 28 / 255.0f, 112 / 255.0f, 1.0f);
	public static Color[] playerColors = new Color[] {Color.BLUE, Color.RED, Color.YELLOW, Color.GREEN, Color.PURPLE, Color.ORANGE, Color.CYAN, Color.BROWN};
	public static float musicVolume = 0.5f, soundVolume = 1.0f;
	
	public static void load() {
		
		dispose();
		
		loaded = false;
		
		assetManager = new AssetManager();
		
		musicHandler = new MusicHandler();
		
		wojworksTexture = new Texture(Gdx.files.internal("data/Images/spr_wojworks.png"));
		
		assetManager.load("data/Images/LudumDare41Textures.pack", TextureAtlas.class);
		
		Preferences preferences = Gdx.app.getPreferences("LudumDare41 Prefs");
		musicVolume = preferences.getFloat("musicVolume", 0.5f);
		soundVolume = preferences.getFloat("soundVolume", 1.0f);
		
		loadSoundsManager();
		
		debugFont.setUseIntegerPositions(false);
		titleFont.setUseIntegerPositions(false);
		
	}
	
	public static void postload() {
		
		atlas = assetManager.get("data/Images/LudumDare41Textures.pack", TextureAtlas.class);
		
		loadMusic();
		loadSounds();
		loadTextures();
		loadSprites();
		loadMisc();
		
	}
	
	private static void loadMisc() {
		
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("data/Fonts/pixel font-7.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 12;
		parameter.flip = true;
		parameter.color = Color.WHITE;
		debugFont = generator.generateFont(parameter);
		generator.dispose();
		
		generator = new FreeTypeFontGenerator(Gdx.files.internal("data/Fonts/pixel font-7.ttf"));
		parameter = new FreeTypeFontParameter();
		parameter.size = 64;
		parameter.flip = true;
		parameter.color = Color.WHITE;
		titleFont = generator.generateFont(parameter);
		generator.dispose();

		generator = new FreeTypeFontGenerator(Gdx.files.internal("data/Fonts/pixel font-7.ttf"));
		parameter = new FreeTypeFontParameter();
		parameter.size = 64;
		parameter.flip = true;
		parameter.color = Color.WHITE;
		winFont = generator.generateFont(parameter);
		generator.dispose();
		
	}
	
	private static void loadSoundsManager() {
		
		assetManager.load("data/Sounds/crash.mp3", com.badlogic.gdx.audio.Sound.class);
		assetManager.load("data/Sounds/engine.mp3", com.badlogic.gdx.audio.Sound.class);
		
	}
	
	private static void loadSounds() {
		
		sndCrash = LudumDare41Game.createSound(assetManager.get("data/Sounds/crash.mp3", com.badlogic.gdx.audio.Sound.class));
		sndEngineList.add(LudumDare41Game.createSound(assetManager.get("data/Sounds/engine.mp3", com.badlogic.gdx.audio.Sound.class)));
		sndEngineList.add(LudumDare41Game.createSound(assetManager.get("data/Sounds/engine.mp3", com.badlogic.gdx.audio.Sound.class)));
		sndEngineList.add(LudumDare41Game.createSound(assetManager.get("data/Sounds/engine.mp3", com.badlogic.gdx.audio.Sound.class)));
		sndEngineList.add(LudumDare41Game.createSound(assetManager.get("data/Sounds/engine.mp3", com.badlogic.gdx.audio.Sound.class)));
		
	}
	
	private static void loadMusic() {
		
		musicMain = new MusicTemplate(Gdx.files.internal("data/Music/music.mp3"));
		musicMain.setLooping(true);
		musicMain.setVolume(0.5f);
		musicHandler.addMusic(musicMain);
		
	}
	
	public static void setMusicVolume(float musicVolume) {
		
		AssetLoader.musicVolume = musicVolume;
		
	}
	
	public static void dispose() {}
	
	private static void loadTextures() {

		whiteTexture = atlas.findRegion("white");
		steeringWheelTexture = atlas.findRegion("Wheel");
		sliderTopTexture = atlas.findRegion("Slider/slider_top");
		sliderBottomTexture = atlas.findRegion("Slider/slider_bottom");
		sliderBodyTexture = atlas.findRegion("Slider/slider_body");
		sliderTexture = atlas.findRegion("Slider/slider");
		sliderFasterTexture = atlas.findRegion("Slider/Forward_button");
		sliderPauseTexture = atlas.findRegion("Slider/Stop_button");
		sliderSlowerTexture = atlas.findRegion("Slider/Backward_button");
		doneButtonTexture = atlas.findRegion("Done");
		carTexture = atlas.findRegion("car");
		grassTexture = atlas.findRegion("Grass_texture");
		roadTexture = atlas.findRegion("Road_texture");
		finishTexture = atlas.findRegion("finish");
		wallTexture = atlas.findRegion("wall_texture");
		titleTexture = atlas.findRegion("logo");

		dashLeftTexture = atlas.findRegion("Dashboard_left");
		dashTopLeftTexture = atlas.findRegion("Dashboard_left_corner");
		dashTopTexture = atlas.findRegion("Dashboard_top");
		dashTopRightTexture = atlas.findRegion("Dashboard_right_corner");
		dashRightTexture = atlas.findRegion("Dashboard_right");

		checkeredBackgroundTexture = atlas.findRegion("checkerboard");
		
	}
	
	private static void loadSprites() {

		spriteWhite = new Sprite(whiteTexture, 1);
		spriteSteeringWheel = new Sprite(steeringWheelTexture, 1);
		spriteSliderTop = new Sprite(sliderTopTexture, 1);
		spriteSliderBottom = new Sprite(sliderBottomTexture, 1);
		spriteSliderBody = new Sprite(sliderBodyTexture, 1);
		spriteSlider = new Sprite(sliderTexture, 1);
		spriteSliderFaster = new Sprite(sliderFasterTexture, 1);
		spriteSliderPause = new Sprite(sliderPauseTexture, 1);
		spriteSliderSlower = new Sprite(sliderSlowerTexture, 1);
		spriteDoneButton = new Sprite(doneButtonTexture, 1);
		spriteCar = new Sprite(carTexture, 1);
		spriteList.add(new Pair<String, Sprite>("Car", spriteCar));
		spriteRoad = new Sprite(roadTexture, 1);
		spriteList.add(new Pair<String, Sprite>("Road", spriteRoad));
		spriteWallBackgroundList.add(new Pair<String, Sprite>("Road", spriteRoad));
		spriteFinish = new Sprite(finishTexture, 1);
		spriteList.add(new Pair<String, Sprite>("Finish", spriteFinish));
		spriteWallBackgroundList.add(new Pair<String, Sprite>("Finish", spriteFinish));
		spriteWall = new Sprite(wallTexture, 1);
		spriteList.add(new Pair<String, Sprite>("Wall", spriteWall));
		spriteWallBackgroundList.add(new Pair<String, Sprite>("Wall", spriteWall));
		spriteTitle = new Sprite(titleTexture, 1);

		spriteDashLeft = new Sprite(dashLeftTexture, 1);
		spriteDashTopLeft = new Sprite(dashTopLeftTexture, 1);
		spriteDashTop = new Sprite(dashTopTexture, 1);
		spriteDashTopRight = new Sprite(dashTopRightTexture, 1);
		spriteDashRight = new Sprite(dashRightTexture, 1);

		backgroundCheckered = new BackgroundTemplate(checkeredBackgroundTexture, 1);
		backgroundList.add(new Pair<String, BackgroundTemplate>("Checkered", backgroundCheckered));
		backgroundGrass = new BackgroundTemplate(grassTexture, 1);
		backgroundList.add(new Pair<String, BackgroundTemplate>("Grass", backgroundGrass));
		backgroundRoad = new BackgroundTemplate(roadTexture, 1);
		backgroundList.add(new Pair<String, BackgroundTemplate>("Road", backgroundRoad));
		
	}
	
}