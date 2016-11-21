package eu.tankernn.gameEngine.terrains;

import static eu.tankernn.gameEngine.settings.Settings.TERRAIN_SIZE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.tuple.Pair;

import eu.tankernn.gameEngine.loader.Loader;
import eu.tankernn.gameEngine.loader.textures.TerrainTexturePack;
import eu.tankernn.gameEngine.loader.textures.Texture;
import eu.tankernn.gameEngine.renderEngine.MasterRenderer;
import eu.tankernn.gameEngine.util.IPositionable;

public class TerrainPack {
	private Map<Pair<Integer, Integer>, Terrain> terrains = new HashMap<Pair<Integer, Integer>, Terrain>();
	private List<Future<TerrainModelData>> waitingData = new ArrayList<Future<TerrainModelData>>();
	private List<IPositionable> waitingForHeight = new ArrayList<IPositionable>();
	private ExecutorService executor = Executors.newCachedThreadPool();
	private int seed;
	private Loader loader;
	private TerrainTexturePack texturePack;
	// TODO Generate blendMap based on seed.
	private Texture blendMap;

	private int lastX = 0, lastZ = 0;

	public TerrainPack(Loader loader, TerrainTexturePack texturePack, Texture blendMap, int seed) {
		this.seed = seed;
		this.loader = loader;
		this.texturePack = texturePack;
		this.blendMap = blendMap;
	}

	public boolean addTerrain(Terrain terrain) {
		if (terrain != null) {
			terrains.put(terrain.getGridPosition(), terrain);
			return true;
		} else {
			return false;
		}
	}

	public List<Terrain> getList() {
		return (List<Terrain>) terrains.values();
	}

	public Pair<Integer, Integer> getGridPosByWorldPos(float x, float z) {
		return Pair.of((int) Math.floor(x / TERRAIN_SIZE), (int) Math.floor(z / TERRAIN_SIZE));
	}

	public Terrain getTerrainByWorldPos(float x, float z) {
		return terrains.get(getGridPosByWorldPos(x, z));
	}

	public float getTerrainHeightByWorldPos(float x, float z) {
		Terrain terrain = getTerrainByWorldPos(x, z);
		if (terrain != null)
			return terrain.getHeightOfTerrain(x, z);
		else
			return 0;
	}

	public void prepareRenderTerrains(MasterRenderer renderer) {
		for (Terrain terrain : terrains.values()) {
			renderer.processTerrain(terrain);
		}
	}

	/**
	 * Generates the terrains in a 3x3 area around the specified position.
	 * 
	 * @param pos
	 *            The object to get the position from
	 */
	public void update(IPositionable pos) {
		Pair<Integer, Integer> currentTerrain = getGridPosByWorldPos(pos.getPosition().x, pos.getPosition().z);

		int newX = currentTerrain.getLeft();
		int newZ = currentTerrain.getRight();

		for (Future<TerrainModelData> futureData : waitingData) {
			if (futureData.isDone()) {
				System.out.println("Adding terrain.");
				try {
					TerrainModelData data = futureData.get();
					terrains.put(Pair.of(data.getGridX(), data.getGridZ()),
							new Terrain(data.getGridX(), data.getGridZ(), loader, texturePack, blendMap, data));
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		
		waitingForHeight.removeIf(p -> {
			if (!terrains.containsKey(getGridPosByWorldPos(p.getPosition().x, p.getPosition().z)))
				return false;
			p.getPosition().setY(getTerrainHeightByWorldPos(p.getPosition().x, p.getPosition().z));
			return true;
		});
		
		// FIXME If a task finishes between the loop above and this statement,
		// the terrain will never be added.
		waitingData.removeIf(f -> f.isDone());

		if (lastX != newX || lastZ != newZ || terrains.isEmpty()) {
			List<Pair<Integer, Integer>> toGenerate = new ArrayList<Pair<Integer, Integer>>();

			for (int x = newX - 1; x <= newX + 1; x++)
				for (int z = newZ - 1; z <= newZ + 1; z++)
					toGenerate.add(Pair.of(x, z));

			for (Pair<Integer, Integer> pair : toGenerate) {
				if (terrains.containsKey(pair) | pair.getLeft() < 0 | pair.getRight() < 0)
					continue;

				Callable<TerrainModelData> task = new Callable<TerrainModelData>() {
					@Override
					public TerrainModelData call() throws Exception {
						return new TerrainModelData(pair.getLeft(), pair.getRight(), seed);
					}
				};

				waitingData.add(executor.submit(task));
			}
			
			for (Pair<Integer, Integer> pair : terrains.keySet()) {
				if (!toGenerate.contains(pair)) {
					terrains.get(pair).delete();
				}
			}
			
			terrains.keySet().retainAll(toGenerate);
			
			System.out.println(terrains.size());

			lastX = newX;
			lastZ = newZ;
		}
	}

	public void cleanUp() {
		executor.shutdown();
	}

	public void addWaitingForTerrainHeight(IPositionable... positionables) {
		waitingForHeight.addAll(Arrays.asList(positionables));
	}

}
