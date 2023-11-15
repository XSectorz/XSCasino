package net.xsapi.panat.xscasino.configuration;

public class configLoader {
    public configLoader() {
        new config().loadConfigu();
        new messages().loadConfigu();
        new lotteryConfig().loadConfigu();
        new tokenConfig().loadConfigu();
    }
}
