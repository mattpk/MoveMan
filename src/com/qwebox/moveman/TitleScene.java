package com.qwebox.moveman;

import java.io.IOException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.games.Games;
import com.qwebox.moveman.SceneManager.SceneType;
import com.qwebox.moveman.google.services.GBaseGameActivity;

public class TitleScene extends Scene {

	public static final float RGB_BG_SHADE = 25f;
	private final SceneManager sceneManager;
	private BaseGameActivity activity;
	private Camera camera;

	//Texture Regions
	private TextureRegion logoTextureRegion, playTextureRegion;
	private TiledTextureRegion muteTextureRegion;

	// Font
	private Font font;
	private Font smallFont;
	private Font exclaFont;
	private Font leaderFont;

	//Objects
	Rectangle play, leaderButton;

	TiledSprite muteButton;

	// Sound
	Sound selectSound;

	public TitleScene(final SceneManager sceneManager) {
		super();
		this.sceneManager = sceneManager;
		this.activity = sceneManager.getActivity();
		this.camera = sceneManager.getCamera();

		loadSceneResources();
		createScene();
	}

	private void loadSceneResources() {
		//Sound
		SoundFactory.setAssetBasePath("sfx/");
		try {
			this.selectSound = SoundFactory.createSoundFromAsset(this.activity.getEngine().getSoundManager(), this.activity, "select.wav");
		} catch (final IOException e) {
			Debug.e(e);
		}

		// Logos
		this.playTextureRegion = SceneManager.processImage("play.png",320,80, activity);

		// Mute
		BitmapTextureAtlas texMute = new BitmapTextureAtlas(this.activity.getTextureManager(), 64, 32, TextureOptions.NEAREST_PREMULTIPLYALPHA);
		muteTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(texMute, this.activity.getAssets(),"mute.ss.png", 0, 0, 2, 1);
		texMute.load();

		// Font
		this.font = loadFont("MunroSmall.ttf",96,android.graphics.Color.WHITE);
		this.smallFont = loadFont("MunroSmall.ttf",24,android.graphics.Color.WHITE);
		this.exclaFont = loadFont("MunroSmall.ttf",128,android.graphics.Color.parseColor("#191919"));
		this.leaderFont = loadFont("MunroSmall.ttf",36,android.graphics.Color.WHITE);
	}

