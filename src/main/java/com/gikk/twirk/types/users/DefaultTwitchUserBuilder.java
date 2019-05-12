package com.gikk.twirk.types.users;

import com.gikk.twirk.types.AbstractTwitchUserFields;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;

class DefaultTwitchUserBuilder extends AbstractTwitchUserFields implements TwitchUserBuilder {

	final String botOwner;

	DefaultTwitchUserBuilder() {
		this.botOwner = "";
	}

	DefaultTwitchUserBuilder(String botOwner) {
		this.botOwner = botOwner;
	}

	@Override
	public TwitchUser build(TwitchMessage message) {
		parseUserProperties(message);
		return new TwitchUserImpl( this );
	}

	@Override
	protected boolean isOwner(String userName) {
		return !botOwner.isEmpty() && botOwner.equalsIgnoreCase(userName);
	}

}
