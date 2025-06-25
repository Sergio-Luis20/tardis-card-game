package br.sergio.tcg.game.card.family;

import br.sergio.tcg.Utils;
import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.game.Player;
import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.LoggerFactory;

public interface ClownGirlCard extends CardFamily {

    FileUpload COIN_GIF = Utils.loadResource("/gifs/coin.gif");

    default void sendCoinGif(Player sender, GameSession session) {
        session.getGameChannel()
                .sendMessage(sender.getBoldName() + " jogou uma moeda!")
                .setFiles(COIN_GIF).queue(null, t -> {
                    var log = LoggerFactory.getLogger(getClass());
                    log.error("Failed to send coin gif", t);
                });
    }

}
