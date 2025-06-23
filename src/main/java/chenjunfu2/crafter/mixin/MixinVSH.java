package chenjunfu2.crafter.mixin;

import eu.pb4.sgui.virtual.inventory.VirtualScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VirtualScreenHandler.class)
public abstract class MixinVSH
{
	@Inject(method = "setStackInSlot", at = @At(value = "TAIL"))
	private void Inj(int slot, int i, ItemStack stack, CallbackInfo ci)
	{
		((ScreenHandler)(Object)this).setStackInSlot(slot,i,stack);
	}
	
	
}
