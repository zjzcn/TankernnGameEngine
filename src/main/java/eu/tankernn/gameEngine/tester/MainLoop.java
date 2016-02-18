package eu.tankernn.gameEngine.tester;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import eu.tankernn.gameEngine.entities.Camera;
import eu.tankernn.gameEngine.entities.Entity;
import eu.tankernn.gameEngine.entities.Light;
import eu.tankernn.gameEngine.entities.Player;
import eu.tankernn.gameEngine.font.meshCreator.FontType;
import eu.tankernn.gameEngine.font.meshCreator.GUIText;
import eu.tankernn.gameEngine.font.rendering.TextMaster;
import eu.tankernn.gameEngine.gui.GuiRenderer;
import eu.tankernn.gameEngine.gui.GuiTexture;
import eu.tankernn.gameEngine.models.RawModel;
import eu.tankernn.gameEngine.models.TexturedModel;
import eu.tankernn.gameEngine.normalMapping.objConverter.NormalMappedObjLoader;
import eu.tankernn.gameEngine.objLoader.ModelData;
import eu.tankernn.gameEngine.objLoader.OBJFileLoader;
import eu.tankernn.gameEngine.particles.ParticleMaster;
import eu.tankernn.gameEngine.particles.ParticleSystem;
import eu.tankernn.gameEngine.particles.ParticleTexture;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.renderEngine.Loader;
import eu.tankernn.gameEngine.renderEngine.MasterRenderer;
import eu.tankernn.gameEngine.terrains.Terrain;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.textures.ModelTexture;
import eu.tankernn.gameEngine.textures.TerrainTexture;
import eu.tankernn.gameEngine.textures.TerrainTexturePack;
import eu.tankernn.gameEngine.util.MousePicker;
import eu.tankernn.gameEngine.water.WaterFrameBuffers;
import eu.tankernn.gameEngine.water.WaterRenderer;
import eu.tankernn.gameEngine.water.WaterShader;
import eu.tankernn.gameEngine.water.WaterTile;

public class MainLoop {
	
	private static final int SEED = 1235;
	
