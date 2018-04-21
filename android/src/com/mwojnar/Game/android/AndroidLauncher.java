package com.mwojnar.Game.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.mwojnar.Game.LudumDare41Game;
import com.mwojnar.Screens.GameScreen;

public class AndroidLauncher extends AndroidApplication {
	
	
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		AndroidSound.androidLauncher = this;
		//PlaygonGame.soundClass = AndroidSound.class;
		initialize(new LudumDare41Game(GameScreen.class), config);
	}
	
}