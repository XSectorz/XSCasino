package net.xsapi.panat.xscasino.user;

import org.bukkit.entity.Player;

public class UserData {

    private Player p;
    private int myLotteryPage;

    public UserData(Player p) {
        this.p = p;
        setMyLotteryPage(1);
    }

    public int getMyLotteryPage() {
        return myLotteryPage;
    }

    public void setMyLotteryPage(int myLotteryPage) {
        this.myLotteryPage = myLotteryPage;
    }

}
