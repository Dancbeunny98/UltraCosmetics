package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.UltraCosmetics;
import be.isach.ultracosmetics.UltraCosmeticsData;
import be.isach.ultracosmetics.command.SubCommand;
import be.isach.ultracosmetics.config.MessageManager;
import be.isach.ultracosmetics.config.SettingsManager;
import be.isach.ultracosmetics.cosmetics.Category;
import be.isach.ultracosmetics.menu.Menus;
import be.isach.ultracosmetics.menu.menus.MenuPets;
import be.isach.ultracosmetics.player.UltraPlayer;
import be.isach.ultracosmetics.util.MathUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.StringJoiner;

/**
 * Menu {@link be.isach.ultracosmetics.command.SubCommand SubCommand}.
 *
 * @author iSach
 * @since 12-21-2015
 */
public class SubCommandMenu extends SubCommand {

    public SubCommandMenu(UltraCosmetics ultraCosmetics) {
        super("menu", "Opens Specified Menu", "<menu> [page]", ultraCosmetics, true);
    }

    @Override
    protected void onExePlayer(Player sender, String[] args) {
        if (!SettingsManager.isAllowedWorld(sender.getWorld())) {
            sender.sendMessage(MessageManager.getMessage("World-Disabled"));
            return;
        }

        UltraPlayer ultraPlayer = ultraCosmetics.getPlayerManager().getUltraPlayer(sender);
        Menus menus = ultraCosmetics.getMenus();
        if (args.length < 2) {
            menus.getMainMenu().open(ultraPlayer);
            return;
        }

        int page = 1;

        if (args.length > 2 && MathUtils.isInteger(args[2])) {
            page = Integer.parseInt(args[2]);
        }

        String s = args[1].toLowerCase();

        if (s.startsWith("ma")) {
            menus.getMainMenu().open(ultraPlayer);
            return;
        } else if (s.startsWith("r") && SettingsManager.getConfig().getBoolean("Pets-Rename.Enabled")) {
            if (SettingsManager.getConfig().getBoolean("Pets-Rename.Permission-Required") && !sender.hasPermission("ultracosmetics.pets.rename")) {
                error(sender, "You don't have permission.");
                return;
            }
            if (ultraPlayer.getCurrentPet() == null) {
                sender.sendMessage(MessageManager.getMessage("Active-Pet-Needed"));
                return;
            }
            ((MenuPets) menus.getCategoryMenu(Category.PETS)).renamePet(ultraPlayer);
            return;
        } else if (s.startsWith("b") && UltraCosmeticsData.get().areTreasureChestsEnabled()) {
            sender.closeInventory();
            ultraCosmetics.getPlayerManager().getUltraPlayer(sender).openKeyPurchaseMenu();
            return;
        }
        Category cat = Category.fromString(s);
        if (cat == null) {
            sendMenuList(sender);
            return;
        }
        if (!cat.isEnabled()) {
            error(sender, "That menu is disabled.");
            return;
        }
        menus.getCategoryMenu(cat).open(ultraPlayer, page);
    }

    @Override
    protected void onExeAnyone(CommandSender sender, String[] args) {
        notAllowed(sender);
    }

    private void sendMenuList(CommandSender sender) {
        error(sender, "Invalid menu, available menus are:");
        StringJoiner menuList = new StringJoiner(", ");
        menuList.add("main");
        if (UltraCosmeticsData.get().areTreasureChestsEnabled()) {
            menuList.add("buykey");
        }
        if (SettingsManager.getConfig().getBoolean("Pets-Rename.Enabled")) {
            menuList.add("renamepet");
        }
        for (Category category : Category.enabled()) {
            menuList.add(category.name().toLowerCase());
        }
        error(sender, menuList.toString());
    }
}
