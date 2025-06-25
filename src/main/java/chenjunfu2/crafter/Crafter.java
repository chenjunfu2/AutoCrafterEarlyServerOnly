package chenjunfu2.crafter;

import chenjunfu2.crafter.registry.ModBlockEntities;
import chenjunfu2.crafter.registry.ModBlocks;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemGroups;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Crafter implements ModInitializer {
	public static final String MOD_ID = "crafter";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize()
	{
		//ModScreenHandlers.registerScreenHandlers();
		
		ModBlocks.registerBlocks();
		ModBlockEntities.registerBlockEntities();
		
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> content.addAfter(Blocks.DROPPER, ModBlocks.CRAFTER_BLOCK));
	}
}