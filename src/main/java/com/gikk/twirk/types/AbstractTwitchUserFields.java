package com.gikk.twirk.types;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.types.twitchMessage.TwitchMessage;
import com.gikk.twirk.types.users.Userstate;

/**Since many types shares these typical User-fields, it is easier to have one class which does all the parsing
 * and then let the respective types Builder classes use it
 *
 * @author Gikkman
 *
 */
public abstract class AbstractTwitchUserFields {
	private static final int[] DEFAULT_COLORS = { 0xFF0000, 0x0000FF, 0x00FF00, 0xB22222, 0xFF7F50,
												  0x9ACD32, 0xFF4500, 0x2E8B57, 0xDAA520, 0xD2691E,
												  0x5F9EA0, 0x1E90FF, 0xFF69B4, 0x8A2BE2, 0x00FF7F };
	private static final String[] EMPTY = new String[0];

	public  String[]  badges;
	public  String[]  badge_info;
    public  int       bits;
    public  String    userName;
	public  String 	  displayName;
	public  int 	  color;
	public  long 	  userID;
	public  int[] 	  emoteSets;
	public  USER_LEVEL userLevel;
	public  Userstate userstate;
	public  String 	  rawLine;

	protected void parseUserProperties(TwitchMessage message){
		//If display-name is empty, it means that the the user name can be read from the IRC message's prefix and
		//that it has it's first character in upper case and the rest of the characters in lower case
		String channelOwner = message.getTarget().substring(1);	//Strip the # from the channel name
		TagMap r = message.getTagMap();

        // The user name is the message's prefix, between the : and the !
        String temp = message.getPrefix();
        String testLogin = r.getAsString(TwitchTags.LOGIN_NAME);
        if(testLogin.isEmpty()) {
            this.userName = temp.contains("!") ? temp.substring(1, temp.indexOf("!") ):"";
        } else {
            this.userName = testLogin;
        }

		temp =  r.getAsString(TwitchTags.DISPLAY_NAME);
		this.displayName = temp.isEmpty()
						   ? Character.toUpperCase( userName.charAt(0) ) + userName.substring(1)
						   : temp;
		temp = r.getAsString(TwitchTags.BADGES);
		this.badges = temp.isEmpty() ? EMPTY : temp.split(",");
		temp = r.getAsString(TwitchTags.BADGE_INFO);
		this.badge_info = temp.isEmpty() ? EMPTY : temp.split(",");

		this.userID = r.getAsLong(TwitchTags.USER_ID);
		this.color  = r.getAsInt(TwitchTags.COLOR);
		this.color = this.color == -1 ? getDefaultColor() : this.color;

		this.emoteSets = parseEmoteSets( r.getAsString(TwitchTags.EMOTE_SET) );
		this.userLevel = parseUserType(displayName.equalsIgnoreCase(channelOwner) || isOwner(displayName));

		this.rawLine = message.getRaw();
	}

	protected abstract boolean isOwner(String userName);

	private int[] parseEmoteSets(String emoteSet) {
		if( emoteSet.isEmpty() ) {
            return new int[0];
        }

		String[] sets = emoteSet.split(",");
		int[] out = new int[ sets.length ];

		for( int i = 0; i < sets.length; i++ ) {
            out[i] = Integer.parseInt( sets[i] );
        }

		return out;
	}

	private USER_LEVEL parseUserType(boolean isOwner) {
		if (isOwner) {
            return USER_LEVEL.OWNER;
        }
		for (String b : badges) {
			if(b.startsWith("moderator/")) return USER_LEVEL.MOD;
			if(b.startsWith("vip/")) return USER_LEVEL.VIP;
		}
		for (String b : badge_info) {
			if(b.startsWith("subscriber/")) {
				int months = Integer.parseInt(b.split("/")[1]);
				if (months >= 24) return USER_LEVEL.SUBSCRIBER_24;
				if (months >= 12) return USER_LEVEL.SUBSCRIBER_12;
				if (months >= 6) return USER_LEVEL.SUBSCRIBER_6;
				if (months >= 3) return USER_LEVEL.SUBSCRIBER_3;
				if (months >= 1) return USER_LEVEL.SUBSCRIBER;
			}
		}
		return USER_LEVEL.DEFAULT;
	}

	private int getDefaultColor(){
		//If display name is empty, just semi-random a color
		if( displayName.isEmpty() ) {
            return DEFAULT_COLORS[ ((int) (System.currentTimeMillis()) % DEFAULT_COLORS.length) ];
        }

		int n = displayName.charAt(0) + displayName.charAt(displayName.length() - 1);
        return DEFAULT_COLORS[n % DEFAULT_COLORS.length];
	}
}
