package com.housebuilder;

import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
	private ModBlocks() {}

	public static final Block WOODEN_HOUSE_BUILDER = register(
		"wooden_house_builder",
		props -> new HouseBuilderBlock(props, HouseStyle.WOODEN),
		BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.WOOD)
	);

	public static final Block MODERN_HOUSE_BUILDER = register(
		"modern_house_builder",
		props -> new HouseBuilderBlock(props, HouseStyle.MODERN),
		BlockBehaviour.Properties.of().strength(1.5f).sound(SoundType.STONE)
	);

	public static final Block MILITARY_BARRACKS_BUILDER = register(
		"military_barracks_builder",
		props -> new HouseBuilderBlock(props, HouseStyle.MILITARY),
		BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.METAL)
	);

	private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties props) {
		ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, HouseBuilderMod.id(name));
		Block block = factory.apply(props.setId(blockKey));
		Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, HouseBuilderMod.id(name));
		BlockItem item = new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix());
		Registry.register(BuiltInRegistries.ITEM, itemKey, item);

		return block;
	}

	/** Called from the mod initializer to trigger static initialisation. */
	public static void initialize() {}
}
