package dev.m00nl1ght.bot;

import com.gikk.twirk.Twirk;
import com.gikk.twirk.TwirkBuilder;

import java.io.File;
import java.io.IOException;

public class Main {

    private static final String DEFAULT_PROFILE = "bot_testing";
    private static final File PROFILE_DIR = new File("profile");

    public static void main(String[] args) throws IOException, InterruptedException {

        final Profile config = new Profile(new File(PROFILE_DIR, args.length > 0 ? args[0] : DEFAULT_PROFILE));
        config.load();
        //config.save();

        final Twirk bot = new TwirkBuilder("#" + config.CHANNEL, config.USERNAME, config.OAUTH).setBotOwner(config.OWNER).build();
        final MainListener core = new MainListener(bot, config);
        core.load();
        bot.addIrcListener(core);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> core.exit()));
        bot.connect();

    }

}
