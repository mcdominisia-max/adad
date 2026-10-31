package pl.autofish;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Plik: .minecraft/config/autofish.json */
public class AutoFishConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** ID dźwięków oznaczających branie ryby. Domyślnie vanilla "splash" spławika (NIE "throw" = zarzucenie). */
    public List<String> biteSounds = new ArrayList<>(List.of("entity.fishing_bobber.splash"));
    /** Opóźnienie (w tickach, 20 = 1 s) między braniem a kliknięciem. */
    public int reelDelayTicks = 2;
    /** Po kliknięciu ignoruj kolejne dźwięki przez tyle ticków (ochrona przed podwójnym kliknięciem). */
    public int cooldownTicks = 20;
    /** true = wypisuje na czacie ID każdego dźwięku (do znalezienia dźwięku brania na CraftPlay). */
    public boolean debug = false;

    public static AutoFishConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("autofish.json");
        try {
            if (Files.exists(path)) {
                AutoFishConfig c = GSON.fromJson(Files.readString(path), AutoFishConfig.class);
                if (c != null && c.biteSounds != null) return c;
            }
        } catch (Exception e) {
            System.err.println("[AutoFish] Nie można wczytać configu: " + e);
        }
        AutoFishConfig c = new AutoFishConfig();
        try { Files.writeString(path, GSON.toJson(c)); } catch (Exception ignored) { }
        return c;
    }
}
