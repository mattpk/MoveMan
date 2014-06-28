package com.qwebox.moveman;

import java.io.IOException;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.debug.Debug;

import com.google.android.gms.games.Games;
import com.qwebox.moveman.SceneManager.SceneType;
import com.qwebox.moveman.google.services.GBaseGameActivity;

import android.content.Context;
import android.content.SharedPreferences;

public class ScoreScreenScene extends Scene {
	
	private SceneManager sceneManager;
	private BaseGameActivity activity;
	private Camera camera;
	
	//
	private int score = 0;
	private int highscore = 0;
	
	// Font & Texts
	private Font font, bestFont;
	private Font numFont,exclaFont, smallFont;
	private Font leaderFont;
	private Text gameOverText;
	private Text scoreText, highscoreText;
	
	//Sound
	Sound blipSound;
	
	//Cutscene Things
	private boolean cutScene;
	private TimerHandler pauseTimer;
	private int cutSceneEventIndex;
	
	private TimerHandler scoreIncreaseTimer;
	private int scoreIncreaseIndex;
	
	private Rectangle play, leaderButton;

	
	// Constructor
	public ScoreScreenScene(final SceneManager sceneManager) {
		super();
		this.sceneManager = sceneManager;
		this.activity = sceneManager.getActivity();
		this.camera = sceneManager.getCamera();
		
		cutScene = true;
		cutSceneEventIndex = 0;
		
		loadSceneResources();
		createScene();
	}
	
	private void loadSceneResources() {
		//Score scrape
		score = ((MainGameScene)sceneManager.mainGameScene).score;
		
		// high score
		mScoreDb = this.activity.getSharedPreferences(HIGHSCORE_DB_NAME, Context.MODE_PRIVATE);
		mScoreDbEditor = this.mScoreDb.edit();
		this.highscore = Math.max(loadHighScore(),score);
		saveHighScore();
		
		
		// Font
		this.font = loadFont("MunroSmall.ttf",96,android.graphics.Color.WHITE,256);
		this.exclaFont = loadFont("MunroSmall.ttf",128,android.graphics.Color.WHITE,256);
		this.numFont = loadFont("MunroSmall.ttf",192,android.graphics.Color.WHITE,512);
		this.bestFont = loadFont("MunroSmall.ttf",64,android.graphics.Color.WHITE,256);
		this.smallFont = loadFont("MunroSmall.ttf",24,android.graphics.Color.WHITE,256);
		this.leaderFont =loadFont("MunroSmall.ttf",36,android.graphics.Color.WHITE,256);
		
		//Sound
		SoundFactory.setAssetBasePath("sfx/");
		try {
			this.blipSound = SoundFactory.createSoundFromAsset(this.activity.getEngine().getSoundManager(), this.activity, "select.wav");
		} catch (final IOException e) {
			Debug.e(e);
		}
	}
	
	private void createScene() {
		this.setBackground(new Background(TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f));
		
		cutSceneSequence();
	}
	

