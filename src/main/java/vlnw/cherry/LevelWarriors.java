package vlnw.cherry;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import vlnw.cherry.config.ItemsConfigManager;

import java.io.File;

public class LevelWarriors extends JavaPlugin implements Listener {
    private ItemsConfigManager itemsConfigManager;

    @Override
    public void onEnable() {
        // Copiar el archivo levelitems.yml del JAR al directorio de configuración del servidor si no existe
        saveResource("levelitems.yml", false);

        // Cargar la configuración desde el archivo en el directorio del servidor
        File levelItemsFile = new File(getDataFolder(), "levelitems.yml");
        FileConfiguration levelItemsConfig = YamlConfiguration.loadConfiguration(levelItemsFile);

        // Inicializar ItemsConfigManager
        itemsConfigManager = new ItemsConfigManager(levelItemsConfig);
        new LevelWarriorsPlaceholder(this).register();
        getLogger().info("PlaceholderAPI detectado y placeholders registrados!");
        // Registrar eventos
        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info(ChatColor.translateAlternateColorCodes('&', "&b[&d&lLevelWarriors plugin activado!&b]"));
    }


    @Override
    public void onDisable() {
        getLogger().info("LevelWarriors plugin desactivado!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "lvlwtop":
                // Comando para obtener el jugador con mayor nivel
                if (sender instanceof Player || sender instanceof ConsoleCommandSender) {
                    Player topPlayer = getTopPlayer();
                    if (topPlayer != null) {
                        double topPlayerLevel = calculatePowerLevel(topPlayer);
                        sender.sendMessage(ChatColor.GREEN + "Jugador más poderoso: " + ChatColor.BOLD + topPlayer.getName() +
                                ChatColor.RESET + " - lvl " + ChatColor.AQUA + topPlayerLevel);
                    } else {
                        sender.sendMessage(ChatColor.RED + "No hay jugadores en línea.");
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador o la consola.");
                }
                return true;

            case "lvlwinv":
                if (args.length == 0) {
                    // Si no se especifica un argumento, muestra el inventario del jugador que ejecuta el comando
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        displayInventory(player);
                    } else {
                        sender.sendMessage(ChatColor.RED + "Este comando solo puede ser ejecutado por un jugador.");
                    }
                } else {
                    // Si se pasa un argumento, intenta mostrar el inventario del jugador especificado
                    String targetName = args[0];
                    Player targetPlayer = getServer().getPlayer(targetName);

                    if (targetPlayer != null) {
                        if (sender instanceof Player) {
                            Player viewer = (Player) sender;
                            viewer.openInventory(targetPlayer.getInventory());
                            sender.sendMessage(ChatColor.GREEN + "Mostrando el inventario de " + ChatColor.AQUA + targetPlayer.getName());
                        } else {
                            sender.sendMessage(ChatColor.RED + "Solo un jugador puede inspeccionar inventarios.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "El jugador " + ChatColor.AQUA + targetName + ChatColor.RED + " no está en línea.");
                    }
                }
                return true;


            case "lvlwreload":
                // Comando para recargar la configuración
                reloadConfig();
                File levelItemsFile = new File(getDataFolder(), "levelitems.yml");
                FileConfiguration levelItemsConfig = YamlConfiguration.loadConfiguration(levelItemsFile);
                itemsConfigManager = new ItemsConfigManager(levelItemsConfig);
                sender.sendMessage(ChatColor.GREEN + "La configuración ha sido recargada.");
                return true;

            case "lvlwmaxlevel":
                // Mostrar el nivel máximo
                int maxLevel = getConfig().getInt("settings.max_level", 25);
                sender.sendMessage(ChatColor.GREEN + "El nivel máximo actual es: " + ChatColor.BOLD + maxLevel);
                return true;

            case "lvlwsetmaxlevel":
                // Cambiar el nivel máximo
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Uso: /lvlwsetmaxlevel <nuevo_nivel>");
                    return true;
                }
                try {
                    int newMaxLevel = Integer.parseInt(args[0]);
                    getConfig().set("settings.max_level", newMaxLevel);
                    saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "¡Nivel máximo actualizado a: " + ChatColor.BOLD + newMaxLevel + "!");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Por favor ingresa un número válido.");
                }
                return true;

            case "lvlwadd":
                // Añadir un ítem con valor
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /lvlwadd <item> <valor>");
                    return true;
                }
                try {
                    Material material = Material.valueOf(args[0].toUpperCase());
                    int value = Integer.parseInt(args[1]);
                    itemsConfigManager.addItem(material, value);
                    sender.sendMessage(ChatColor.GREEN + "¡Ítem " + material.name() + " añadido con valor " + value + "!");
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "El ítem o valor proporcionado no es válido.");
                }
                return true;

