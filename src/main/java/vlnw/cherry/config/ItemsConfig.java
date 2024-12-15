package vlnw.cherry.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import vlnw.cherry.LevelWarriors;

import java.io.File;
import java.io.IOException;

public class ItemsConfig {
    private LevelWarriors plugin;
    private String filename;
    private FileConfiguration fileConfiguration;
    private File file = null;
    private String folderName;

    public ItemsConfig(String filename, String folderName, LevelWarriors plugin) {
        this.filename = filename;
        this.folderName = folderName;
        this.plugin = plugin;
    }
    public String getPath() {
        return filename;
    }

    public void registerConfig() {
        if (folderName != null) {
            file = new File(plugin.getDataFolder() + File.separator + folderName, filename);
        }
        else {
            file = new File(plugin.getDataFolder(), filename);
        }

        if (!file.exists()){
            if (folderName != null){
                plugin.saveResource(folderName + File.separator + filename,false);
            }
            else {
                plugin.saveResource(filename, false);
            }
        }

        fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(file);
        }
        catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void safeConfig(){
        try {
            fileConfiguration.save(file);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        return fileConfiguration;
    }

    public boolean reloadConfig() {
        if (fileConfiguration == null) {
            if (folderName != null) {
                file = new File(plugin.getDataFolder() + File.separator + folderName, filename);
            } else {
                file = new File(plugin.getDataFolder(), filename);
            }
        }
        fileConfiguration = YamlConfiguration.loadConfiguration(file);
        if (file != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(file);
            fileConfiguration.setDefaults(defConfig);
        }
        return true;
    }

}
