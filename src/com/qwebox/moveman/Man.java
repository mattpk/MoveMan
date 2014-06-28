package com.qwebox.moveman;

import org.andengine.entity.modifier.MoveModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.modifier.ease.EaseSineInOut;

import android.util.Log;

public class Man extends Sprite {

	public static final float MOVE_SPEED = 0.075f;
	private final int MOVEDIST = 64;
	private int direction = 0;
	
	public Man(float pX, float pY, float width, float height, TextureRegion manTextureRegion, VertexBufferObjectManager vertexBufferObjectManager)
	{
		super(pX,pY,width,height,manTextureRegion,vertexBufferObjectManager);
	}
	
	// Move by a 64 pixel swipe
	// 0 up 1 right 2 down 3 left
	public void moveSwipe(int direction) {
		this.direction = direction;
		switch (direction) {
		case 0:
			// up
			if (this.getY()+MOVEDIST < MoveManActivity.CAMERA_HEIGHT)
			this.swipeYTo(this.getY()+MOVEDIST);
			break;
		case 1:
			// right
			if (this.getX() + MOVEDIST < MoveManActivity.CAMERA_WIDTH)
			this.swipeXTo(this.getX()+MOVEDIST);
			break;
		case 2:
			// down
			if (this.getY()> 0)
			this.swipeYTo(this.getY()-MOVEDIST);
			break;
		case 3:
			// left
			if (this.getX() > 0)
			this.swipeXTo(this.getX()-MOVEDIST);
			break;
		}
	}
	
	private void swipeXTo (float x) {
		registerEntityModifier(new MoveModifier(MOVE_SPEED, this.getX(),x, this.getY(), this.getY(), EaseSineInOut.getInstance()));
	}
	
	private void swipeYTo (float y) {
		registerEntityModifier(new MoveModifier(MOVE_SPEED, this.getX(),this.getX(), this.getY(), y, EaseSineInOut.getInstance()));
	}
	
	public void setDirection (int direction) {
		this.direction = direction;
	}
	
	public int getDirection () {
		return this.direction;
	}
	
}
