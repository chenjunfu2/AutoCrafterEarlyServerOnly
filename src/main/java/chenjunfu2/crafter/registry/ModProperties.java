package chenjunfu2.crafter.registry;

import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;

public class ModProperties
{
	public static final BooleanProperty CRAFTING = BooleanProperty.of("crafting");
	public static final EnumProperty<JigsawOrientation> ORIENTATION = EnumProperty.of("orientation", JigsawOrientation.class);
}
