package io.github.tbenassi.com.hotdeposit.client.mixin;

import io.github.tbenassi.com.hotdeposit.client.HotDepositClient;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
    @Inject(method = "interactBlock", at = @At("HEAD"))
    public void interactBlockHead(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        BlockEntity blockEntity = player.getWorld().getBlockEntity(hitResult.getBlockPos());
        if (blockEntity instanceof ChestBlockEntity ||
                blockEntity instanceof ShulkerBoxBlockEntity ||
                blockEntity instanceof BarrelBlockEntity ||
                blockEntity instanceof EnderChestBlockEntity) {
            // check if chest is blocked
            if (ChestBlock.isChestBlocked(player.getWorld(), blockEntity.getPos())) {
                return;
            }
            // check if double chest is blocked
            if (HotDepositClient.isDoubleChest(blockEntity)) {
                BlockPos otherHalfPos = HotDepositClient.getTheOtherHalfPosOfLargeChest(player.getWorld(), blockEntity.getPos());
                if (otherHalfPos != null && ChestBlock.isChestBlocked(player.getWorld(), otherHalfPos)) {
                    return;
                }
            }
            if (!HotDepositClient.isHotDepositRunning() && !(ChestBlock.isChestBlocked(player.getWorld(), blockEntity.getPos()))) {
                HotDepositClient.clearContainerQueue(); // This helps make sure the container queue matches the screen queue
                HotDepositClient.addContainerToQueue(blockEntity);
            }
        }
    }
}
