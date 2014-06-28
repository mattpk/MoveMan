package com.qwebox.moveman;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.IGameInterface.OnCreateResourcesCallback;
import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
import org.andengine.ui.IGameInterface.OnPopulateSceneCallback;

import com.qwebox.moveman.SceneManager.SceneType;
import com.qwebox.moveman.google.services.GBaseGameActivity;

public class MoveManActivity extends GBaseGameActivity {

	
	public boolean muted = false;
	public final static int CAMERA_WIDTH = 480;
	public final static int CAMERA_HEIGHT = 800;
	
	private Camera mCamera;
	private SceneManager sceneManager;
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		mCamera = new Camera(0,0,CAMERA_WIDTH,CAMERA_HEIGHT);
		EngineOptions engineOptions = new EngineOptions(true,ScreenOrientation.PORTRAIT_FIXED, new RatioResolutionPolicy(CAMERA_WIDTH,CAMERA_HEIGHT), mCamera);
		engineOptions.getAudioOptions().setNeedsSound(true);
		engineOptions.getAudioOptions().setNeedsMusic(true);
		return engineOptions;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		this.sceneManager = new SceneManager(this,mEngine,mCamera);
		this.sceneManager.loadSplashSceneResources();
		
		// To indicate finished creating resources
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		pOnCreateSceneCallback.onCreateSceneFinished(this.sceneManager.createSplashScene());
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		/*
		mEngine.registerUpdateHandler(new TimerHandler(1f,new ITimerCallback()
		{
			public void onTimePassed(final TimerHandler pTimerHandler)
			{
				mEngine.unregisterUpdateHandler(pTimerHandler); */
				sceneManager.loadAndCreateGameScenes();
				sceneManager.setCurrentScene(SceneType.TITLE);
				
						// Rate
				
                this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    	AppRater.app_launched(sceneManager.getActivity());
                    }
            });	
					 /*
			}
		}));
		*/
			pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	// Back Button Functionality
    @Override
    public void onBackPressed()
    {
        if(sceneManager.getCurrentScene() == SceneType.MAINGAME){
        	mEngine.start();
        	sceneManager.mainGameScene = new MainGameScene(sceneManager); // Reset main game
        	sceneManager.setCurrentScene(SceneType.TITLE);
        }
        else if (sceneManager.getCurrentScene() == SceneType.SCORESCREEN) {
        	sceneManager.mainGameScene = new MainGameScene(sceneManager); // Reset main game
        	sceneManager.setCurrentScene(SceneType.TITLE);
        }
        else if (sceneManager.getCurrentScene() == SceneType.HELP) {
        	sceneManager.setCurrentScene(SceneType.TITLE);
        }
        else{
            this.finish();
        }
    }
}
