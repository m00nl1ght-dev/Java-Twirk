package dev.m00nl1ght.bot.twist;

import com.gikk.twirk.types.usernotice.Usernotice;
import com.gikk.twirk.types.usernotice.subtype.Raid;
import com.gikk.twirk.types.usernotice.subtype.Subscription;
import com.gikk.twirk.types.users.TwitchUser;
import dev.m00nl1ght.bot.ChannelEventHandler;
import dev.m00nl1ght.bot.MainListener;
import dev.m00nl1ght.bot.listener.MsgListenerTypes;
import dev.m00nl1ght.bot.twist.dbd.DbdCommand;

public class TwistExtension implements ChannelEventHandler {

    public static TwistExtension INSTANCE;

    protected final MainListener core;
    protected String resubMsg = "";
    protected int moduloResub = 0;
    protected String raidMsg = "";

    public TwistExtension(MainListener core) {this.core = core;}

    public static void register(MainListener core) {
        INSTANCE = new TwistExtension(core);
        core.commandManager.registerType(RangAlias.TYPE);
        core.commandManager.registerType(ExtensionCommand.TYPE);
        core.commandManager.registerType(DbdCommand.TYPE);
        core.commandManager.register(INSTANCE);
        MsgListenerTypes.register(DonoListener.ID, DonoListener::new);
    }

    @Override
    public void onUsernotice(TwitchUser user, Usernotice notice) {
        if (notice.isSubscription() && !resubMsg.isEmpty()) {
            Subscription sub = notice.getSubscription().get();
            int months = sub.getMonths();
            if (moduloResub == 0 || months % moduloResub == 0) {
                core.sendMessage(resubMsg.replaceAll("<u>", user.getDisplayName()));
            }
        } else if (notice.isRaid() && !raidMsg.isEmpty()) {
            Raid r = notice.getRaid().get();
            core.sendMessage(raidMsg.replaceAll("<u>", r.getSourceDisplayName()).replaceAll("<c>", Integer.toString(r.getRaidCount())));
        }
    }

}
