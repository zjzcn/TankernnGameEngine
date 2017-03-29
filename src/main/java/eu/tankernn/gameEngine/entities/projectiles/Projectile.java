package eu.tankernn.gameEngine.entities.projectiles;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.gameEngine.entities.Entity3D;
import eu.tankernn.gameEngine.loader.models.AABB;
import eu.tankernn.gameEngine.loader.models.TexturedModel;
import eu.tankernn.gameEngine.particles.ParticleSystem;
import eu.tankernn.gameEngine.terrains.TerrainPack;

public abstract class Projectile extends Entity3D {
	
	private ParticleSystem particleSystem;
	private final float range;
	private final Vector3f startPosition;
	
	public Projectile(TerrainPack terrain, TexturedModel model, Vector3f position, Vector3f velocity, float range, AABB boundingBox, ParticleSystem particleSystem) {
		super(model, position, boundingBox, terrain);
		this.particleSystem = particleSystem;
		this.velocity = velocity;
		this.range = range;
		this.startPosition = new Vector3f(position);
	}
	
	public void update() {
		super.update();
		particleSystem.setPosition(this.getPosition());
		
		if (this.terrain != null) {
			this.position.y = Math.max(this.position.y, 5 + terrain.getTerrainHeightByWorldPos(position.x, position.z));
		}
		
		Vector3f distance = Vector3f.sub(position, startPosition, null);
		if (distance.length() > range) {
			kill();
		}
	}
	
	public void checkCollision(List<Entity3D> entities) {
		entities.stream().filter((e) -> AABB.collides(e.getBoundingBox(), Projectile.this.getBoundingBox())).forEach(this::onCollision);
	}

	protected void kill() {
		particleSystem.remove();
		this.dead = true;
	}
	
	public abstract void onCollision(Entity3D entity);
}
