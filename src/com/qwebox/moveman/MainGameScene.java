package com.qwebox.moveman;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.AnimatedSprite.IAnimationListener;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.source.AssetBitmapTextureAtlasSource;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;
import org.andengine.util.modifier.IModifier;

import android.graphics.Color;

import com.qwebox.moveman.SceneManager.SceneType;


public class MainGameScene extends Scene{

	private final int MIN_SWIPE_DIST = 25;
	private float BULLET_SPEED = 750f;
	private float BULLET_SIZE = 16f;
	private final float ENEMY_SIZE = 64f;
	private final SceneManager sceneManager;
	private BaseGameActivity activity;
	private Camera camera;

	//Texture Regions
	private TextureRegion manUpTextureRegion, manDownTextureRegion, manLeftTextureRegion, manRightTextureRegion, smallBulletTextureRegion, thomasTextureRegion;
	private TiledTextureRegion deathAnimationTextureRegion, pauseTextureRegion;
	// Texture Atlas (Used for bg at this point)
	BitmapTextureAtlas mBitmapTextureAtlas;

	RepeatingSpriteBackground mGrassBackground, mMetalBackground;

	//Objects / Object pools
	private Man player;
	private ArrayList<Bullet> usedBullets = new ArrayList<Bullet>();
	private ArrayList<Bullet> freeBullets = new ArrayList<Bullet>();
	private ArrayList<Enemy> usedEnemies = new ArrayList<Enemy>();
	private ArrayList<Enemy> freeEnemies = new ArrayList<Enemy>();
	private ArrayList<AnimatedSprite> usedEnemyDeathExplosions = new ArrayList<AnimatedSprite>();
	private ArrayList<AnimatedSprite> freeEnemyDeathExplosions = new ArrayList<AnimatedSprite>();

	private TiledSprite pauseButton;
	private Text pauseText;

	//Timers
	private int secondsElapsed = 0;
	private final int START_MOB_COUNT = 2;
	private final float LEVEL_FACTOR = 0.15f;

	private Font font, font_black;
	private Text scoreText;
	public int score = 0;
	private int health = 3;

	private Rectangle flashRect = null;
	private boolean isFlashing = false;

	private boolean startedPauseTouch = false;

	//Sounds
	Sound pewSound, killSound, hurtSound;

	//Touch/Swipe tools
	private float[] lastTouchEvent;
	private boolean isReset = false;
	private boolean canSwipe = true;
	private boolean swipedYet = false;

	//Game Over Transition
	private boolean frozen = false;
	private boolean ununfreezable = false;

	// Layers
	private final int LAYER_TOP = 2;
	private final int LAYER_ENEMIES = 1;
	private final int LAYER_PLAYER = 0;

	// Constructor
	public MainGameScene(final SceneManager sceneManager) {
		super();
		this.sceneManager = sceneManager;
		this.activity = sceneManager.getActivity();
		this.camera = sceneManager.getCamera();

		loadSceneResources();
		createScene();
	}

	private void loadSceneResources() {

		//Sounds	
		SoundFactory.setAssetBasePath("sfx/");
		try {
			this.pewSound = SoundFactory.createSoundFromAsset(this.activity.getEngine().getSoundManager(), this.activity, "pew.wav");
			this.hurtSound = SoundFactory.createSoundFromAsset(this.activity.getEngine().getSoundManager(), this.activity, "hurt.wav");
			this.killSound = SoundFactory.createSoundFromAsset(this.activity.getEngine().getSoundManager(), this.activity, "kill.wav");
		} catch (final IOException e) {
			Debug.e(e);
		}

		// Font
		FontFactory.setAssetBasePath("fonts/");
		BitmapTextureAtlas fontTexture = new BitmapTextureAtlas(this.activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.font = FontFactory.createFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), "MunroSmall.ttf", 64, true, android.graphics.Color.WHITE);
		//this.font = FontFactory.createStrokeFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), "MunroSmall.ttf", 66, true, android.graphics.Color.WHITE, 2, android.graphics.Color.BLACK);
		fontTexture.load();
		this.font.load();
		
