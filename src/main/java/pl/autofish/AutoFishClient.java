package pl.autofish;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

import java.util.concurrent.atomic.AtomicBoolean;

public class AutoFishClient implements ClientModInitializer {
    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("autofish", "main"));

    private static KeyMapping toggleKey;
    private static AutoFishConfig config;

    private static boolean enabled = false;
    private static final AtomicBoolean biteFlag = new AtomicBoolean(false);
    private static int pending = -1;   // ticki do kliknięcia, -1 = brak
    private static int cooldown = 0;

    @Override
    public void onInitializeClient() {
        config = AutoFishConfig.load();
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autofish.toggle", InputConstants.Type.KEYSYM, InputConstants.KEY_V, CATEGORY));
        ClientTickEvents.END_CLIENT_TICK.register(AutoFishClient::tick);
    }

    /** Wywoływane z mixina dla KAŻDEGO dźwięku z serwera (może być wątek sieciowy). */
    public static void onSound(String id) {
        if (!enabled || config == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (config.debug) {
            mc.execute(() -> {
                if (mc.player != null) mc.player.sendSystemMessage(Component.literal("[AutoFish] dźwięk: " + id));
            });
        }
        for (String s : config.biteSounds) {
            if (id.equals(s) || id.equals("minecraft:" + s)) {
                biteFlag.set(true);
                return;
            }
        }
    }

    private static void tick(Minecraft mc) {
        while (toggleKey.consumeClick()) {
            enabled = !enabled;
            reset();
            if (mc.player != null) {
                mc.player.sendSystemMessage(Component.literal(
                        "[AutoFish] " + (enabled ? "WŁĄCZONY" : "WYŁĄCZONY")));
            }
        }
        if (!enabled || mc.player == null || mc.gameMode == null) { reset(); return; }

        boolean bite = biteFlag.getAndSet(false);

        // Działamy tylko gdy spławik jest w wodzie i trzymamy wędkę.
        InteractionHand hand = null;
        if (mc.player.getMainHandItem().is(Items.FISHING_ROD)) hand = InteractionHand.MAIN_HAND;
        else if (mc.player.getOffhandItem().is(Items.FISHING_ROD)) hand = InteractionHand.OFF_HAND;
        if (hand == null || mc.player.fishing == null) { pending = -1; return; }

        if (cooldown > 0) { cooldown--; return; }   // dźwięki w cooldownie są odrzucone

        if (bite && pending < 0) pending = config.reelDelayTicks;

        if (pending >= 0) {
            if (pending == 0) {
                // JEDNO kliknięcie PPM. Bez ponownego zarzucania - spławik zostaje.
                mc.gameMode.useItem(mc.player, hand);
                mc.player.swing(hand);
                pending = -1;
                cooldown = config.cooldownTicks;
            } else {
                pending--;
            }
        }
    }

    private static void reset() {
        biteFlag.set(false);
        pending = -1;
        cooldown = 0;
    }
}
