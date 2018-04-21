package com.mwojnar.Game;

import java.lang.reflect.InvocationTargetException;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.files.FileHandle;
import com.playgon.GameEngine.Sound;
import com.playgon.GameEngine.PlaygonSound;

public class LudumDare41Game extends Game {
	
	public static String[] args = null;
	public static Class<? extends Sound> soundClass = PlaygonSound.class;
	private Class<Screen> screenClass = null;
	
	public static Sound createSound(FileHandle fileHandle) {
		
		try {
			Sound sound = soundClass.newInstance();
			sound.load(fileHandle);
			return sound;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public static Sound createSound(com.badlogic.gdx.audio.Sound libGDXSound) {
		
		try {
			Sound sound = soundClass.newInstance();
			sound.load(libGDXSound);
			return sound;
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
	
	public LudumDare41Game(Class<?> screenClass) {
		
		super();
		this.screenClass = (Class<Screen>) screenClass;
		
	}
	
	public LudumDare41Game(String[] args, Class<?> screenClass) {
		
		super();
		this.screenClass = (Class<Screen>) screenClass;
		LudumDare41Game.args = args;
		
	}
	
	@Override
	public void create() {
		
		try {
			setScreen((Screen) screenClass.getConstructors()[0].newInstance());
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
	
}