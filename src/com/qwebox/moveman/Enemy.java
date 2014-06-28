package com.qwebox.moveman;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Enemy extends Sprite {

	private final float SPEED = 40f;
	private Man manToChase;
	private PhysicsHandler mPhysicsHandler;
	
	
	public Enemy(float pX, float pY, float width, float height, TextureRegion enemyTextureRegion, VertexBufferObjectManager vertexBufferObjectManager, Man manToChase)
	{
		super(pX,pY,width,height,enemyTextureRegion,vertexBufferObjectManager);
		this.manToChase = manToChase;
		this.mPhysicsHandler = new PhysicsHandler(this);
		this.registerUpdateHandler(this.mPhysicsHandler);
		this.adjustToChase();
	}

	private void adjustToChase() {
		float x1 = this.getX();
		float y1 = this.getY();
		float x2 = manToChase.getX();
		float y2 = manToChase.getY();
		
		float deltaX = x2-x1;
		float deltaY = y2-y1;
		
		double r = Math.sqrt(Math.pow(deltaX,2)+Math.pow(deltaY,2));
		float vX = (float)((SPEED*deltaX)/r);
		float vY = (float)((SPEED*deltaY)/r);
		
		this.mPhysicsHandler.setVelocity(vX,vY);
	}
	
	
	@Override
	protected void onManagedUpdate(final float pSecondsElapsed) {
		this.adjustToChase();
		super.onManagedUpdate(pSecondsElapsed);
	}
}
