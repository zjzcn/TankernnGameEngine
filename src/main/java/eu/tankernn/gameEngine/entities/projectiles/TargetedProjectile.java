package eu.tankernn.gameEngine.entities.projectiles;

import org.lwjgl.util.vector.Vector3f;

import eu.tankernn.gameEngine.entities.Entity3D;
import eu.tankernn.gameEngine.loader.models.AABB;
import eu.tankernn.gameEngine.loader.models.TexturedModel;
import eu.tankernn.gameEngine.particles.ParticleSystem;
import eu.tankernn.gameEngine.terrains.TerrainPack;
import eu.tankernn.gameEngine.util.IPositionable;

public class TargetedProjectile extends Projectile {
	
	private IPositionable target;
	private float speed;
	
	public TargetedProjectile(TerrainPack terrain, TexturedModel model, Vector3f position, IPositionable target, float speed, AABB boundingBox, ParticleSystem particleSystem) {
		super(terrain, model, position, new Vector3f(0, 0, 0), Float.MAX_VALUE, boundingBox, particleSystem);
		this.target = target;
		this.speed = speed;
	}
	
	@Override
	public void update() {
		// Store direction in velocity vector
		Vector3f.sub(target.getPosition(), this.getPosition(), this.getVelocity());
		// Normalize and scale velocity vector
		getVelocity().normalise().scale(speed);
		super.update();
	}
	
	@Override
	public void onCollision(Entity3D entity) {
		if (entity.equals(target))
			this.kill();
	}
	
}
