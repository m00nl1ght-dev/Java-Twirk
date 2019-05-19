package com.gikk.twirk.enums;

/**
 * Enum for representing different types of users. <br>
 * DEFAULT in this case means that no information was given, and it can be assumed that a user
 * with DEFAULT user type is a normal user, without any special privileges is this channel.<br><br>
 * <p>
 * USER_TYPE comes with a value.<br>
 * These values can be used to make sure that only users of a certain type can do something.<br><br>
 * <p>
 * For example:<br>
 * <pre><code>if( user.USER_TYPE.value >= USER_TYPE.MOD.value )</code>
 * 	<code>doSomething();</code></pre>
 *
 * @author Gikkman, m00nl1ght
 */
public enum USER_LEVEL {

    OWNER(10),
    MOD(8),
    VIP(7),
    SUBSCRIBER_24(6),
    SUBSCRIBER_12(5),
    SUBSCRIBER_6(4),
    SUBSCRIBER_3(3),
    SUBSCRIBER(2),
    DEFAULT(0);

    public final int value;

    private USER_LEVEL(int value) {
        this.value = value;
    }

}