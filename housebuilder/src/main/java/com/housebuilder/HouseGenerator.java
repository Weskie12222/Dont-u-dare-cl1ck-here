package com.housebuilder;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Builds the houses procedurally, so no structure files are needed.
 *
 * Coordinates are "local" to the house:
 *   u = sideways (positive = to the player's right)
 *   f = forward  (positive = away from the player)
 *   y = up       (0 = the level the builder block was placed on)
 * The builder block sits at u=0, f=0, y=0 and is replaced by the front door.
 */
public final class HouseGenerator {
	private HouseGenerator() {}

	public static void build(Level level, BlockPos origin, Direction forward, HouseStyle style) {
		Ctx c = new Ctx(level, origin, forward);
		switch (style) {
			case WOODEN -> buildWooden(c);
			case MODERN -> buildModern(c);
			case MILITARY -> buildMilitary(c);
		}
		c.settleAll();
	}

	// ------------------------------------------------------------------
	// Wooden house: 9 wide x 7 deep, oak walls, dark oak gable roof
	// ------------------------------------------------------------------
	private static void buildWooden(Ctx c) {
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState planks = Blocks.OAK_PLANKS.defaultBlockState();
		BlockState log = Blocks.OAK_LOG.defaultBlockState();
		BlockState floor = Blocks.SPRUCE_PLANKS.defaultBlockState();
		BlockState cobble = Blocks.COBBLESTONE.defaultBlockState();
		BlockState roofPlanks = Blocks.DARK_OAK_PLANKS.defaultBlockState();
		BlockState pane = Blocks.GLASS_PANE.defaultBlockState();
		Direction back = c.fwd.getOpposite();

		// Clear the space (trees, hills...) and lay the foundation + floor.
		c.fill(-6, 6, -2, 8, 0, 10, air);
		c.foundation(-4, 4, 0, 6, cobble);
		c.fill(-4, 4, 0, 6, -1, -1, floor);
		c.fill(-1, 1, -1, -1, -1, -1, floor); // porch

		// Walls: oak logs on the corners, planks elsewhere.
		for (int u = -4; u <= 4; u++) {
			for (int f = 0; f <= 6; f++) {
				boolean edge = Math.abs(u) == 4 || f == 0 || f == 6;
				if (!edge) continue;
				boolean corner = Math.abs(u) == 4 && (f == 0 || f == 6);
				c.fill(u, u, f, f, 0, 3, corner ? log : planks);
			}
		}

		// Windows (u, f), two panes tall.
		int[][] windows = {{-2, 0}, {2, 0}, {-2, 6}, {0, 6}, {2, 6}, {-4, 2}, {-4, 4}, {4, 2}, {4, 4}};
		for (int[] w : windows) {
			c.setSettled(w[0], w[1], 1, pane);
			c.setSettled(w[0], w[1], 2, pane);
		}

		// Front door (replaces the builder block).
		c.door(0, 0, Blocks.SPRUCE_DOOR);

		// Gable roof: stairs climb toward the ridge, ends are filled with planks.
		for (int k = 0; k < 4; k++) {
			int y = 4 + k;
			int ul = 4 - k;
			for (int f = -1; f <= 7; f++) {
				c.set(-ul, f, y, c.facing(Blocks.DARK_OAK_STAIRS, c.right));
				c.set(ul, f, y, c.facing(Blocks.DARK_OAK_STAIRS, c.right.getOpposite()));
			}
			c.fill(-(ul - 1), ul - 1, 0, 0, y, y, planks);
			c.fill(-(ul - 1), ul - 1, 6, 6, y, y, planks);
		}
		c.fill(0, 0, -1, 7, 8, 8, roofPlanks); // ridge

		// Interior.
		BlockState hangingLantern = Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true);
		c.set(0, 2, 7, hangingLantern);
		c.set(0, 4, 7, hangingLantern);
		c.set(-3, 1, 0, Blocks.LANTERN.defaultBlockState());
		c.set(3, 1, 0, Blocks.LANTERN.defaultBlockState());

		c.set(-3, 5, 0, Blocks.CRAFTING_TABLE.defaultBlockState());
		c.set(-2, 5, 0, c.facing(Blocks.FURNACE, back));
		c.set(-1, 5, 0, c.facing(Blocks.CHEST, back));
		c.bed(3, 4, 0, Blocks.RED_BED);
	}

	// ------------------------------------------------------------------
	// Modern house: 11 wide x 9 deep, two floors, glass + concrete, flat roof
	// ------------------------------------------------------------------
	private static void buildModern(Ctx c) {
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState white = Blocks.WHITE_CONCRETE.defaultBlockState();
		BlockState black = Blocks.BLACK_CONCRETE.defaultBlockState();
		BlockState gray = Blocks.GRAY_CONCRETE.defaultBlockState();
		BlockState lightGray = Blocks.LIGHT_GRAY_CONCRETE.defaultBlockState();
		BlockState glass = Blocks.GLASS.defaultBlockState();
		Direction back = c.fwd.getOpposite();

		// Clear + foundation + ground floor + front path.
		c.fill(-7, 7, -3, 11, 0, 10, air);
		c.foundation(-5, 5, 0, 8, gray);
		c.fill(-5, 5, 0, 8, -1, -1, lightGray);
		c.fill(-1, 1, -3, -1, -1, -1, Blocks.SMOOTH_STONE.defaultBlockState());

		// Walls, ground floor + upper floor.
		for (int u = -5; u <= 5; u++) {
			for (int f = 0; f <= 8; f++) {
				boolean edge = Math.abs(u) == 5 || f == 0 || f == 8;
				if (!edge) continue;
				for (int y = 0; y <= 7; y++) {
					c.set(u, f, y, modernWall(u, f, y, white, black, glass));
				}
			}
		}

		// Upper floor slab, with a hole for the staircase along the left wall.
		c.fill(-4, 4, 1, 7, 4, 4, lightGray);
		c.fill(-4, -4, 1, 4, 4, 4, air);

		// Staircase: five steps climbing forward along the left wall.
		for (int i = 0; i < 5; i++) {
			if (i > 0) {
				c.fill(-4, -4, 1 + i, 1 + i, 0, i - 1, Blocks.SMOOTH_QUARTZ.defaultBlockState());
			}
			c.set(-4, 1 + i, i, c.facing(Blocks.QUARTZ_STAIRS, c.fwd));
		}

		// Flat roof with a 1-block overhang.
		c.fill(-6, 6, -1, 9, 8, 8, black);

		// Front door.
		c.door(0, 0, Blocks.DARK_OAK_DOOR);

		// Lighting: hanging lanterns on both floors.
		BlockState hangingLantern = Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true);
		for (int u : new int[] {-2, 2}) {
			for (int f : new int[] {3, 6}) {
				c.set(u, f, 3, hangingLantern);
				c.set(u, f, 7, hangingLantern);
			}
		}

		// Ground floor: utility wall.
		c.set(1, 7, 0, c.facing(Blocks.CHEST, back));
		c.set(2, 7, 0, c.facing(Blocks.FURNACE, back));
		c.set(3, 7, 0, Blocks.CRAFTING_TABLE.defaultBlockState());

		// Upper floor: bed + bookshelves.
		c.bed(3, 5, 5, Blocks.RED_BED);
		c.fill(-3, -1, 7, 7, 5, 6, Blocks.BOOKSHELF.defaultBlockState());
	}

	// ------------------------------------------------------------------
	// Military barracks: 15 wide x 13 deep stone-brick fortress with four
	// corner watchtowers, battlements, an iron door, a barracks hall with
	// 8 beds + footlockers, and a back armory with workstations.
	// ------------------------------------------------------------------
	private static void buildMilitary(Ctx c) {
		BlockState air = Blocks.AIR.defaultBlockState();
		BlockState stone = Blocks.STONE_BRICKS.defaultBlockState();
		BlockState plinth = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
		BlockState roof = Blocks.DEEPSLATE_TILES.defaultBlockState();
		BlockState floor = Blocks.POLISHED_ANDESITE.defaultBlockState();
		BlockState bars = Blocks.IRON_BARS.defaultBlockState();
		BlockState hangingLantern = Blocks.LANTERN.defaultBlockState().setValue(BlockStateProperties.HANGING, true);
		Direction back = c.fwd.getOpposite();
		Direction left = c.right.getOpposite();

		// Clear, foundation, floor, and a stone-brick path to the gate.
		c.fill(-9, 9, -5, 14, 0, 12, air);
		c.foundation(-7, 7, 0, 12, plinth);
		c.fill(-7, 7, 0, 12, -1, -1, floor);
		c.fill(-1, 1, -4, -1, -1, -1, stone);

		// Outer walls: deepslate plinth, stone brick above.
		for (int u = -7; u <= 7; u++) {
			for (int f = 0; f <= 12; f++) {
				boolean edge = Math.abs(u) == 7 || f == 0 || f == 12;
				if (!edge) continue;
				c.fill(u, u, f, f, 0, 0, plinth);
				c.fill(u, u, f, f, 1, 4, stone);
			}
		}

		// Flat roof plus battlements (alternating blocks) along the wall tops.
		c.fill(-7, 7, 0, 12, 5, 5, roof);
		for (int u = -7; u <= 7; u++) {
			for (int f = 0; f <= 12; f++) {
				boolean edge = Math.abs(u) == 7 || f == 0 || f == 12;
				if (edge && Math.floorMod(u + f, 2) == 0) {
					c.set(u, f, 6, stone);
				}
			}
		}

		// Four 3x3 corner watchtowers, 9 high, with battlements and a beacon lantern.
		for (int su : new int[] {-1, 1}) {
			for (int tf : new int[] {0, 10}) {
				int uMin = su < 0 ? -7 : 5;
				int uMax = su < 0 ? -5 : 7;
				c.fill(uMin, uMax, tf, tf + 2, 0, 8, stone);
				for (int u = uMin; u <= uMax; u++) {
					for (int f = tf; f <= tf + 2; f++) {
						boolean center = u == (uMin + uMax) / 2 && f == tf + 1;
						if (center) {
							c.set(u, f, 9, Blocks.LANTERN.defaultBlockState());
						} else if (Math.floorMod(u + f, 2) == 0) {
							c.set(u, f, 9, stone);
						}
					}
				}
			}
		}

		// Interior dividers: entry hall | barracks | armory, each with a 3-wide archway.
		for (int f : new int[] {3, 8}) {
			c.fill(-6, 6, f, f, 0, 4, stone);
			c.fill(-1, 1, f, f, 0, 2, air);
		}

		// Arrow-slit windows (iron bars, two tall).
		int[][] slits = {{-3, 0}, {3, 0}, {-7, 5}, {-7, 6}, {7, 5}, {7, 6}, {-7, 9}, {7, 9}, {-3, 12}, {0, 12}, {3, 12}};
		for (int[] s : slits) {
			c.setSettled(s[0], s[1], 2, bars);
			c.setSettled(s[0], s[1], 3, bars);
		}

		// Iron gate with a button on each side (replaces the builder block).
		c.door(0, 0, Blocks.IRON_DOOR);
		c.wallButton(1, -1, 1, back);
		c.wallButton(1, 1, 1, c.fwd);

		// Lighting.
		int[][] lights = {{-3, 2}, {3, 2}, {-3, 5}, {3, 5}, {-3, 7}, {3, 7}, {-2, 10}, {0, 10}, {2, 10}};
		for (int[] l : lights) {
			c.set(l[0], l[1], 4, hangingLantern);
		}

		// Entry hall: guard lectern + alarm bell.
		c.set(-3, 1, 0, c.facing(Blocks.LECTERN, c.fwd));
		c.set(3, 1, 0, Blocks.BELL.defaultBlockState());

		// Barracks: four beds per side (head to the wall) with a footlocker at each bed.
		for (int f = 4; f <= 7; f++) {
			c.bed(-5, f, 0, Blocks.GREEN_BED, left);
			c.set(-4, f, 0, c.facing(Blocks.CHEST, c.right));
			c.bed(5, f, 0, Blocks.GREEN_BED, c.right);
			c.set(4, f, 0, c.facing(Blocks.CHEST, left));
		}

		// Armory: workstations and weapon lockers along the back wall.
		c.set(-4, 11, 0, Blocks.SMITHING_TABLE.defaultBlockState());
		c.set(-3, 11, 0, c.facing(Blocks.ANVIL, c.fwd));
		c.set(-2, 11, 0, c.facing(Blocks.GRINDSTONE, back)
			.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR));
		c.set(-1, 11, 0, c.facing(Blocks.BLAST_FURNACE, back));
		c.set(0, 11, 0, Blocks.CRAFTING_TABLE.defaultBlockState());
		for (int u = 1; u <= 4; u++) {
			c.set(u, 11, 0, c.facing(Blocks.CHEST, back));
		}
		for (int s : new int[] {-1, 1}) {
			c.set(4 * s, 10, 0, Blocks.BARREL.defaultBlockState());
			c.set(4 * s, 10, 1, Blocks.BARREL.defaultBlockState());
			c.set(5 * s, 9, 0, Blocks.BARREL.defaultBlockState());
			c.set(6 * s, 9, 0, Blocks.BARREL.defaultBlockState());
		}

		// Forecourt: flagpole with a green banner of wool, and a sandbag barricade.
		c.fill(-4, -4, -3, -3, 0, 8, Blocks.COBBLESTONE_WALL.defaultBlockState());
		c.fill(-3, -1, -3, -3, 6, 7, Blocks.GREEN_WOOL.defaultBlockState());
		c.fill(3, 6, -3, -3, 0, 0, Blocks.SANDSTONE.defaultBlockState());
		c.fill(4, 5, -3, -3, 1, 1, Blocks.SANDSTONE.defaultBlockState());
	}

	private static BlockState modernWall(int u, int f, int y, BlockState white, BlockState black, BlockState glass) {
		boolean cornerPillar = Math.abs(u) == 5 && (f == 0 || f == 8);
		if (cornerPillar || y == 4 || y == 7) {
			return black;
		}

		int au = Math.abs(u);
		boolean upper = y >= 5;
		boolean glassBand = y == 1 || y == 2 || y == 5 || y == 6;
		boolean isGlass;

		if (f == 0) { // front wall
			isGlass = upper ? (au <= 4 && glassBand) : (au >= 2 && au <= 4 && y <= 2);
		} else if (au == 5) { // side walls
			isGlass = f >= 2 && f <= 6 && glassBand;
		} else { // back wall
			isGlass = au <= 3 && glassBand;
		}
		return isGlass ? glass : white;
	}

	// ------------------------------------------------------------------
	// Helper that converts local (u, f, y) coordinates to world positions.
	// ------------------------------------------------------------------
	private static final class Ctx {
		// setBlock flags: 2 = send to clients, no neighbour updates (fast, avoids pops)
		private static final int FLAGS = 2;

		final Level level;
		final BlockPos origin;
		final Direction fwd;
		final Direction right;
		private final List<BlockPos> toSettle = new ArrayList<>();

		Ctx(Level level, BlockPos origin, Direction fwd) {
			this.level = level;
			this.origin = origin;
			this.fwd = fwd;
			this.right = fwd.getClockWise();
		}

		BlockPos at(int u, int f, int y) {
			return origin.relative(right, u).relative(fwd, f).above(y);
		}

		void set(int u, int f, int y, BlockState state) {
			level.setBlock(at(u, f, y), state, FLAGS);
		}

		/** Places a block that must later re-connect to its neighbours (glass panes). */
		void setSettled(int u, int f, int y, BlockState state) {
			BlockPos pos = at(u, f, y);
			level.setBlock(pos, state, FLAGS);
			toSettle.add(pos);
		}

		void settleAll() {
			for (BlockPos pos : toSettle) {
				BlockState current = level.getBlockState(pos);
				BlockState updated = Block.updateFromNeighbourShapes(current, level, pos);
				if (updated != current) {
					level.setBlock(pos, updated, FLAGS);
				}
			}
		}

		void fill(int u1, int u2, int f1, int f2, int y1, int y2, BlockState state) {
			for (int u = Math.min(u1, u2); u <= Math.max(u1, u2); u++) {
				for (int f = Math.min(f1, f2); f <= Math.max(f1, f2); f++) {
					for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
						set(u, f, y, state);
					}
				}
			}
		}

		/** Fills empty/replaceable/water space under the floor (y=-2 downward) so houses never float. */
		void foundation(int u1, int u2, int f1, int f2, BlockState state) {
			for (int u = u1; u <= u2; u++) {
				for (int f = f1; f <= f2; f++) {
					for (int y = -2; y >= -16; y--) {
						BlockPos pos = at(u, f, y);
						BlockState existing = level.getBlockState(pos);
						boolean gap = existing.isAir() || existing.canBeReplaced() || !existing.getFluidState().isEmpty();
						if (!gap) break;
						level.setBlock(pos, state, FLAGS);
					}
				}
			}
		}

		BlockState facing(Block block, Direction direction) {
			return block.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, direction);
		}

		void door(int u, int f, Block door) {
			BlockState base = facing(door, fwd);
			set(u, f, 0, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
			set(u, f, 1, base.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
		}

		/** Foot at (u, f, y), head one block further forward. */
		void bed(int u, int f, int y, Block bed) {
			bed(u, f, y, bed, fwd);
		}

		/** Foot at (u, f, y), head one block further in the given direction. */
		void bed(int u, int f, int y, Block bed, Direction dir) {
			int du = 0;
			int df = 0;
			if (dir == fwd) {
				df = 1;
			} else if (dir == fwd.getOpposite()) {
				df = -1;
			} else if (dir == right) {
				du = 1;
			} else {
				du = -1;
			}
			BlockState base = facing(bed, dir);
			set(u, f, y, base.setValue(BlockStateProperties.BED_PART, BedPart.FOOT));
			set(u + du, f + df, y, base.setValue(BlockStateProperties.BED_PART, BedPart.HEAD));
		}

		/** Stone button stuck to the wall face; {@code outward} is the direction the button faces. */
		void wallButton(int u, int f, int y, Direction outward) {
			set(u, f, y, Blocks.STONE_BUTTON.defaultBlockState()
				.setValue(BlockStateProperties.ATTACH_FACE, AttachFace.WALL)
				.setValue(BlockStateProperties.HORIZONTAL_FACING, outward));
		}
	}
}
