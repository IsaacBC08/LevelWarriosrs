package vlnw.cherry;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public class LevelWarriorsPlaceholder extends PlaceholderExpansion {
    private final LevelWarriors plugin;

    public LevelWarriorsPlaceholder(LevelWarriors plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true; // Hace que la expansión persista después de recargar PlaceholderAPI
    }

    @Override
    public boolean canRegister() {
        return true; // Permite registrar esta expansión
    }

    @Override
    public String getIdentifier() {
        return "levelwarriors"; // El identificador de tu placeholder
        //levelwarriors.lvs == player.uuid
        //
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(plugin.getPlayerLevel(player)); // Devuelve el nivel del jugador
        }

        return null; // Si el placeholder no coincide, devuelve null
    }
}
