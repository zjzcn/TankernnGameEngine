package eu.tankernn.gameEngine;

import java.io.FileNotFoundException;
import java.io.IOException;

import eu.tankernn.gameEngine.entities.Camera;
import eu.tankernn.gameEngine.loader.Loader;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.renderEngine.MasterRenderer;
import eu.tankernn.gameEngine.renderEngine.skybox.Skybox;
import eu.tankernn.gameEngine.renderEngine.water.WaterMaster;
import eu.tankernn.gameEngine.util.InternalFile;

public class TankernnGame {
	protected Loader loader;
	protected MasterRenderer renderer;
	protected WaterMaster waterMaster;
	protected Camera camera;
	protected Skybox sky;
	
	public TankernnGame(Skybox skybox, String dudvMap, String normalMap) {
		this.sky = skybox;
		try {
			loader = new Loader(new InternalFile("models.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		camera = new Camera();
		renderer = new MasterRenderer(loader, camera, skybox);
		try {
			waterMaster = new WaterMaster(loader, loader.loadTexture(dudvMap), loader.loadTexture(normalMap), camera);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void update() {
		camera.update();
	}
	
	public void render() {
		DisplayManager.updateDisplay();
	}
	
	public void cleanUp() {
		waterMaster.cleanUp();
		loader.cleanUp();
		renderer.cleanUp();
	}
}
