package com.qwebox.moveman;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;

public class ScoreScreenTemplate extends Scene {
	
	private SceneManager sceneManager;
	private BaseGameActivity activity;
	private Camera camera;
	
	public ScoreScreenTemplate(final SceneManager sceneManager) {
		super();
		this.sceneManager = sceneManager;
		this.activity = sceneManager.getActivity();
		this.camera = sceneManager.getCamera();
		
		loadSceneResources();
		createScene();
	}
	
	private void loadSceneResources() {
		
	}
	
	private void createScene() {
		
	}

}
