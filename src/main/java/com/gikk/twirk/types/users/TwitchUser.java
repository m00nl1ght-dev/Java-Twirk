package com.gikk.twirk.types.users;

import com.gikk.twirk.enums.USER_LEVEL;
import com.gikk.twirk.events.TwirkListener;

/**
 * Class for representing a Twitch User's attributes<br><br>
 * <p>
 * Whenever we receive a PRIVMSG or WHISPER from Twitch (see {@link TwirkListener}, Twitch always sends us some
 * information about the sender, such as what color the user has in chat on Twitch, how the users name should
 * be capitalized, if the user has Turbo, and so on. This class encapsulates all that info, and makes it easy
 * to work with.
 *
 * @author Gikkman
 */
public interface TwitchUser {

    /**
     * Retrieves this user's user name. This is the name the user logs in to
     * Twitch with
     *
     * @return The user's user name
     */
    public String getUserName();

    /**
     * Retrieves this user's display name, as displayed in Twitch chat
     *
     * @return The user's display name
     */
    public String getDisplayName();

    /**
     * Retrieves info whether this user is the owner of this channel or not
     *
     * @return {@code true} if the user is the owner, {@code false} if not
     */
    public boolean isOwner();

    /**
     * Retrieves info whether this user is a mod in this channel or not
     *
     * @return {@code true} if the user is mod, {@code false} if not
     */
    public boolean isMod();

    /**
     * Retrieves info whether this user has turbo or not
     *
     * @return {@code true} if the user has turbo, {@code false} if not
     */
    public boolean isTurbo();

    /**
     * Retrieves info whether this user is a sub to this channel or not
     *
     * @return {@code true} if the user is a sub, {@code false} if not
     */
    public boolean isSub();

    /**
     * Retrieves this users {@link USER_LEVEL} <br>
     *
     * @return The user's USER_LEVEL
     */
    public USER_LEVEL getUserLevel();

    public default boolean hasPermission(USER_LEVEL level) {
        return hasPermission(level.value);
    }

    public boolean hasPermission(int level);

    /**
     * Retrieves this users display color, as seen in Twitch chat.<br>
     * The color is a hexadecimal number.
     *
     * @return The users display color, as a hex number
     */
    public int getColor();

    /**
     * Retrieves the users set of badges in Twitch chat. A badge looks like this: <br>
     * {@code broadcaster/1} <br><br>
     * <p>
     * There are several different badges, such as {@code broadcaster/1}, {@code turbo/1} and so on. I do
     * not know all of them explicitly, or what to do with them.
     * <p>
     * TODO: Find out more about badges
     *
     * @return Arrays of strings, representing this users badges. Might be empty if user has none.
     */
    public String[] getBadges();

    public boolean hasBadge(String id);

    public int getBadge(String id);

    public int getBadgeInfo(String id);

    /**
     * Retrieves this user's unique user ID. This ID is decided by Twitch, and will
     * always be the same for the same user
     *
     * @return The users unique user ID
     */
    public long getUserID();

}
