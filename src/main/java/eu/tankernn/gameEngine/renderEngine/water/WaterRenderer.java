package eu.tankernn.gameEngine.renderEngine.water;

import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.gameEngine.entities.Camera;
import eu.tankernn.gameEngine.entities.ILight;
import eu.tankernn.gameEngine.loader.Loader;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.renderEngine.DisplayManager;
import eu.tankernn.gameEngine.renderEngine.Vao;
import eu.tankernn.gameEngine.settings.Settings;
import eu.tankernn.gameEngine.util.Maths;

public class WaterRenderer {
	
	private static final float WAVE_SPEED = 0.03f;
	private static final float WAVE_STRENGTH = 0.04f;
	private static final float SHINE_DAMPER = 20.0f;
	private static final float REFLECTIVITY = 0.5f;
	
	private Vao quad;
	private WaterShader shader;
	private WaterFrameBuffers buffers;
	
	private float moveFactor = 0;
	
	private Texture dudvTexture;
	private Texture normalMap;
	
	public WaterRenderer(Loader loader, Texture dudvTexture, Texture normalMap, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers buffers) {
		this.shader = shader;
		this.buffers = buffers;
		this.dudvTexture = dudvTexture;
		this.normalMap = normalMap;
		shader.start();
		shader.connectTextureUnits();
		shader.projectionMatrix.loadMatrix(projectionMatrix);
		shader.stop();
		setUpVAO(loader);
	}
	
	public void render(List<WaterTile> water, Camera camera, List<ILight> lights) {
		prepareRender(camera, lights);
		for (WaterTile tile: water) {
			Matrix4f modelMatrix = Maths.createTransformationMatrix(new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), new Vector3f(0, 0, 0), tile.getSize());
			shader.modelMatrix.loadMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getIndexCount());
		}
		unbind();
	}
	
	private void prepareRender(Camera camera, List<ILight> lights) {
		shader.start();
		shader.viewMatrix.loadCamera(camera);
		shader.cameraPosition.loadVec3(camera.getPosition());
		moveFactor += WAVE_SPEED * DisplayManager.getFrameTimeSeconds();
		moveFactor %= 1;
		shader.moveFactor.loadFloat(moveFactor);
		shader.loadLights(lights);
		shader.nearPlane.loadFloat(Settings.NEAR_PLANE);
		shader.farPlane.loadFloat(Settings.FAR_PLANE);
		shader.waveStrength.loadFloat(WAVE_STRENGTH);
		shader.shineDamper.loadFloat(SHINE_DAMPER);
		shader.reflectivity.loadFloat(REFLECTIVITY);
		quad.bind(0);
		buffers.getReflectionFbo().getColourTexture().bindToUnit(0);
		buffers.getRefractionFbo().getColourTexture().bindToUnit(1);
		dudvTexture.bindToUnit(2);
		normalMap.bindToUnit(3);
		buffers.getRefractionFbo().getDepthTexture().bindToUnit(4);
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void unbind() {
		GL11.glDisable(GL11.GL_BLEND);
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		shader.stop();
	}
	
	private void setUpVAO(Loader loader) {
		// Just x and z vertex positions here, y is set to 0 in v.shader
		float[] vertices = {-1, -1, -1, 1, 1, -1, 1, -1, -1, 1, 1, 1};
		quad = loader.loadToVAO(vertices, 2);
	}
	
}
