package com.mwojnar.GameObjects;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.playgon.GameEngine.Collision;
import com.playgon.GameEngine.ControllerEvent;
import com.playgon.GameEngine.Entity;
import com.playgon.GameEngine.Mask;
import com.playgon.GameEngine.TouchEvent;
import com.playgon.GameWorld.GameWorld;

public class Particle extends Entity {
	
	private boolean rotates = false, affectedByGravity = false, shrinks = false, timed = false, fading = false, isColliding = false;
	private int maxTime = 0, timeLeft = 0;
	private float gravity = 1.0f, rotationSpeed = 5.0f, gravityDirection = (float) (Math.PI / 2.0f);
	private FadeEffect fadeEffect = FadeEffect.FADEOUT;
	private ShrinkEffect shrinkEffect = ShrinkEffect.FADEOUT;
	
	List<Class<?>> collideEntities = new ArrayList<Class<?>>();
	
	public enum FadeEffect {
		
		FADEOUT, FADEIN, FADEINOUT, FADEOUTIN;
		
	}
	
	public enum ShrinkEffect {
		
		FADEOUT, FADEIN, FADEINOUT, FADEOUTIN;
		
	}
	
	public void resetTimer() {
		
		timeLeft = maxTime;
		
	}
	
	public Particle(GameWorld myWorld) {
		
		super(myWorld);
		
	}
	
	private Rectangle screenRectangle = new Rectangle(), rectangle = new Rectangle();
	
	Vector2 camPos = new Vector2(), gameDimensions = new Vector2();

	@Override
	public void update(float delta, List<TouchEvent> touchEventList, List<Character> charactersTyped, List<Integer> keysFirstDown, List<Integer> keysFirstUp, List<Integer> keysDown, List<ControllerEvent> controllerEvents) {
		
		super.update(delta, touchEventList, charactersTyped, keysFirstDown, keysFirstUp, keysDown, controllerEvents);
		
		if (getSprite() != null) {
			
			if (isColliding && !this.collideEntities.isEmpty() && getMask().isEmpty()) {
				
				setPivot(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f);
				setMask(new Mask(this, new Vector2(getSprite().getWidth() / 2.0f, getSprite().getHeight() / 2.0f), getSprite().getWidth() / 2.0f));
				
			}
			
		}
		if (isColliding) {
			
			List<Entity> collideEntities = getWorld().getEntityList();
			for (int i = 0; i < collideEntities.size(); i++) {
				
				if (this.collideEntities.contains(collideEntities.get(i).getClass())) {
					
					List<Collision> collisions = collisionsWith(collideEntities.get(i));
					if (collisions.size() > 0) {
						
						destroy();
						return;
						
					}
					
				}
				
			}
			
		}
		if (timed) {
			
			timeLeft--;
			if (timeLeft <= 0) {
				
				destroy();
				return;
				
			} else {
				
				if (shrinks) {
					
					switch (shrinkEffect) {
					
					case FADEIN: setScale(1.0f - ((float)timeLeft / (float)maxTime)); break;
					case FADEOUT: setScale((float)timeLeft / (float)maxTime); break;
					case FADEOUTIN: setScale(Math.abs((float)timeLeft - maxTime / 2.0f) * 2.0f / (float)maxTime); break;
					case FADEINOUT: setScale(1.0f - (Math.abs((float)timeLeft - maxTime / 2.0f) * 2.0f / (float)maxTime)); break;
					
					}
					
				}
				if (fading) {
					
					switch (fadeEffect) {
					
					case FADEIN: setAlpha(1.0f - ((float)timeLeft / (float)maxTime)); break;
					case FADEOUT: setAlpha((float)timeLeft / (float)maxTime); break;
					case FADEOUTIN: setAlpha(Math.abs((float)timeLeft - maxTime / 2.0f) * 2.0f / (float)maxTime); break;
					case FADEINOUT: setAlpha(1.0f - (Math.abs((float)timeLeft - maxTime / 2.0f) * 2.0f / (float)maxTime)); break;
					
					}
					
				}
				
			}
			
		}
		
		if (affectedByGravity) {
			
			//Vector2 gridVelocity = getGridVelocity();
			addRadialVelocity(gravity, gravityDirection);
			//setGridVelocity(gridVelocity.x, gridVelocity.y + gravity);
			
		}
		if (rotates) {
			
			addRotation(rotationSpeed);
			
		}
		
		camPos = getWorld().getCamPos(false);
		gameDimensions = getWorld().getGameDimensions();
		screenRectangle.set(camPos.x + 40, camPos.y - 100, gameDimensions.x - 80, gameDimensions.y + 200);
		rectangle.set(getMask().getBoundingRectangle());
		if (!getMask().isEmpty()) {
			
			if (!(screenRectangle.overlaps(rectangle.setPosition(getPos(false))))) {
				
				destroy();
				
			}
			
		} else {
			
			if (!screenRectangle.contains(getPos(false))) {
				
				destroy();
				
			}
			
		}
		
		moveByVelocity();
		
	}
	
