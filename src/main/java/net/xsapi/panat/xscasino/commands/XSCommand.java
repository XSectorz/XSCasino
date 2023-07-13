package net.xsapi.panat.xscasino.commands;

import net.kyori.adventure.audience.Audience;
import net.xsapi.panat.xscasino.gui.ui_main_lottery;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XSCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String arg, String[] args) {

        if(commandSender instanceof Player) {

            Player sender = (Player) commandSender;
            Audience player = (Audience) sender;
            if (command.getName().equalsIgnoreCase("xscasino")) {
                if(args.length == 0) {
                    if(!sender.hasPermission("xsapi.xscasino.help")) {
                        player.sendMessage(XSUtils.messages("no_permission"));
                        return false;
                    }

                    for(String str : XSUtils.messagesList("commands_list")) {
                        player.sendMessage(XSUtils.replaceComponents(str));
                    }

                    return true;
                } else if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("lottery")) {
                        if(!sender.hasPermission("xsapi.xscasino.lottery")) {

                            player.sendMessage(XSUtils.messages("no_permission"));
                            return false;
                        }

                        ui_main_lottery.openLotteryGUI(sender);

                        return true;
                    }
                }
            }
        }

        return false;
    }
}
