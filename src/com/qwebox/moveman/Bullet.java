package com.qwebox.moveman;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class Bullet extends Sprite {
	
	private float damage;
	private final PhysicsHandler mPhysicsHandler;
	
	public Bullet(float pX, float pY, float width, float height, TextureRegion bulletTextureRegion, VertexBufferObjectManager vertexBufferObjectManager, float xVel, float yVel)
	{
		this(pX,pY,width,height,bulletTextureRegion,vertexBufferObjectManager,xVel,yVel,5f);
	}
	
	public Bullet(float pX, float pY, float width, float height, TextureRegion bulletTextureRegion, VertexBufferObjectManager vertexBufferObjectManager, float xVel, float yVel, float damage)
	{
		super(pX,pY,width,height,bulletTextureRegion,vertexBufferObjectManager);
		this.mPhysicsHandler = new PhysicsHandler(this);
		this.registerUpdateHandler(this.mPhysicsHandler);
		this.mPhysicsHandler.setVelocity(xVel,yVel);
		
		this.damage = damage;
	}
	
	public float getDamage() {
		return damage;
	}
	public void setDamage(float damage) {
		this.damage = damage;
	}
	
	public void resetBullet (float pX, float pY, float width, float height, float xVel, float yVel) {
		this.setX(pX);
		this.setY(pY);
		this.setWidth(width);
		this.setHeight(height);
		this.mPhysicsHandler.setVelocityX(xVel);
		this.mPhysicsHandler.setVelocityY(yVel);
	}
	
	public PhysicsHandler getPhysicsHandler() {
		return this.mPhysicsHandler;
	}
	
}
