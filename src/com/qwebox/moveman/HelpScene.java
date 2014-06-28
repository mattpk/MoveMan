package com.qwebox.moveman;

import org.andengine.engine.camera.Camera;
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

import com.qwebox.moveman.SceneManager.SceneType;

public class HelpScene extends Scene {
	
	private SceneManager sceneManager;
	private BaseGameActivity activity;
	private Camera camera;
	
	private Font font, exclaFont;
	private Text helpText;
	
	private Rectangle play;
	
	public HelpScene(final SceneManager sceneManager) {
		super();
		this.sceneManager = sceneManager;
		this.activity = sceneManager.getActivity();
		this.camera = sceneManager.getCamera();
		
		loadSceneResources();
		createScene();
	}
	
	private void loadSceneResources() {
		this.font = loadFont("MunroSmall.ttf",64,android.graphics.Color.WHITE);
		this.exclaFont = loadFont("MunroSmall.ttf",128,android.graphics.Color.WHITE);
	}
	
	private void createScene() {
		this.setBackground(new Background(TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f));
		helpText = new Text(0, 0, font, "Swipe to move.\n\nTap to shoot\n forward.\n\nDon't get hit\nthree times.", 100,new TextOptions(HorizontalAlign.CENTER),this.activity.getVertexBufferObjectManager());
		helpText.setPosition(this.camera.getWidth()*0.5f-helpText.getWidth()*0.5f,75);
		attachChild(helpText);
		drawButton();
	}

	private Font loadFont(String path, int size, int color) {
		FontFactory.setAssetBasePath("fonts/");
		BitmapTextureAtlas fontTexture = new BitmapTextureAtlas(this.activity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		fontTexture.load();
		Font makeFont = FontFactory.createFromAsset(this.activity.getFontManager(), fontTexture, this.activity.getAssets(), path, size, true, color);
		makeFont.load();
		return makeFont;
	}
	
	private void drawButton() {
		//Draw play
		play = new Rectangle(0,0,280,160,this.activity.getVertexBufferObjectManager()) {
			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
					play.setColor(106/255f,26/255f,26/255f);
					if (!((MoveManActivity)activity).muted) ((TitleScene)(sceneManager.titleScene)).selectSound.play();
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
		play.setPosition(this.camera.getWidth()*0.5f-play.getWidth()*0.5f,this.camera.getHeight()*0.7f);
		
		this.attachChild(play);
		this.registerTouchArea(play);
		
		setTouchAreaBindingOnActionDownEnabled(true);
		setTouchAreaBindingOnActionMoveEnabled(true);
		
		// Draw !!!s on play
		Text exclaText = new Text(0,0,this.exclaFont,"OK",10,this.activity.getVertexBufferObjectManager());
		exclaText.setColor(TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f,TitleScene.RGB_BG_SHADE/255f);
		exclaText.setPosition((play.getX()+play.getWidth()*0.5f)-exclaText.getWidth()*0.5f,(play.getY()+play.getHeight()*0.5f)-exclaText.getHeight()*0.5f);
		this.attachChild(exclaText);
	}
	
}
