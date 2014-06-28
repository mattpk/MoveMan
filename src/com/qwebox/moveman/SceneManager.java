package com.qwebox.moveman;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.ui.activity.BaseGameActivity;

public class SceneManager {

	private SceneType currentScene;
	private BaseGameActivity activity;
	private Engine engine;
	private Camera camera;

	private TextureRegion splashTextureRegion;
	
	public Scene titleScene, splashScene, mainGameScene, scoreScreenScene, helpScene;

	
	public enum SceneType
	{
		SPLASH, TITLE, MAINGAME, SCORESCREEN, HELP
	}

	//Constructor
	public SceneManager(BaseGameActivity activity, Engine engine, Camera camera) {
		this.activity = activity;
		this.engine = engine;
		this.camera = camera;
	}

	//Method loads all of the splash screen resources
	public void loadSplashSceneResources() {

		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		BitmapTextureAtlas splashTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(),256,256,TextureOptions.DEFAULT);
		this.splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas,activity, "splash.PNG",0,0);
		splashTextureAtlas.load();
	}
	
	//Splash scene with logo in middle
	public Scene createSplashScene() {
		// Create the splash scene and set bgcolor to black and add the splash logo.
		splashScene = new Scene();
		splashScene.setBackground(new Background(0,0,0));
		Sprite splash = new Sprite(0,0,this.splashTextureRegion,activity.getVertexBufferObjectManager()) {
			@Override
			protected void preDraw(GLState pGLState, Camera pCamera)
			{
				super.preDraw(pGLState, pCamera);
				pGLState.enableDither();
			}
		};
		splash.setScale(1.5f);
		splash.setPosition((camera.getWidth()-splash.getWidth())*0.5f,(camera.getHeight()-splash.getHeight())*0.5f);
		//splashScene.attachChild(splash);
		return splashScene;
	}

	
	//Method loads and creates all of the Game Scenes
	public void loadAndCreateGameScenes() {
		titleScene = new TitleScene(this);
		mainGameScene = new MainGameScene(this);
		scoreScreenScene = new ScoreScreenScene(this);
		helpScene = new HelpScene(this);
	}
	
	//Creates and loads BitmapTextureAtlas + returns TextureRegion based on image path and dimensions
	public static TextureRegion processImage(String path, int width, int height, BaseGameActivity activity) {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		BitmapTextureAtlas thisTextureAtlas = new BitmapTextureAtlas(activity.getTextureManager(),width,height,TextureOptions.DEFAULT);
		thisTextureAtlas.load();
		return BitmapTextureAtlasTextureRegionFactory.createFromAsset(thisTextureAtlas,activity, path,0,0);
	}

	//SceneType currentScene Getter
	public SceneType getCurrentScene() {
		return currentScene;
	}

	//SceneType currentScene Setter
	public void setCurrentScene(SceneType scene) {
		this.currentScene = scene;
		switch (scene)
		{
		case SPLASH:
			break;
		case TITLE:
			engine.setScene(titleScene);
			break;
		case MAINGAME:
			engine.setScene(mainGameScene);
			break;
		case SCORESCREEN:
			scoreScreenScene = new ScoreScreenScene(this); // Reset score Screen
			mainGameScene = new MainGameScene(this); // Reset main game
			engine.setScene(scoreScreenScene);
			break;
		case HELP:
			engine.setScene(helpScene);
			break;
		}
	}
	
	// BaseGameActivity getter
	public BaseGameActivity getActivity() {
		return this.activity;
	}
	
	//Camera getter
	public Camera getCamera() {
		return this.camera;
	}
}
