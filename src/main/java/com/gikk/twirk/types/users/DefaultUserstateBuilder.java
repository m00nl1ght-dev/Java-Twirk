package com.gikk.twirk.types.users;

import com.gikk.twirk.types.AbstractTwitchUserFields;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;

class DefaultUserstateBuilder extends AbstractTwitchUserFields implements UserstateBuilder {

	final String botOwner;

	DefaultUserstateBuilder() {
		this.botOwner = "";
	}

	DefaultUserstateBuilder(String botOwner) {
		this.botOwner = botOwner;
	}

	@Override
	public Userstate build(TwitchMessage message) {
		parseUserProperties(message);
		return new UserstateImpl(this);
	}

	@Override
	protected boolean isOwner(String userName) {
		return !botOwner.isEmpty() && botOwner.equalsIgnoreCase(userName);
	}

}
