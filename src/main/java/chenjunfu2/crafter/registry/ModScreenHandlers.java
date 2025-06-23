package chenjunfu2.crafter.registry;

import chenjunfu2.crafter.screen.CrafterScreenHandler;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlag;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers<T extends ScreenHandler> implements ToggleableFeature
{
	public static final ScreenHandlerType<CrafterScreenHandler> CRAFTER_3X3 = register("crafter_3x3", CrafterScreenHandler::new);
	
	private final FeatureSet requiredFeatures;
	private final ScreenHandlerType.Factory<T> factory;
	
	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory) {
		return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.VANILLA_FEATURES));
	}
	
	private static <T extends ScreenHandler> ScreenHandlerType<T> register(String id, ScreenHandlerType.Factory<T> factory, FeatureFlag... requiredFeatures) {
		return Registry.register(Registries.SCREEN_HANDLER, id, new ScreenHandlerType<>(factory, FeatureFlags.FEATURE_MANAGER.featureSetOf(requiredFeatures)));
	}
	
	public ModScreenHandlers(ScreenHandlerType.Factory<T> factory, FeatureSet requiredFeatures) {
		this.factory = factory;
		this.requiredFeatures = requiredFeatures;
	}
	
	public T create(int syncId, PlayerInventory playerInventory) {
		return this.factory.create(syncId, playerInventory);
	}
	
	@Override
	public FeatureSet getRequiredFeatures() {
		return this.requiredFeatures;
	}
	
	public interface Factory<T extends ScreenHandler> {
		T create(int syncId, PlayerInventory playerInventory);
	}
	
	public static void registerScreenHandlers() {}
}
