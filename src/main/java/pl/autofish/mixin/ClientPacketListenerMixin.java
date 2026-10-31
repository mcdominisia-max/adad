package pl.autofish.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.autofish.AutoFishClient;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void autofish$onSound(ClientboundSoundPacket packet, CallbackInfo ci) {
        AutoFishClient.onSound(packet.getSound().value().location().toString());
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("HEAD"))
    private void autofish$onEntitySound(ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        AutoFishClient.onSound(packet.getSound().value().location().toString());
    }
}