            case "lvlwedit":
                // Editar un ítem existente
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "Uso: /lvlwedit <item> <nuevo_valor>");
                    return true;
                }
                try {
                    Material material = Material.valueOf(args[0].toUpperCase());
                    int value = Integer.parseInt(args[1]);
                    if (itemsConfigManager.updateItem(material, value)) {
                        sender.sendMessage(ChatColor.GREEN + "¡Ítem " + material.name() + " actualizado a valor " + value + "!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "El ítem no existe en la configuración.");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "El ítem o valor proporcionado no es válido.");
                }
                return true;

            case "lvlwdel":
                // Eliminar un ítem
                if (args.length < 1) {
                    sender.sendMessage(ChatColor.RED + "Uso: /lvlwdel <item>");
                    return true;
                }
                try {
                    Material material = Material.valueOf(args[0].toUpperCase());
                    if (itemsConfigManager.removeItem(material)) {
                        sender.sendMessage(ChatColor.GREEN + "¡Ítem " + material.name() + " eliminado!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "El ítem no existe en la configuración.");
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "El ítem proporcionado no es válido.");
                }
                return true;

            default:
                return false;
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            int powerLevel = (int) calculatePowerLevel(player);
            updatePlayerPowerDisplay(player, powerLevel);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        int powerLevel = (int) calculatePowerLevel(player);
        updatePlayerPowerDisplay(player, powerLevel);
    }
    private void displayInventory(Player player) {
        ItemStack[] inventoryContents = player.getInventory().getContents();

        // Verificar si el inventario está vacío
        if (inventoryContents == null || inventoryContents.length == 0) {
            player.sendMessage(ChatColor.RED + "Tu inventario está vacío.");
            return;
        }

        // Imprimir los ítems en el chat
        player.sendMessage(ChatColor.GREEN + "Inventario de " + player.getName() + ":");
        for (ItemStack item : inventoryContents) {
            if (item != null && item.getType() != Material.AIR) {
                String itemName = item.getType().toString().replace("_", " ").toLowerCase();
                int amount = item.getAmount();
                // Mostrar el ítem y su cantidad
                player.sendMessage(ChatColor.YELLOW + itemName + " x" + amount);
            }
        }
    }

    private int calculatePowerLevel(Player player) {
        int totalPower = 0;

        // Iterar sobre los ítems del inventario
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null) {
                Material material = item.getType();
                int basePoints = itemsConfigManager.getItemPoints(material); // Obtener puntos base
                int enchantmentCount = item.getEnchantments().size(); // Obtener cantidad de encantamientos

                // Aplicar multiplicador por encantamientos
                int itemPower = basePoints * (1 + enchantmentCount);
                totalPower += itemPower;
            }
        }

        return totalPower;
    }

    public int getPlayerLevel(Player player) {
        // Calcula y devuelve el nivel del jugador
        return (int) calculatePowerLevel(player);
    }
    private Player getTopPlayer() {
        Player topPlayer = null;
        int highestLevel = 0;

        // Iterar sobre todos los jugadores en línea
        for (Player player : getServer().getOnlinePlayers()) {
            int playerLevel = (int) calculatePowerLevel(player);
            if (playerLevel > highestLevel) {
                highestLevel = playerLevel;
                topPlayer = player;
            }
        }
        return topPlayer;
    }
    private void updatePlayerPowerDisplay(Player player, int powerLevel) {
        // Obtener el nivel máximo de la configuración
        int maxLevel = getConfig().getInt("settings.max_level", 25);
        if (powerLevel > maxLevel) {
            // Ejecutar el comando /say desde la consola
            String message = ChatColor.DARK_RED + "" + ChatColor.BOLD + player.getName() +
                    " ha rebasado el nivel máximo, ahora es el enemigo número 1 de la nación!";
            getServer().dispatchCommand(getServer().getConsoleSender(), "say " + ChatColor.stripColor(message));
        }

        // Calcular el porcentaje del nivel
        double levelPercentage = (double) powerLevel / maxLevel * 100;

        // Determinar el color basado en el porcentaje
        ChatColor levelColor;
        if (levelPercentage <= 50) {
            levelColor = ChatColor.WHITE; // 50% o menos: blanco
        } else if (levelPercentage <= 100) {
            levelColor = ChatColor.YELLOW; // Entre 50% y 100%: amarillo
        } else {
            levelColor = ChatColor.DARK_RED; // 100% o más: rojo oscuro
        }

        // Crear el prefijo de nivel con el color correspondiente
        String levelPrefix = levelColor + "" + ChatColor.BOLD + "[Lvl " + powerLevel + "]" + ChatColor.WHITE + " " + ChatColor.BOLD;

        player.setDisplayName(levelPrefix + player.getName());
        player.setPlayerListName(levelPrefix + player.getName());
    }
}
