package com.gikk.twirk.types.users;

import com.gikk.twirk.enums.USER_LEVEL;


class TwitchUserImpl implements TwitchUser{
	//***********************************************************
	// 				VARIABLES
	//***********************************************************
    private final String userName;
	private final String displayName;
	private final int color;
	private final long userID;
	private final USER_LEVEL userLevel;
	private final String[] badges;
	private final String[] badge_info;

	//***********************************************************
	// 				CONSTRUCTOR
	//***********************************************************

	TwitchUserImpl(DefaultTwitchUserBuilder builder) {
        this.userName    = builder.userName;
		this.displayName = builder.displayName;
		this.badges 	 = builder.badges;
		this.badge_info  = builder.badge_info;
		this.userID 	 = builder.userID;
		this.userLevel 	 = builder.userLevel;
		this.color 	 	 = builder.color;
	}

	//***********************************************************
	// 				PUBLIC
	//***********************************************************

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
	public String getDisplayName(){
		return displayName;
	}

    @Override
    public boolean isOwner() {
        return hasPermission(USER_LEVEL.OWNER);
    }

    @Override
	public boolean isMod(){
		return hasPermission(USER_LEVEL.MOD);
	}

    @Override
	public boolean isTurbo(){
		return hasBadge("turbo");
	}

    @Override
	public boolean isSub(){
		return hasPermission(USER_LEVEL.SUBSCRIBER);
	}

    @Override
	public USER_LEVEL getUserLevel() {
		return userLevel;
	}

	@Override
	public boolean hasPermission(USER_LEVEL level) {
		return userLevel.value >= level.value;
	}

    @Override
	public int getColor(){
		return color;
	}

    @Override
	public String[] getBadges(){
		return badges;
	}

	@Override
	public boolean hasBadge(String id) {
		for (String b : badges) {
			if (b.startsWith(id + "/")) return true;
		}
		return false;
	}

	@Override
	public int getBadge(String id) {
		for (String b : badges) {
			if (b.startsWith(id + "/")) return Integer.parseInt(b.split("/")[1]);
		}
		return -1;
	}

	@Override
	public int getBadgeInfo(String id) {
		for (String b : badge_info) {
			if (b.startsWith(id + "/")) return Integer.parseInt(b.split("/")[1]);
		}
		return -1;
	}

    @Override
	public long getUserID(){
		return userID;
	}
}