	public static void main(String[] args) {
		List<Entity> entities = new ArrayList<Entity>();
		List<Entity> normalMapEntities = new ArrayList<Entity>();
		TerrainPack terrainPack = new TerrainPack();
		
		DisplayManager.createDisplay();
		Loader loader = new Loader();
		System.setProperty("org.lwjgl.librarypath", new File("natives").getAbsolutePath());
		
//		try {
//			AL.create();
//		} catch (LWJGLException e) {
//			e.printStackTrace();
//		}
//		int source = AL10.alGenSources();
//		int buffer = AL10.alGenBuffers();
//		AL10.alBufferData(buffer, AL10.AL_FORMAT_STEREO16, ByteBuffer.allocate(100), 10);
//		AL10.alSource3f(source, AL10.AL_POSITION, 1, 1, 1);
//		AL10.alSourceQueueBuffers(source, buffer);
//		AL10.alSourcePlay(source);
		
		
		
		// Monkey
		ModelData monkeyData = OBJFileLoader.loadOBJ("character");
		RawModel monkeyModel = loader.loadToVAO(monkeyData);
		TexturedModel texturedMonkeyModel = new TexturedModel(monkeyModel, new ModelTexture(loader.loadTexture("erkky")));
		
		ModelTexture texture = texturedMonkeyModel.getTexture();
		texture.setReflectivity(3);
		texture.setShineDamper(10);
		
		Entity entity = new Entity(texturedMonkeyModel, new Vector3f(0, 0, 20), 0, 0, 0, 1);
		entities.add(entity);
		TexturedModel monkey = new TexturedModel(monkeyModel, new ModelTexture(loader.loadTexture("white")));
		Player player = new Player(monkey, new Vector3f(10, 0, 50), 0, 0, 0, 1, terrainPack);
		entities.add(player);
		Camera camera = new Camera(player, terrainPack);
		
		MasterRenderer renderer = new MasterRenderer(loader, camera);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		TextMaster.init(loader);
		
		FontType font = new FontType(loader.loadTexture("arial"), "arial.fnt");
		GUIText text = new GUIText("Sample text", 3, font, new Vector2f(0.5f, 0.5f), 0.5f, true);
		text.setColour(1, 1, 1);
		
		//Barrel
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader), new ModelTexture(loader.loadTexture("barrel")));
		barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
		barrelModel.getTexture().setShineDamper(10);
		barrelModel.getTexture().setReflectivity(0.5f);
		
		normalMapEntities.add(new Entity(barrelModel, new Vector3f(75, 10, 75), 0, 0, 0, 1f));
		
		Light sun = new Light(new Vector3f(100000, 150000, -70000), new Vector3f(1, 1, 1));
		List<Light> lights = new ArrayList<Light>(); //TODO Sort this list
		lights.add(sun);
		lights.add(new Light(new Vector3f(0, 10, -10), new Vector3f(2, 0, 0), new Vector3f(1, 0.01f, 0.002f)));
		
		// ### Terrain textures ###
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy"));
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("dirt"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("pinkFlowers"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		
		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap"));
		
		terrainPack.addTerrain(new Terrain(0, 1, loader, texturePack, blendMap, SEED));
		terrainPack.addTerrain(new Terrain(1, 1, loader, texturePack, blendMap, SEED));
		terrainPack.addTerrain(new Terrain(0, 0, loader, texturePack, blendMap, SEED));
		terrainPack.addTerrain(new Terrain(1, 0, loader, texturePack, blendMap, SEED));
		
		// ### Random grass generation ###
		
		ModelTexture textureAtlas = new ModelTexture(loader.loadTexture("fern"));
		textureAtlas.setNumberOfRows(2);
		TexturedModel grassModel = new TexturedModel(loader.loadToVAO(OBJFileLoader.loadOBJ("fern")), textureAtlas);
		//grassModel.getTexture().setHasTransparency(true);
		//grassModel.getTexture().setUseFakeLighting(true);
		Random rand = new Random();
		
		for (int i = 0; i < 1000; i++) {
			float x = rand.nextFloat() * 1000;
			float z = rand.nextFloat() * 1000;
			
			entities.add(new Entity(grassModel, rand.nextInt(4), new Vector3f(x, terrainPack.getTerrainHeightByWorldPos(x, z), z), 0, 0, 0, 1));
		}
		
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrainPack, entities);
		
		// #### Water rendering ####
		WaterFrameBuffers buffers = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		List<WaterTile> waterTiles = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(75, 75, 0);
		waterTiles.add(water);
		
		// #### Gui rendering ####
		List<GuiTexture> guis = new ArrayList<GuiTexture>();
		GuiTexture depth = new GuiTexture(buffers.getRefractionDepthTexture(), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		GuiTexture refraction = new GuiTexture(renderer.getShadowMapTexture(), new Vector2f(-0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
		guis.add(depth);
		guis.add(refraction);
		
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particles/cosmic"), 4, true);
		ParticleSystem ps = new ParticleSystem(particleTexture, 50, 10, 0.3f, 4);
		
		while (!Display.isCloseRequested()) {
			entity.increaseRotation(0, 1, 0);
			player.move(terrainPack);
			camera.move();
			picker.update();
			
			if (picker.getCurrentTerrainPoint() != null) {
				lights.get(1).getPosition().x = picker.getCurrentTerrainPoint().x;
				lights.get(1).getPosition().z = picker.getCurrentTerrainPoint().z;
			}
			
			// Update debug info
			
			Terrain currentTerrain = terrainPack.getTerrainByWorldPos(player.getPosition().x, player.getPosition().z);
			if (currentTerrain != null) {
				text.remove();
				Vector3f pos = player.getPosition();
				String textString = "X: " + Math.floor(pos.x) + " Y: " + Math.floor(pos.y) + " Z: " + Math.floor(pos.z);
				text = new GUIText(textString, 1, font, new Vector2f(0.5f, 0f), 0.5f, false);
			}
			renderer.renderShadowMap(entities, sun);
			
			ps.generateParticles(player.getPosition());
			ParticleMaster.update(camera);
			
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			
			// Reflection
			buffers.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			camera.invertRoll();
			renderer.renderScene(entities, normalMapEntities, terrainPack, lights, camera, new Vector4f(0, 1, 0, -water.getHeight()));
			camera.getPosition().y += distance;
			camera.invertPitch();
			camera.invertRoll();
			
			// Refraction
			buffers.bindRefractionFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, terrainPack, lights, camera, new Vector4f(0, -1, 0, water.getHeight()));
			
			// Screen
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			buffers.unbindCurrentFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, terrainPack, lights, camera, new Vector4f(0, 1, 0, Float.MAX_VALUE));
			waterRenderer.render(waterTiles, camera, lights);
			ParticleMaster.renderParticles(camera);
			guiRenderer.render(guis);
			TextMaster.render();
			
			DisplayManager.updateDisplay();
		}
		
		ParticleMaster.cleanUp();
		TextMaster.cleanUp();
		buffers.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();
	}
}
