package net.xsapi.panat.xscasino.commands;

import net.kyori.adventure.audience.Audience;
import net.xsapi.panat.xscasino.gui.ui_main_lottery;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class XSCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String arg, String[] args) {

        if(commandSender instanceof Player) {

            Player sender = (Player) commandSender;
            if (command.getName().equalsIgnoreCase("xscasino")) {

                if(args.length == 0) {
                    if(!sender.hasPermission("xsapi.xscasino.help")) {
                        XSUtils.sendMessages(sender,"no_permission");
                        return false;
                    }

                    for(String str : XSUtils.messagesList("commands_list")) {
                        XSUtils.sendReplaceComponents(sender,str);
                    }

                    return true;
                } else if(args.length == 1) {
                    if(args[0].equalsIgnoreCase("lottery")) {
                        if(!sender.hasPermission("xsapi.xscasino.lottery")) {

                            XSUtils.sendMessages(sender,"no_permission");
                            return false;
                        }

                        ui_main_lottery.openLotteryGUI(sender);

                        return true;
                    }
                } else if(args.length == 3) {
                    if(args[0].equalsIgnoreCase("lottery")) {
                        if(args[1].equalsIgnoreCase("setLock")) {
                            int data = 0;

                            try {
                                data = Integer.parseInt(args[2]);
                            } catch (NumberFormatException nfe) {
                                XSUtils.sendMessages(sender,"inputNAN");
                                return false;
                            }

                            if(data < 0 || data > 99) {
                                XSUtils.sendMessages(sender,"not_in_range");
                                return false;
                            }

                            if(XSHandlers.getUsingRedis()) {
                                String msg = "LockPrize:" + data + ":" + sender.getName();
                                XSHandlers.sendMessageToRedisAsync("XSCasinoRedisData/XSLottery/Change/"+ XSHandlers.getHostCrossServer() + "/" + XSHandlers.getLocalRedis(),msg);
                            } else {
                                XSHandlers.XSLottery.setLockPrize(data);
                                XSHandlers.XSLottery.setSetterName(sender.getName());
                            }

                            XSUtils.sendMessages(sender,"set_lock_success");
                            return true;
                        }
                    }
                }

                XSUtils.sendMessages(sender,"no_command");
            }
        } else {
            if (command.getName().equalsIgnoreCase("xscasino")) {
                if(args.length == 2) {
                    if(args[0].equalsIgnoreCase("lottery")) {
                        if(Bukkit.getPlayer(args[1].toString()) != null) {
                            Player p = Bukkit.getPlayer(args[1].toString());

                            ui_main_lottery.openLotteryGUI(p);
                        }
                    }
                }
            }
        }

        return false;
    }
}
