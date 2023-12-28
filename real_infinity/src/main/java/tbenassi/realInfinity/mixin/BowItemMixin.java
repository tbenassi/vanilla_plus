package tbenassi.realInfinity.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BowItem.class)
public abstract class BowItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void redirectUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        ItemStack itemStack = user.getStackInHand(hand);
        boolean bl = !user.getProjectileType(itemStack).isEmpty() || EnchantmentHelper.getLevel(Enchantments.INFINITY, itemStack) > 0;
        if (!user.getAbilities().creativeMode && !bl) {
            cir.setReturnValue(TypedActionResult.fail(itemStack));
        } else {
            user.setCurrentHand(hand);
            cir.setReturnValue(TypedActionResult.consume(itemStack));
        }
    }
}