		new android.graphics.Color();
		this.font_black = FontFactory.createFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), "MunroSmall.ttf", 64, true, Color.rgb((int)TitleScene.RGB_BG_SHADE,(int)TitleScene.RGB_BG_SHADE,(int)TitleScene.RGB_BG_SHADE));
		//this.font = FontFactory.createStrokeFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), "MunroSmall.ttf", 66, true, android.graphics.Color.WHITE, 2, android.graphics.Color.BLACK);
		this.font_black.load();


		// Texture Regions load
		manUpTextureRegion = SceneManager.processImage("player_up.png",16,16,activity);
		manDownTextureRegion = SceneManager.processImage("player_down.png",16,16,activity);
		manLeftTextureRegion = SceneManager.processImage("player_left.png",16,16,activity);
		manRightTextureRegion = SceneManager.processImage("player_right.png",16,16,activity);
		smallBulletTextureRegion = SceneManager.processImage("smallbullet.png",4,4,activity);
		thomasTextureRegion = SceneManager.processImage("thomas.PNG",32,32,activity);

		//Death animation load
		BitmapTextureAtlas texDeath = new BitmapTextureAtlas(this.activity.getTextureManager(), 256, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		deathAnimationTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texDeath, this.activity.getAssets(),"death.ss.png", 0, 0, 5, 1);
		texDeath.load();

		// Load pause
		BitmapTextureAtlas texPause = new BitmapTextureAtlas(this.activity.getTextureManager(), 32, 16, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		pauseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texPause, this.activity.getAssets(),"pause.ss.png", 0, 0, 2, 1);
		texPause.load();

		// background
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.activity.getTextureManager(), 32, 32);
		this.mGrassBackground = new RepeatingSpriteBackground(camera.getWidth(), camera.getHeight(), this.activity.getTextureManager(), AssetBitmapTextureAtlasSource.create(this.activity.getAssets(), "gfx/grass.gif"), this.activity.getVertexBufferObjectManager());
		this.mMetalBackground = new RepeatingSpriteBackground(camera.getWidth(), camera.getHeight(), this.activity.getTextureManager(), AssetBitmapTextureAtlasSource.create(this.activity.getAssets(), "gfx/metal.png"), this.activity.getVertexBufferObjectManager());
		
		this.mBitmapTextureAtlas.load();
	}

	private void createScene() {
		// Set up layers
		for (int x =0; x< 3; x++) {
			this.attachChild(new Entity());
		}

		// bg
		int randPick = randInt(0,1);
		switch (randPick) {
		case 0:
			this.setBackground(mGrassBackground);
			break;
		case 1:
			this.setBackground(mMetalBackground);
			this.font = this.font_black;
			break;	
		}
		
		// Draw man
		player = new Man(0,0,manDownTextureRegion.getWidth()*4,manDownTextureRegion.getHeight()*4,manDownTextureRegion,activity.getVertexBufferObjectManager());
		player.setPosition(camera.getWidth()*0.5f-player.getWidth()*0.5f,camera.getHeight()*0.5f-player.getHeight()*0.5f);
		this.getChildByIndex(LAYER_PLAYER).attachChild(player);

		//Set up screenTouching
		setOnSceneTouchListener(new IOnSceneTouchListener() {

			@Override
			public boolean onSceneTouchEvent(Scene pScene,TouchEvent pSceneTouchEvent) {
				if (!frozen) {
					if (pSceneTouchEvent.isActionDown() && !isReset)
					{
						lastTouchEvent = new float[]{pSceneTouchEvent.getX(),pSceneTouchEvent.getY()};
						isReset = true;
						swipedYet = false;
					} else if (pSceneTouchEvent.isActionMove() && Math.max(Math.abs(pSceneTouchEvent.getX()-lastTouchEvent[0]),Math.abs(pSceneTouchEvent.getY()-lastTouchEvent[1])) >= MIN_SWIPE_DIST && !swipedYet) {
						swipe(lastTouchEvent,new float[]{pSceneTouchEvent.getX(),pSceneTouchEvent.getY()});
						swipedYet = true;
					}
					if (pSceneTouchEvent.isActionUp()) {
						if (!swipedYet)
							shoot();
						isReset = false;
					}
					return true;
				}
				return false;
			}
		});
		setTouchAreaBindingOnActionDownEnabled(true);

		//Begin enemy spawning
		beginEnemySpawn();

		// Draw score
		this.scoreText = new Text(10, 2, this.font, "Score: 0",1000, this.activity.getVertexBufferObjectManager());
		this.getChildByIndex(LAYER_TOP).attachChild(scoreText);

		// draw pause button and pause text
		pauseText = new Text(0,0,this.font,"PAUSED", 7, this.activity.getVertexBufferObjectManager());
		pauseText.setPosition(camera.getWidth()*0.5f-pauseText.getWidth()*0.5f,camera.getHeight()*0.5f-pauseText.getHeight()*0.5f);
		pauseText.setVisible(false);
		this.getChildByIndex(LAYER_TOP).attachChild(pauseText);
		pauseText.setZIndex(1);
		pauseText.setParent(this);

		pauseButton = new TiledSprite(0,0,54,54,pauseTextureRegion,this.activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					if (startedPauseTouch) {
						if (!((MoveManActivity)activity).muted)
							((TitleScene)(sceneManager.titleScene)).selectSound.play();
						if (frozen && !ununfreezable) {
							unfreeze();
							pauseText.setVisible(false);
							pauseButton.setCurrentTileIndex(0);
						} else {
							freeze();
							pauseText.setVisible(true);
							pauseButton.setCurrentTileIndex(1);
						}
						startedPauseTouch = false;
						return true;
					}
						startedPauseTouch = false;
				}
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					startedPauseTouch = true;
				}
				return true;
			}
		};

		if (frozen) {
			pauseButton.setCurrentTileIndex(1);
		} else {
			pauseButton.setCurrentTileIndex(0);
		}
		pauseButton.setPosition(4,camera.getHeight()-pauseButton.getHeight()-4);
		this.getChildByIndex(LAYER_TOP).attachChild(pauseButton);
		this.registerTouchArea(pauseButton);

	}

	public int getSecondsElapsed() {
		return this.secondsElapsed;
	}

	public void setSecondsElapsed(int secondsElapsed) {
		this.secondsElapsed = secondsElapsed;
	}

	private void createRandomMobs(int n) {
		for (int x =0; x< n && !frozen ;x++) {
			float pos = randInt(0,(int)((camera.getWidth()+ENEMY_SIZE)*2+(camera.getHeight()+ENEMY_SIZE)*2));
			float pX = 0-ENEMY_SIZE;
			float pY = 0-ENEMY_SIZE;
			if (pos <= camera.getWidth()+ENEMY_SIZE) {
				pX = pos-ENEMY_SIZE;
			} else if (pos <= (camera.getWidth()+ENEMY_SIZE)*2) {
				pX = (pos-(camera.getWidth()+ENEMY_SIZE))-ENEMY_SIZE;
				pY = camera.getHeight();
			} else if (pos <= (camera.getWidth()+ENEMY_SIZE)*2 + camera.getHeight()+ENEMY_SIZE) {
				pY = (pos - (camera.getWidth()+ENEMY_SIZE)*2)-ENEMY_SIZE;
			} else {
				pY = (pos - ((camera.getWidth()+ENEMY_SIZE)*2+camera.getHeight()+ENEMY_SIZE));
				pX = camera.getWidth();
			}
			spawnMob(pX,pY);
		}
	}

	private void spawnMob(float pX, float pY) {
		if (freeEnemies.size() > 0) {
			Enemy getEnemy = freeEnemies.remove(0);
			usedEnemies.add(getEnemy);
			getEnemy.setVisible(true);
			getEnemy.setIgnoreUpdate(false);
			getEnemy.setX(pX);
			getEnemy.setY(pY);
		}
		else {
			Enemy enemy = new Enemy(pX,pY,ENEMY_SIZE,ENEMY_SIZE,thomasTextureRegion,activity.getVertexBufferObjectManager(),player);
			usedEnemies.add(enemy);
			this.getChildByIndex(LAYER_ENEMIES).attachChild(enemy);
			enemy.setZIndex(3);
			enemy.setParent(this);
		}
		this.sortChildren();
	}

	private void beginEnemySpawn(){
		TimerHandler enemyTimerHandler;

		this.registerUpdateHandler(enemyTimerHandler = new TimerHandler(1f, new ITimerCallback()
		{                      
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				float level = getSecondsElapsed()/5f;
				//Log.d("",secondsElapsed+"");
				if (getSecondsElapsed()%5f == 0 && !frozen) {
					int mobCount = (int)(START_MOB_COUNT+LEVEL_FACTOR*level);
					createRandomMobs(mobCount);
				}
				if (!frozen)
					setSecondsElapsed(getSecondsElapsed()+1);
				pTimerHandler.reset();
			}
		}));
	}

	public static int randInt(int min, int max) {
		// Usually this can be a field rather than a method variable
		Random rand = new Random();

		// nextInt is normally exclusive of the top value,
		// so add 1 to make it inclusive
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	private int directionByTouch(float deltaX, float deltaY) {

		if (Math.abs(deltaX) > Math.abs(deltaY)) { // If horiz swipe
			if (deltaX >= 0) // right
				return 1;
			else			 // left
				return 3;
		} else { // If vertical swipe
			if (deltaY >= 0) // up
				return 0;
			else
				return 2;
		}
	}

	// 0 down 1 right 2 up 3 left
	public void moveSwipe(int direction) {
		switch (direction) {
		case 2:
			player.setTextureRegion(manUpTextureRegion);
			break;
		case 1:
			player.setTextureRegion(manRightTextureRegion);
			break;
		case 0:
			player.setTextureRegion(manDownTextureRegion);
			break;
		case 3:
			player.setTextureRegion(manLeftTextureRegion);
			break;
		}
		player.moveSwipe(direction);
	}

	private void swipe (float[] start, float[] end) {
		if (canSwipe) {

			float deltaX = end[0]-start[0];
			float deltaY = end[1]-start[1];

			if (Math.max(Math.abs(deltaX),Math.abs(deltaY)) >= MIN_SWIPE_DIST) {
				moveSwipe(directionByTouch(deltaX,deltaY));
			}

			canSwipe = false;
			TimerHandler touchTimerHandler;
			this.registerUpdateHandler(touchTimerHandler = new TimerHandler(player.MOVE_SPEED, new ITimerCallback()
			{                      
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					canSwipe = true;
				}
			}));
		}
	}

	private void shoot() {
		//Sound
		if (!((MoveManActivity)activity).muted)
			pewSound.play();

		float placeX = player.getX()+player.getWidth()*0.5f;
		float placeY = player.getY()+player.getHeight()*0.5f;
		float speedX = 0;
		float speedY = 0;
		switch (player.getDirection()) {
		case 0:
			placeY += player.getHeight()*0.5f;
			speedY = BULLET_SPEED;
			break;
		case 1:
			placeX += player.getWidth()*0.5f;
			speedX = BULLET_SPEED;
			break;
		case 2:
			placeY -= player.getHeight()*0.5f;
			speedY = -1f*BULLET_SPEED;
			break;
		case 3:
			placeX -= player.getWidth()*0.5f;
			speedX = -1f*BULLET_SPEED;
			break;
		}

		placeX -= BULLET_SIZE*0.5f;
		placeY -= BULLET_SIZE*0.5f;
		// bullet recylcing
		for  (int x = usedBullets.size()-1; x>= 0; x--)
		{
			Bullet b = usedBullets.get(x);
			if (b.getX()< b.getWidth()-64 || b.getY()< b.getHeight()-64 || b.getX() > MoveManActivity.CAMERA_WIDTH+64 || b.getY()> MoveManActivity.CAMERA_HEIGHT+64) {
				b.setVisible(false);
				b.setIgnoreUpdate(true);
				usedBullets.remove(x);
				freeBullets.add(b);
			}
		}

		if (freeBullets.size() > 0) {
			Bullet getBullet = freeBullets.remove(0);
			usedBullets.add(getBullet);
			getBullet.setVisible(true);
			getBullet.setIgnoreUpdate(false);
			getBullet.resetBullet(placeX,placeY,BULLET_SIZE,BULLET_SIZE,speedX,speedY);

		}
		else {
			Bullet bullet = new Bullet(placeX,placeY,BULLET_SIZE,BULLET_SIZE, smallBulletTextureRegion, activity.getVertexBufferObjectManager(), speedX, speedY);
			usedBullets.add(bullet);
			this.getChildByIndex(LAYER_ENEMIES).attachChild(bullet);
		}
	}

	private boolean isCollide(Sprite start, Sprite end) {
		if (start.getX()+start.getWidth() < end.getX()) return false;
		if (start.getX() > end.getX() + end.getWidth()) return false;
		if (start.getY()+start.getHeight() < end.getY()) return false;
		if (start.getY() > end.getY() + end.getHeight()) return false;
		return true;
	}

	private void hitFlash() {
		if (flashRect == null) {
			flashRect = new Rectangle(0,0,this.camera.getWidth(),this.camera.getHeight(), this.activity.getVertexBufferObjectManager());
			flashRect.setColor(153f/255f,0,0,0);
			this.getChildByIndex(LAYER_TOP).attachChild(flashRect);
			flashRect.setZIndex(1);
			flashRect.setParent(this);
		}

		if (!isFlashing) {
			isFlashing = true;
			flashRect.setVisible(true);
			flashRect.registerEntityModifier(new AlphaModifier(0.1f,0,0.6f));
			TimerHandler flashTimer;
			if (!((MoveManActivity)activity).muted) hurtSound.play();
			// 0- 0.1 turn red
			// 0.1-0.4 stay red
			// 0.4-0.9 turn clear
			// 0.9-1.6 untouchable
			this.registerUpdateHandler(flashTimer = new TimerHandler(0.4f, new ITimerCallback()
			{                      
				@Override
				public void onTimePassed(final TimerHandler pTimerHandler)
				{
					health--;
					if (health <= 0 ) {
						gameOver();
						flashRect.registerEntityModifier(new AlphaModifier(1f,0.6f,0.9f));
					} else {
						flashRect.registerEntityModifier(new AlphaModifier(0.5f,0.6f,0));

						registerUpdateHandler(new TimerHandler(0.7f, new ITimerCallback() {
							@Override
							public void onTimePassed(final TimerHandler pTimerHandler) {
								isFlashing = false;
								flashRect.setVisible(false);
							}
						}));
					} // end else
				} // end override
			}));
		}
	}


	private void freeze() {
		frozen = true;
		for (Enemy e : usedEnemies) {
			e.setIgnoreUpdate(true);
		}
		for (Bullet b : usedBullets){
			b.setIgnoreUpdate(true);
		}
	}
	private void unfreeze() {
		if (!ununfreezable) {
			frozen = false;
			for (Enemy e : usedEnemies) {
				e.setIgnoreUpdate(false);
			}
			for (Bullet b : usedBullets){
				b.setIgnoreUpdate(false);
			}
		}
	}
	private void gameOver() {
		/*
		scoreText.setText("GG\nScore: " + score);
		this.sortChildren();
		activity.getEngine().stop();
		 */
		activity.getEngine().stop(); // ensure nothing dumb happens
		freeze();
		ununfreezable = true;
		// Set up fade to black
		Rectangle fadeToBlackRect = new Rectangle(0,0,camera.getWidth(),camera.getHeight(),this.activity.getVertexBufferObjectManager());
		fadeToBlackRect.setColor(TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f,0);
		this.getChildByIndex(LAYER_TOP).attachChild(fadeToBlackRect);

		IEntityModifierListener fadeListener = new IEntityModifierListener() {
			@Override
			public void onModifierStarted(IModifier<IEntity> pModifier, IEntity pItem) {
				// Something you wanna do when modifier is still at work
			}
			@Override
			public void onModifierFinished(IModifier<IEntity> pModifier, IEntity pItem) {
				// Things to be done after the modifer has finished
				sceneManager.setCurrentScene(SceneType.SCORESCREEN);
			}
		};
		fadeToBlackRect.registerEntityModifier(new AlphaModifier(1.15f,0,1,fadeListener));
		activity.getEngine().start(); // resume


	}

	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {

		if (!frozen){
			// Check collisions between bullets and enemies
			ArrayList <Bullet> remBul = new ArrayList <Bullet> ();
			ArrayList <Enemy> remEne = new ArrayList <Enemy> ();

			for (int y = usedEnemies.size()-1; y>=0; y--) {
				Enemy e = usedEnemies.get(y);
				for  (int x = usedBullets.size()-1; x>= 0; x--) {
					Bullet b = usedBullets.get(x);
					if (isCollide((Sprite)b,(Sprite)e) && !remBul.contains(b) && !remEne.contains(e)) {
						remBul.add(b);
						remEne.add(e);
					}
				}
				// Player collision with enemy
				if (isCollide((Sprite)player,(Sprite)e)) {
					hitFlash();
				}
			}
			if (remEne.size() > 0) {
				if (!((MoveManActivity)activity).muted) killSound.play();
				score+= remEne.size();
				for (int x = remBul.size()-1; x>= 0; x--) {
					Bullet b = remBul.get(x);
					Enemy e = remEne.get(x);

					//Bullet
					b.setVisible(false);
					b.setIgnoreUpdate(true);
					usedBullets.remove(b);
					freeBullets.add(b);
					//Enemy
					e.setVisible(false);
					e.setIgnoreUpdate(true);
					usedEnemies.remove(e);
					freeEnemies.add(e);

					// Set up death animation

					AnimatedSprite explosion;
					if (freeEnemyDeathExplosions.size() > 0) {
						explosion = freeEnemyDeathExplosions.remove(0);
						explosion.setVisible(true);
						explosion.setIgnoreUpdate(false);
						explosion.setPosition(e.getX(),e.getY());
					} else {
						explosion = new AnimatedSprite(e.getX(), e.getY(),64,64,deathAnimationTextureRegion, this.activity.getVertexBufferObjectManager());
						this.getChildByIndex(LAYER_ENEMIES).attachChild(explosion);
					}
					usedEnemyDeathExplosions.add(explosion);
					explosion.animate(50, false, new IAnimationListener() {
						@Override
						public void onAnimationFinished(AnimatedSprite arg0) {
							arg0.setVisible(false);
							arg0.setIgnoreUpdate(true);
							usedEnemyDeathExplosions.remove(arg0);
							freeEnemyDeathExplosions.add(arg0);
						}

						@Override
						public void onAnimationFrameChanged(AnimatedSprite arg0, int arg1,
								int arg2) {

						}

						@Override
						public void onAnimationLoopFinished(AnimatedSprite arg0, int arg1,
								int arg2) {

						}

						@Override
						public void onAnimationStarted(AnimatedSprite arg0, int arg1) {
							// TODO Auto-generated method stub

						}
					});
				}
				// Change text
				scoreText.setText("Score: " + this.score);
				this.sortChildren();
			}
		}
		super.onManagedUpdate(pSecondsElapsed);

		// Check collisions between
	}

}
