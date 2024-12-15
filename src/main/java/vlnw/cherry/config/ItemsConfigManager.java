package vlnw.cherry.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ItemsConfigManager {
    private final Map<Material, Integer> itemPointsMap = new HashMap<>();
    private final FileConfiguration config;

    public ItemsConfigManager(FileConfiguration config) {
        this.config = config;
        loadConfig(config);
    }

    public void loadConfig(FileConfiguration config) {
        itemPointsMap.clear();

        // Cargar puntos de armaduras, armas, herramientas y especiales
        loadItemPoints(config.getConfigurationSection("items"));
    }

    private void loadItemPoints(ConfigurationSection section) {
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            Material material = Material.matchMaterial(key.toUpperCase());
            if (material != null) {
                int points = section.getInt(key, 0);
                itemPointsMap.put(material, points);
            }
        }
    }
    // Añadir un nuevo ítem con sus puntos
    public void addItem(Material material, int points) {
        itemPointsMap.put(material, points); // Actualizar el mapa
        config.set("items." + material.name(), points); // Actualizar el archivo
        saveConfig();
    }

    // Actualizar un ítem existente
    public boolean updateItem(Material material, int points) {
        if (itemPointsMap.containsKey(material)) {
            itemPointsMap.put(material, points); // Actualizar el mapa
            config.set("items." + material.name(), points); // Actualizar el archivo
            saveConfig();
            return true;
        }
        return false;
    }

    // Eliminar un ítem existente
    public boolean removeItem(Material material) {
        if (itemPointsMap.containsKey(material)) {
            itemPointsMap.remove(material); // Actualizar el mapa
            config.set("items." + material.name(), null); // Eliminar del archivo
            saveConfig();
            return true;
        }
        return false;
    }

    public int getItemPoints(Material material) {
        return itemPointsMap.getOrDefault(material, 0);
    }
    public void saveConfig() {
        try {
            ((YamlConfiguration) config).save(new File(config.getCurrentPath())); // Guarda el archivo de configuración
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
