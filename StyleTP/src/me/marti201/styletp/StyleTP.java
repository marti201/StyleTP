package me.marti201.styletp;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class StyleTP extends JavaPlugin implements CommandExecutor, Listener {

	ArrayList<UUID> off = new ArrayList<UUID>();

	String enableMsg;
	String disableMsg;
	String noPermission;
	boolean configProblem = false;
	Sound sound;
	float soundPitch;
	float soundVolume;
	Effect effect;
	int particlesNumber;

	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		getCommand("styletp").setExecutor(this);

		saveDefaultConfig();

		FileConfiguration conf = getConfig();

		enableMsg = ChatColor.translateAlternateColorCodes('&', conf.getString("message-enable"));
		disableMsg = ChatColor.translateAlternateColorCodes('&', conf.getString("message-disable"));
		noPermission = ChatColor.translateAlternateColorCodes('&', conf.getString("message-no-permission"));
		particlesNumber = conf.getInt("number-of-particles");
		soundPitch = (float) conf.getDouble("sound-pitch");
		soundVolume = (float) conf.getDouble("sound-volume");

		try {
			// Trying to make this as easy to set as possible by replacing
			// spaces with underscores and making everything uppercase
			Effect ef = Effect.valueOf(conf.getString("effect").toUpperCase().replace(" ", "_"));
			Sound so = Sound.valueOf(conf.getString("sound").toUpperCase().replace(" ", "_"));

			sound = so;
			effect = ef;
		} catch (Exception ex) {
			configProblem = true;
			getLogger().warning(ChatColor.RED
					+ "There is a problem with the 'effect' or 'sound' variables in the StyleTP configuration file! The plugin won't work until it's fixed");
		}

	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {

			Player p = (Player) sender;

			if (!p.hasPermission("styletp.use")) {
				p.sendMessage(noPermission);
				return true;
			}

			UUID uuid = p.getUniqueId();

			if (off.contains(uuid)) {
				off.remove(uuid);
				p.sendMessage(enableMsg);
			} else {
				off.add(uuid);
				p.sendMessage(disableMsg);
			}

		} else
			sender.sendMessage("You can't use this command as the console");
		return true;
	}
	
	// Overriding other plugins to see if they cancel the event
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent e) {
		Player p = e.getPlayer();

		if (e.isCancelled() || !p.hasPermission("styletp.use") || off.contains(p.getUniqueId()))
			return; // No need to do anything

		if (configProblem) {
			p.sendMessage(ChatColor.RED
					+ "[StyleTP] There is a problem with the configuration file! Please inform the server administrator");
			return;
		}

		// Play the effects
		playEffect(e.getFrom());

		// The player is still in the process of teleporting
		// so play the destination effect 1 tick later
		Bukkit.getScheduler().runTaskLater(this, () -> playEffect(e.getTo()), 1L);

	}

	public void playEffect(Location loc) {
		// Playing the effects at the center of the player's body
		loc = new Location(loc.getWorld(), loc.getX(), loc.getY() + 1, loc.getZ());

		loc.getWorld().playSound(loc, sound, soundPitch, soundVolume);
		for (int i = 0; i < particlesNumber; i++) {
			loc.getWorld().playEffect(loc, effect, 0, 3);
		}
	}

}
