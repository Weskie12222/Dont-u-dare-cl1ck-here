package com.housebuilder;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;

public class HouseBuilderMod implements ModInitializer {
	public static final String MOD_ID = "housebuilder";

	@Override
	public void onInitialize() {
		ModBlocks.initialize();

		// Show both builder blocks in the "Building Blocks" creative tab.
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS).register(output -> {
			output.accept(ModBlocks.WOODEN_HOUSE_BUILDER);
			output.accept(ModBlocks.MODERN_HOUSE_BUILDER);
			output.accept(ModBlocks.MILITARY_BARRACKS_BUILDER);
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