	public boolean isRotates() {
		
		return rotates;
		
	}
	
	public Particle setRotates(boolean rotates) {
		
		this.rotates = rotates;
		return this;
		
	}
	
	public Particle setRotationSpeed(float rotationSpeed) {
		
		this.rotationSpeed = rotationSpeed;
		return this;
		
	}
	
	public boolean isAffectedByGravity() {
		
		return affectedByGravity;
		
	}
	
	public Particle setAffectedByGravity(boolean affectedByGravity) {
		
		this.affectedByGravity = affectedByGravity;
		return this;
		
	}
	
	public boolean isShrinking() {
		
		return shrinks;
		
	}
	
	public Particle setShrinking(boolean shrinks) {
		
		this.shrinks = shrinks;
		return this;
		
	}
	
	public int getMaxTime() {
		
		return maxTime;
		
	}
	
	public Particle setMaxTime(int maxTime) {
		
		this.maxTime = maxTime;
		timeLeft = maxTime;
		return this;
		
	}
	
	public Particle addCollideEntity(Class<?> collideEntityClass) {
		
		collideEntities.add(collideEntityClass);
		isColliding = true;
		return this;
		
	}
	
	public float getGravity() {
		
		return gravity;
		
	}
	
	public Particle setGravity(float gravity) {
		
		this.gravity = gravity;
		return this;
		
	}
	
	public boolean isTimed() {
		
		return timed;
		
	}
	
	public Particle setTimed(boolean timed) {
		
		this.timed = timed;
		return this;
		
	}
	
	public Particle setFading(boolean isFading) {
		
		fading = isFading;
		return this;
		
	}
	
	public boolean isFading() {
		
		return fading;
		
	}
	
	public FadeEffect getFadeEffect() {
		
		return fadeEffect;
		
	}
	
	public Particle setFadeEffect(FadeEffect fadeEffect) {
		
		this.fadeEffect = fadeEffect;
		if (fadeEffect == FadeEffect.FADEIN || fadeEffect == FadeEffect.FADEINOUT) {
			
			setAlpha(0.0f);
			
		}
		return this;
		
	}

	public Particle setShrinkEffect(ShrinkEffect fadeinout) {
		
		this.shrinkEffect = shrinkEffect;
		if (shrinkEffect == ShrinkEffect.FADEIN || shrinkEffect == ShrinkEffect.FADEINOUT) {
			
			setScale(0.0f);
			
		}
		return this;
		
	}
	
	public ShrinkEffect getShrinkEffect() {
		
		return shrinkEffect;
		
	}
	
	public float getGravityDirection() {
		
		return gravityDirection;
		
	}
	
	public void setGravityDirection(float gravityDirection) {
		
		this.gravityDirection = gravityDirection;
		
	}
	
	@Override
	public boolean checkRegion() {
		
		return true;
		
	}
	
}