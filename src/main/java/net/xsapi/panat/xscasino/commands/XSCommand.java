package net.xsapi.panat.xscasino.commands;

import net.xsapi.panat.xscasino.gui.ui_module_roulette;
import net.xsapi.panat.xscasino.modules.token;
import net.xsapi.panat.xscasino.types.RouletteType;
import net.xsapi.panat.xscasino.types.TokenType;
import net.xsapi.panat.xscasino.gui.ui_main_lottery;
import net.xsapi.panat.xscasino.gui.ui_main_token;
import net.xsapi.panat.xscasino.handlers.XSHandlers;
import net.xsapi.panat.xscasino.handlers.XSUtils;
import net.xsapi.panat.xscasino.user.XSUser;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

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
                    } else if(args[0].equalsIgnoreCase("token")) {

                        if(!sender.hasPermission("xsapi.xscasino.token")) {

                            XSUtils.sendMessages(sender,"no_permission");
                            return false;
                        }

                        ui_main_token.openTokenMenu(sender);
                        return true;
                    } else if(args[0].equalsIgnoreCase("help")) {

                        if(!sender.hasPermission("xsapi.xscasino.help")) {
                            XSUtils.sendMessages(sender,"no_permission");
                            return false;
                        }
                        for(String str : XSUtils.messagesList("commands_list")) {
                            XSUtils.sendReplaceComponents(sender,str);
                        }
                        return true;
                    } else if(args[0].equalsIgnoreCase("roulette")) {
                        if(!sender.hasPermission("xsapi.xscasino.roulette")) {

                            XSUtils.sendMessages(sender,"no_permission");
                            return false;
                        }
                        XSUser xsUser = XSHandlers.xsCasinoUser.get(sender.getUniqueId());
                        xsUser.setRouletteType(RouletteType.NONE);
                        xsUser.setPredictWinType(RouletteType.NONE);
                        xsUser.setUpdateRouletteUI(false);
                        xsUser.getNewItemInventory().clear();
                        xsUser.getUseToken().clear();
                        ui_module_roulette.openRoulette(sender);
                        return true;
                    }

                } else if(args.length == 3) {
                    if(args[0].equalsIgnoreCase("lottery")) {
                        if(args[1].equalsIgnoreCase("setLock")) {
                            if(!sender.hasPermission("xsapi.xscasino.admin")) {
                                XSUtils.sendMessages(sender,"no_permission");
                                return false;
                            }

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
                    } else if(args[0].equalsIgnoreCase("help")) {
                        if(!sender.hasPermission("xsapi.xscasino.help")) {
                            XSUtils.sendMessages(sender,"no_permission");
                            return false;
                        }

                        for(String str : XSUtils.messagesList("commands_list")) {
                            XSUtils.sendReplaceComponents(sender,str);
                        }

                        return true;
                    } else if(args[0].equalsIgnoreCase("token")) {

                        if(!sender.hasPermission("xsapi.xscasino.admin")) {
                            XSUtils.sendMessages(sender,"no_permission");
                            return false;
                        }

                        if(args[1].equalsIgnoreCase("setItem")) {
                            String type = args[2].toString();

                            ItemStack it = sender.getInventory().getItemInMainHand().clone();

                            String itString = XSUtils.itemStackToBase64(it);

                            if(type.equalsIgnoreCase("100")) {
                                token.getTokenList().put("token_100",itString);
                            } else if(type.equalsIgnoreCase("1000")) {
                                token.getTokenList().put("token_1000",itString);
                            } else if(type.equalsIgnoreCase("10000")) {
                                token.getTokenList().put("token_10000",itString);
                            }


                            XSUtils.sendMessages(sender,"roulette_setItem");
                            /*for(Map.Entry<String,ItemStack> tokensL : token.getTokenList().entrySet()) {
                                Bukkit.broadcastMessage("Key: " + tokensL.getKey() + " val: " + tokensL.getValue().getType());
                            }*/
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