	private void cutSceneSequence() {
		gameOverText = new Text(0, 0, font, "Game Over", "Game Over".length(),new TextOptions(HorizontalAlign.CENTER),this.activity.getVertexBufferObjectManager());
		gameOverText.setPosition(this.camera.getWidth()*0.5f-gameOverText.getWidth()*0.5f,100-50);
		
		scoreText = new Text(0, 0, numFont, "0", 2000,activity.getVertexBufferObjectManager());
		highscoreText = new Text(0,0,bestFont,"Best:\n"+highscore, 100,new TextOptions(HorizontalAlign.RIGHT),activity.getVertexBufferObjectManager());
		
		this.registerUpdateHandler(pauseTimer = new TimerHandler(0.5f, new ITimerCallback()
		{                      
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				if (cutSceneEventIndex < 3)
					pauseTimer.reset();
				switch (cutSceneEventIndex) {
				case 0:
					attachChild(gameOverText);
					break;
				case 1:
					// Get score
					scoreText.setPosition((camera.getWidth()*0.5f-scoreText.getWidth()*0.5f)-(64),300-50);
					attachChild(scoreText);
					scoreIncreaseSequence();
					break;
				case 2:
					//Best:
					highscoreText.setPosition(Math.min((scoreText.getX()+scoreText.getWidth()+64),(camera.getWidth()-highscoreText.getWidth())),((scoreText.getY()+scoreText.getHeight())-highscoreText.getHeight()-32)-50);
					attachChild(highscoreText);
					break;
				case 3:
					// Show button
					drawButton();
					break;
				}
				if (!((MoveManActivity)activity).muted) blipSound.play();
				cutSceneEventIndex ++;
			} // end override
		}));
	}
	
	private void drawButton() {
		//Draw play
		play = new Rectangle(0,0,280,160,this.activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					play.setColor(106/255f,26/255f,26/255f);
					if (!((MoveManActivity)activity).muted) blipSound.play();
					return true;
				}
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP && !(pTouchAreaLocalX <= 0 || pTouchAreaLocalX >= this.getWidth() || pTouchAreaLocalY <= 0 || pTouchAreaLocalY >= this.getHeight()) ) {
					play.setColor(176f/255f,43f/255f,44f/255f);
					sceneManager.setCurrentScene(SceneType.MAINGAME);
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
		play.setPosition(this.camera.getWidth()*0.5f-play.getWidth()*0.5f,(this.camera.getHeight()*0.7f)-100);
		
		this.attachChild(play);
		this.registerTouchArea(play);
		
		setTouchAreaBindingOnActionDownEnabled(true);
		setTouchAreaBindingOnActionMoveEnabled(true);
		
		// Draw !!!s on play
		Text exclaText = new Text(0,0,this.exclaFont,"!!!!!!",10,this.activity.getVertexBufferObjectManager());
		exclaText.setColor(TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f);
		exclaText.setPosition((play.getX()+play.getWidth()*0.5f)-exclaText.getWidth()*0.5f,(play.getY()+play.getHeight()*0.5f)-exclaText.getHeight()*0.5f);
		this.attachChild(exclaText);
		
		//Draw leaderboard button
		leaderButton = new Rectangle (0,0,280,80,this.activity.getVertexBufferObjectManager()) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					leaderButton.setColor(207/255f,166/255f,0/255f);
					if (!((MoveManActivity)activity).muted) blipSound.play();
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
		
		// Draw Leaderboards on button
		Text leaderText = new Text(0,0,this.leaderFont,"Leaderboards",20,this.activity.getVertexBufferObjectManager());
		leaderText.setPosition((leaderButton.getX()+leaderButton.getWidth()*0.5f)-leaderText.getWidth()*0.5f,(leaderButton.getY()+leaderButton.getHeight()*0.5f)-leaderText.getHeight()*0.5f);
		this.attachChild(leaderText);
		
	}
	
	private void scoreIncreaseSequence() {
		scoreIncreaseIndex = 0;
		this.registerUpdateHandler(scoreIncreaseTimer = new TimerHandler(0.05f, new ITimerCallback()
		{                      
			@Override
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				scoreIncreaseIndex++;
				scoreText.setText(((int)(((scoreIncreaseIndex)*1f)*(score*0.1f)))+"");
				scoreText.setPosition((camera.getWidth()*0.5f-scoreText.getWidth()*0.5f)-(64),300-50);
				
				if (scoreIncreaseIndex < 10)
					scoreIncreaseTimer.reset();
			} // end override
		}));
	}

	//Highscore stuff
	private static final String HIGHSCORE_DB_NAME = "Highscores";
	private static final String HIGHSCORE_LABEL = "score";
	 
	private SharedPreferences mScoreDb;
	private SharedPreferences.Editor mScoreDbEditor;
	 
	 
	public boolean saveHighScore() {
	        this.mScoreDbEditor.putInt(HIGHSCORE_LABEL, this.highscore);
	        return this.mScoreDbEditor.commit();
	}
	 
	public int loadHighScore() {
	        return this.mScoreDb.getInt(HIGHSCORE_LABEL, 0);
	}
	// Done highscore stuff
	
	private Font loadFont(String path, int size, int color, int textSize) {
		FontFactory.setAssetBasePath("fonts/");
		BitmapTextureAtlas fontTexture = new BitmapTextureAtlas(this.activity.getTextureManager(), textSize, textSize, TextureOptions.BILINEAR);
		fontTexture.load();
		Font makeFont = FontFactory.createFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), path, size, true, color);
		makeFont.load();
		return makeFont;
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
