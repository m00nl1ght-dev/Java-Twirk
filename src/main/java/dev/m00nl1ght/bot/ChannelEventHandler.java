package dev.m00nl1ght.bot;

import com.gikk.twirk.types.usernotice.Usernotice;
import com.gikk.twirk.types.users.TwitchUser;

public interface ChannelEventHandler {

    void onUsernotice(TwitchUser user, Usernotice usernotice);
    
}
