package com.mwojnar.GameWorld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.mwojnar.Assets.AssetLoader;
import com.playgon.GameWorld.GameRenderer;
import com.playgon.GameWorld.GameWorld;

public class LudumDare41Renderer extends GameRenderer {
	
	public static int timeSinceStart = 0;
	
	public LudumDare41Renderer(GameWorld world, int gameWidth, int gameHeight) {
		
		super(world, gameWidth, gameHeight);
        
	}
	
	@Override
	public void render(float delta, float runTime) {
		
		if (AssetLoader.assetManager.update()) {
			
			if (!AssetLoader.loaded) {
				
				//AssetLoader.assetManager.finishLoading();
				AssetLoader.loaded = true;
				AssetLoader.postload();
				getMyWorld().initialize();
				
			}
			
			getBatcher().totalRenderCalls = 0;
			
			long beforeTime = System.currentTimeMillis();
			super.render(delta, runTime);
			long afterTime = System.currentTimeMillis();
			timeSinceStart++;
			if (timeSinceStart < 0) {
				
				timeSinceStart = 0;
				
			}
			
			//getShapeRenderer().begin(ShapeType.Filled);
			//getShapeRenderer().ellipse(getMyWorld().getViewEllipseCenter2().x - getDimensions().x / 7.0f, getMyWorld().getViewEllipseCenter2().y - getDimensions().y / 7.0f, getDimensions().x / 3.5f, getDimensions().y / 3.5f);
			//getShapeRenderer().ellipse(getMyWorld().getViewEllipseCenter().x - getDimensions().x / 4.0f, getMyWorld().getViewEllipseCenter().y - getDimensions().y / 4.0f, getDimensions().x / 2.0f, getDimensions().y / 2.0f);
			//getShapeRenderer().circle(getCamPos(false).x + getDimensions().x / 2.0f, getMyWorld().getViewEntityPosY(), 5);
			//getShapeRenderer().end();
			
			if (((LudumDare41World) getMyWorld()).isPaused()) {
				
				/*getBatcher().begin();
				dribbleText.drawAbsolute(this, getDimensions().x / 2.0f, getDimensions().y / 2.0f - 10.0f, "Paused", 1.0f, 1.0f, 0.0f, DribbleText.Align.CENTER, DribbleText.Align.CENTER, 20, AssetLoader.greenTextColor);
				dribbleText.drawAbsolute(this, getDimensions().x - 10.0f, getDimensions().y - 20.0f, "Main Menu", 1.0f, 1.0f, 0.0f, DribbleText.Align.RIGHT, DribbleText.Align.RIGHT, 20, AssetLoader.greenTextColor);
				getBatcher().end();*/
				
			}
			
			if (((LudumDare41World) getMyWorld()).isLoading()) {
				
	//			SpriteBatch batch = getBatcher();
	//			batch.begin();
	//			float prevScale = AssetLoader.debugFont.getScaleX();
	//			Color prevColor = AssetLoader.debugFont.getColor();
	//			AssetLoader.debugFont.getData().setScale(2);
	//			AssetLoader.debugFont.setColor(Color.WHITE);
	//			Gdx.gl.glClearColor(0, 0, 0, 0);
	//			AssetLoader.debugFont.draw(batch, "LOADING", 0, 0);
	//			AssetLoader.debugFont.getData().setScale(prevScale);
	//			AssetLoader.debugFont.setColor(prevColor);
	//			batch.end();
				
				/*getBatcher().begin();
				this.setCamPos(new Vector2(getMyWorld().getGameDimensions().x / 2.0f, getMyWorld().getGameDimensions().y / 2.0f));
				AssetLoader.spriteLevelEndTile.drawTiled(0, 0.0f, 0.0f, getMyWorld().getGameDimensions().x, getMyWorld().getGameDimensions().y, this);
				Color previousColor = new Color(getBatcher().getColor());
				getBatcher().setColor(AssetLoader.blueTextColor);
				AssetLoader.spriteLoading.draw(getMyWorld().getGameDimensions().x / 2.0f - AssetLoader.spriteLoading.getWidth() / 2.0f, getMyWorld().getGameDimensions().y / 2.0f - AssetLoader.spriteLoading.getHeight() / 2.0f, (timeSinceStart / 15) % 3, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, this);
				getBatcher().setColor(previousColor);
				getBatcher().end();*/
				
			}
			
	//		if (((DribbleWorld)this.getMyWorld()).isShowFPS()) {
	//			
	//			SpriteBatch batch = getBatcher();
	//			batch.begin();
	//			//ShapeRenderer shapes = getShapeRenderer();
	//			//shapes.begin(ShapeType.Filled);
	//			//shapes.setColor(Color.BLACK);
	//			//shapes.rect(getCamPos(false).x, getCamPos(false).y, AssetLoader.debugFont.getBounds(Integer.toString((int)this.getMyWorld().getAverageFPS())).width * 2, AssetLoader.debugFont.getBounds(Integer.toString((int)this.getMyWorld().getTrueFPS())).height * 2);
	//			//shapes.end();
	//			float prevScale = AssetLoader.debugFont.getScaleX();
	//			Color prevColor = AssetLoader.debugFont.getColor();
	//			AssetLoader.debugFont.getData().setScale(2);
	//			AssetLoader.debugFont.setColor(Color.RED);
	//			//IntBuffer buf = BufferUtils.newIntBuffer(16);
	//			//Gdx.gl.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, buf);
	//			//int maxSize = buf.get(0);
	//			AssetLoader.debugFont.draw(batch, Integer.toString(/*maxSize*/(int)getMyWorld().getAverageFPS()), getCamPos(false).x, getCamPos(false).y + getDimensions().y - AssetLoader.debugFont.getLineHeight());
	//			AssetLoader.debugFont.getData().setScale(prevScale);
	//			AssetLoader.debugFont.setColor(prevColor);
	//			batch.end();
	//			
	//		}
			
			if (((LudumDare41World) getMyWorld()).getLevelToLoad() != null) {
				
				/*getBatcher().begin();
				this.setCamPos(new Vector2(getMyWorld().getGameDimensions().x / 2.0f, getMyWorld().getGameDimensions().y / 2.0f));
				AssetLoader.spriteLevelEndTile.drawTiled(0, 0.0f, 0.0f, getMyWorld().getGameDimensions().x, getMyWorld().getGameDimensions().y, this);
				Color previousColor = new Color(getBatcher().getColor());
				getBatcher().setColor(AssetLoader.blueTextColor);
				AssetLoader.spriteLoading.draw(getMyWorld().getGameDimensions().x / 2.0f - AssetLoader.spriteLoading.getWidth() / 2.0f, getMyWorld().getGameDimensions().y / 2.0f - AssetLoader.spriteLoading.getHeight() / 2.0f, (timeSinceStart / 15) % 3, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f, this);
				getBatcher().setColor(previousColor);
				getBatcher().end();*/
				
			}
			
		} else {
			
			Gdx.gl.glClearColor(Color.BLACK.r, Color.BLACK.g, Color.BLACK.b, 1);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			getBatcher().begin();
			getBatcher().draw(AssetLoader.wojworksTexture, getDimensions().x / 2.0f - AssetLoader.wojworksTexture.getWidth() * 2.0f, getDimensions().y / 2.0f - AssetLoader.wojworksTexture.getHeight() * 2.0f, AssetLoader.wojworksTexture.getWidth() * 4.0f, AssetLoader.wojworksTexture.getHeight() * 4.0f, 0, 0, AssetLoader.wojworksTexture.getWidth(), AssetLoader.wojworksTexture.getHeight(), false, true);
			getBatcher().end();
			getShapeRenderer().setAutoShapeType(true);
			getShapeRenderer().begin();
			getShapeRenderer().set(ShapeType.Filled);
			getShapeRenderer().setColor(Color.WHITE);
			getShapeRenderer().rect(0.0f, 600.0f, getDimensions().x * AssetLoader.assetManager.getProgress(), 10.0f);
			getShapeRenderer().end();
			
		}
		
	}
	
}