	private Font loadFont(String path, int size, int color) {
		FontFactory.setAssetBasePath("fonts/");
		BitmapTextureAtlas fontTexture = new BitmapTextureAtlas(this.activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		fontTexture.load();
		Font makeFont = FontFactory.createFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), path, size, true, color);
		makeFont.load();
		return makeFont;
	}

	private void createScene() {
		// bg
		this.setBackground(new Background(RGB_BG_SHADE/255f,RGB_BG_SHADE/255f,RGB_BG_SHADE/255f));

		// Draw text logo
		Text moveText = new Text(0,0,this.font,"move\nMAN",20,new TextOptions(HorizontalAlign.CENTER),this.activity.getVertexBufferObjectManager());
		moveText.setPosition(this.camera.getWidth()*0.5f-moveText.getWidth()*0.5f,100);

		//Draw my name
		Text nameText = new Text(0,0,this.smallFont,"a game by Matthew Chung", 25, new TextOptions(HorizontalAlign.RIGHT),this.activity.getVertexBufferObjectManager());
		nameText.setPosition(this.camera.getWidth()-nameText.getWidth()-1,this.camera.getHeight()-nameText.getHeight()-1);

		this.attachChild(moveText);
		this.attachChild(nameText);

		// draw mute button
		muteButton = new TiledSprite(0,0,54,54,muteTextureRegion,this.activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
					((MoveManActivity)activity).muted = (!((MoveManActivity)activity).muted);
					if (((MoveManActivity)activity).muted) {
						muteButton.setCurrentTileIndex(1);
					} else {
						muteButton.setCurrentTileIndex(0);
					}
				}
				return true;
			}
		};

		if (((MoveManActivity)activity).muted) {
			muteButton.setCurrentTileIndex(1);
		} else {
			muteButton.setCurrentTileIndex(0);
		}

		muteButton.setPosition(4,camera.getHeight()-muteButton.getHeight()-4);
		attachChild(muteButton);
		this.registerTouchArea(muteButton);

		//Draw play
		play = new Rectangle(0,0,280,160,this.activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					play.setColor(106/255f,26/255f,26/255f);
					if (!((MoveManActivity)activity).muted) selectSound.play();
					return true;
				}
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP && !(pTouchAreaLocalX <= 0 || pTouchAreaLocalX >= this.getWidth() || pTouchAreaLocalY <= 0 || pTouchAreaLocalY >= this.getHeight()) ) {
					play.setColor(176f/255f,43f/255f,44f/255f);
					playButtonAction();
					return true;
				}
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_MOVE && (pTouchAreaLocalX <= 0 || pTouchAreaLocalX >= this.getWidth() || pTouchAreaLocalY <= 0 || pTouchAreaLocalY >= this.getHeight())) {
					play.setColor(176f/255f,43f/255f,44f/255f);
					return true;
				}
				return false;
			}
		};
		play.setColor(176f/255f,43f/255f,44f/255f);
		play.setPosition(this.camera.getWidth()*0.5f-play.getWidth()*0.5f,this.camera.getHeight()*0.5f);

		this.attachChild(play);
		this.registerTouchArea(play);
		
		//Draw leaderboard button
		leaderButton = new Rectangle (0,0,280,80,this.activity.getVertexBufferObjectManager()) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					leaderButton.setColor(207/255f,166/255f,0/255f);
					if (!((MoveManActivity)activity).muted) selectSound.play();
					return true;
				}
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP && !(pTouchAreaLocalX <= 0 || pTouchAreaLocalX >= this.getWidth() || pTouchAreaLocalY <= 0 || pTouchAreaLocalY >= this.getHeight()) ) {
					leaderButton.setColor(255/255f,204/255f,0/255f);
					leaderButtonAction();
					return true;
				}
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_MOVE && (pTouchAreaLocalX <= 0 || pTouchAreaLocalX >= this.getWidth() || pTouchAreaLocalY <= 0 || pTouchAreaLocalY >= this.getHeight())) {
					leaderButton.setColor(255/255f,204/255f,0/255f);
					return true;
				}
				return false;
			}
		};
		leaderButton.setColor(255/255f,204/255f,0/255f);
		leaderButton.setPosition(this.camera.getWidth()*0.5f-leaderButton.getWidth()*0.5f,play.getY()+play.getHeight() + 30);
		
		this.attachChild(leaderButton);
		this.registerTouchArea(leaderButton);
		
		setTouchAreaBindingOnActionDownEnabled(true);
		setTouchAreaBindingOnActionMoveEnabled(true);

		// Draw !!!s on play
		Text exclaText = new Text(0,0,this.exclaFont,"!!!!!!",10,this.activity.getVertexBufferObjectManager());
		exclaText.setPosition((play.getX()+play.getWidth()*0.5f)-exclaText.getWidth()*0.5f,(play.getY()+play.getHeight()*0.5f)-exclaText.getHeight()*0.5f);
		this.attachChild(exclaText);
		
		// Draw Leaderboards on button
		Text leaderText = new Text(0,0,this.leaderFont,"Leaderboards",20,this.activity.getVertexBufferObjectManager());
		leaderText.setPosition((leaderButton.getX()+leaderButton.getWidth()*0.5f)-leaderText.getWidth()*0.5f,(leaderButton.getY()+leaderButton.getHeight()*0.5f)-leaderText.getHeight()*0.5f);
		this.attachChild(leaderText);
		// Add more later (Options, Quit);
	}

	private void playButtonAction() {
		if (this.activity.getSharedPreferences("Highscores", Context.MODE_PRIVATE).getInt("score", 0) > 0)
			sceneManager.setCurrentScene(SceneType.MAINGAME);
		else {
			sceneManager.setCurrentScene(SceneType.HELP);
		}
	}
	
	private void leaderButtonAction() {
		try 
		{
			//runs on UI thread
			activity.runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
		            //checks if user is signed in
		            //you must call an instance of your game activity here to get
		            //to Game Helper
					if(!((GBaseGameActivity) activity).getGameHelper().isSignedIn()) {
						((GBaseGameActivity) activity).getGameHelper().beginUserInitiatedSignIn();
					}
				}
			});
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		try 
		{
			((GBaseGameActivity) activity).runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					if (((GBaseGameActivity) activity).getGameHelper().isSignedIn()) 
					{
						Games.Leaderboards.submitScore(((GBaseGameActivity) activity).getGameHelper().getApiClient(), activity.getString(R.string.leaderboard_highscore), activity.getSharedPreferences("Highscores", Context.MODE_PRIVATE).getInt("score", 0));
					}
		            else
		            {
		                //save to local
		            }
				}
			});
		} 
		catch (Exception e1) 
		{
			e1.printStackTrace();
		}
		
		try 
		{
			((GBaseGameActivity) activity).runOnUiThread(new Runnable() 
			{
				@Override
				public void run() 
				{
					if(((GBaseGameActivity) activity).getGameHelper().isSignedIn())
					{
						//shows leaderboards
						((GBaseGameActivity) activity).startActivityForResult(Games.Leaderboards.getLeaderboardIntent(((GBaseGameActivity) activity).getGameHelper().getApiClient(), activity.getString(R.string.leaderboard_highscore)), 0);
					}
					else
					{
						((GBaseGameActivity) activity).getGameHelper().makeSimpleDialog(activity.getString(R.string.gamehelper_not_loged_in));
					}
				}
			});
		} 
		catch (Exception e2) 
		{
			e2.printStackTrace();
		}
		
	}
}
