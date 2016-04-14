package eu.tankernn.gameEngine.entities;

import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import eu.tankernn.gameEngine.terrains.TerrainPack;

public class PlayerCamera extends Camera {
	
	private float distanceFromPlayer = 50;
	private float angleAroundPlayer = 0;
	private float lockedPosition = 0;
	private boolean isLocked = false;
	
	private Player player;
	private TerrainPack terrainPack;
	
	
	public PlayerCamera(Player player, TerrainPack terrainPack) {
		this.player = player;
		this.terrainPack = terrainPack;
	}
	
	/**
	 * Handles player input regarding camera movement.
	 */
	@Override
	public void update() {
		calculateZoom();
		if (Mouse.isButtonDown(0) || Mouse.isButtonDown(1)) {
			if (!this.isLocked) {
				this.isLocked = true;
				this.lockedPosition = player.getRotY();
			}
			try {
				Mouse.setNativeCursor(new Cursor(1, 1, 0, 0, 1, IntBuffer.allocate(1), null)); //Hide the cursor
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
			
			calculatePitch();
			calculateAngleAroundPlayer();
			
			if (Mouse.isButtonDown(1)) {
				float targetRot = this.angleAroundPlayer + this.lockedPosition;
				this.lockedPosition = 0;
				float delta = targetRot - player.getRotY();
				player.increaseRotation(0, delta, 0);
			}
		} else {
			if (this.isLocked) {
				this.isLocked = false;
				this.angleAroundPlayer -= (player.getRotY() - lockedPosition);
			}
			try {
				Mouse.setNativeCursor(null);
			} catch (LWJGLException e) {
				e.printStackTrace();
			}
		}
		
		if (!this.isLocked) {
			adjustToCenter();
		}
		
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		
		if (this.isLocked) {
			this.yaw = 180 - (lockedPosition + angleAroundPlayer);
		} else
			this.yaw = 180 - (player.getRotY() + angleAroundPlayer);
	}
	
	private void adjustToCenter() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			this.angleAroundPlayer = this.angleAroundPlayer % 360;
			
			if (this.angleAroundPlayer < 0)
				this.angleAroundPlayer = 360 + this.angleAroundPlayer;
			
			if (this.angleAroundPlayer - 10 < 0) {
				this.angleAroundPlayer = 0;
				return;
			}
			
			if (this.angleAroundPlayer < 180)
				this.angleAroundPlayer -= 10;
			else
				this.angleAroundPlayer += 10;
		}
	}
	
	private float calculateHorizontalDistance() {
		return (float) (distanceFromPlayer * Math.cos(Math.toRadians(pitch)));
	}
	
	private float calculateVerticalDistance() {
		return (float) (distanceFromPlayer * Math.sin(Math.toRadians(pitch)));
	}
	
	private void calculateCameraPosition(float horizDistance, float verticDistance) {
		float theta;
		if (this.isLocked)
			theta = lockedPosition + angleAroundPlayer;
		else
			theta = player.getRotY() + angleAroundPlayer;
		float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));
		position.x = player.getPosition().x - offsetX;
		position.z = player.getPosition().z - offsetZ;
		position.y = player.getPosition().y + player.getHeight() + verticDistance;
		position.y = Math.max(position.y, terrainPack.getTerrainHeightByWorldPos(position.x, position.z) + 1);
	}
	
	private void calculateZoom() {
		float zoomLevel = Mouse.getDWheel() * 0.1f;
		distanceFromPlayer -= zoomLevel;
		distanceFromPlayer = Math.max(distanceFromPlayer, 10);
		distanceFromPlayer = Math.min(distanceFromPlayer, 100);
	}
	
	private void calculatePitch() {
		float pitchChange = Mouse.getDY() * 0.1f;
		pitch -= pitchChange;
		pitch = Math.min(pitch, 90);
	}
	
	private void calculateAngleAroundPlayer() {
		float angleChange = Mouse.getDX() * 0.3f;
		angleAroundPlayer -= angleChange;
	}
}