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
	public static MusicHandler musicHandler;
	public static List<Class<? extends Entity>> classList = new ArrayList<Class<? extends Entity>>();
	public static List<Pair<String, Sprite>> spriteList = new ArrayList<Pair<String, Sprite>>();
	public static List<Pair<String, BackgroundTemplate>> backgroundList = new ArrayList<Pair<String, BackgroundTemplate>>();
	public static List<Pair<String, MusicTemplate>> musicList = new ArrayList<Pair<String, MusicTemplate>>();
	public static BitmapFont debugFont = new BitmapFont(true), titleFont = new BitmapFont(true);
	public static float musicVolume = 0.5f, soundVolume = 1.0f;
	
	public static void load() {
		
		dispose();
		
		loaded = false;
		
		assetManager = new AssetManager();
		
		musicHandler = new MusicHandler();
		
		wojworksTexture = new Texture(Gdx.files.internal("data/Images/spr_wojworks.png"));
		
		assetManager.load("data/Images/LudumDare41Textures.pack", TextureAtlas.class);
		
		Preferences preferences = Gdx.app.getPreferences("LudumDare41 Prefs");
		musicVolume = preferences.getFloat("musicVolume", 1.0f);
		soundVolume = preferences.getFloat("soundVolume", 0.5f);
		
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
		
	}
	
	private static void loadSoundsManager() {
		
		//assetManager.load("data/Sounds/snd_airGain.mp3", com.badlogic.gdx.audio.Sound.class);
		
	}
	
	private static void loadSounds() {
		
		//sndAirGain = LudumDare41Game.createSound(assetManager.get("data/Sounds/snd_airGain.mp3", com.badlogic.gdx.audio.Sound.class));
		
		/*sndGrpMonster = new SoundGroup();
		sndGrpMonster.add(sndMonster1);
		sndGrpMonster.add(sndMonster2);
		sndGrpMonster.add(sndMonster3);*/
		
	}
	
	private static void loadMusic() {
		
		/*mainMusic = new MusicTemplate(Gdx.files.internal("data/Music/AquaAscent_Theme.mp3"));
		mainMusic.setLooping(true);
		musicHandler.addMusic(mainMusic);*/
		
	}
	
	public static void setMusicVolume(float musicVolume) {
		
		AssetLoader.musicVolume = musicVolume;
		
	}
	
	public static void dispose() {}
	
	private static void loadTextures() {
		
		//load stuff
		
	}
	
	private static void loadSprites() {
		
		//load stuff
		
	}
	
}