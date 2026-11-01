
package org.minecraftplugins.funnyaddons;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class OneHitSword extends JavaPlugin implements Listener {

    private NamespacedKey swordKey;

    @Override
    public void onEnable() {
        swordKey = new NamespacedKey(this, "one_hit_sword");

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("OneHitSword enabled!");
    }

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {
        if (!command.getName().equalsIgnoreCase("onehitsword")) {
            return false;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command must be used in-game.");
            return true;
        }

        ItemStack sword = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta meta = sword.getItemMeta();

        if (meta == null) {
            player.sendMessage(ChatColor.RED + "Could not create sword!");
            return true;
        }

        meta.setDisplayName(
                ChatColor.DARK_RED.toString() +
                        ChatColor.BOLD + "ONE-HIT SWORD"
        );

        meta.setLore(List.of(
                ChatColor.GRAY + "Every hit is fatal.",
                ChatColor.DARK_RED + "A weapon of immense power"
        ));

        meta.getPersistentDataContainer().set(
                swordKey,
                PersistentDataType.BYTE,
                (byte) 1
        );

        sword.setItemMeta(meta);

        // Add sword to inventory
        var leftovers = player.getInventory().addItem(sword);

        // Drop sword if inventory is full
        for (ItemStack leftover : leftovers.values()) {
            player.getWorld().dropItemNaturally(
                    player.getLocation(),
                    leftover
            );
        }

        player.sendMessage(
                ChatColor.GREEN + "You received the One-Hit Sword!"
        );

        return true;
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player)) {
            return;
        }

        // Check if attack is already cancelled
        if (event.isCancelled()) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (weapon.getType() != Material.NETHERITE_SWORD) {
            return;
        }

        ItemMeta meta = weapon.getItemMeta();

        if (meta == null) {
            return;
        }

        // Check if this is our special sword
        if (!meta.getPersistentDataContainer().has(
                swordKey,
                PersistentDataType.BYTE
        )) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity target)) {
            return;
        }

        // One hit kill!
        target.setHealth(0.0);
    }
}
