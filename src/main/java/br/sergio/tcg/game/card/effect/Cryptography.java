package br.sergio.tcg.game.card.effect;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.game.TurnDetails;
import br.sergio.tcg.game.card.EffectCard;
import br.sergio.tcg.game.card.Rarity;
import br.sergio.tcg.game.card.SecretCard;
import br.sergio.tcg.game.card.family.RevelationCard;
import br.sergio.tcg.game.event.events.ShowCardEvent;

import java.util.concurrent.CompletableFuture;

public class Cryptography extends EffectCard implements RevelationCard, SecretCard {

    public Cryptography() {
        super("Criptografia", Rarity.UNCOMMON, "Se alguma carta de revelação for jogada " +
                "em campo, você pode secretamente jogar esta carta e ocultar a verdadeira face da " +
                "carta.", getImage("criptografia.png"));
    }

    @Override
    public CompletableFuture<Void> action(TurnDetails turn) {
        turn.getSession().listen(ShowCardEvent.class, (registration, event) -> {
            var card = event.getCard();
            if (card instanceof RevelationCard) {
                var newFace = DiscordService.getInstance().getEmbedFactory().createCardEmbed(card);
                event.setCardEmbed(newFace);
                registration.cancel();
            }
        });
        sendLog(turn.getSession(), turn.getPlayer());
        return completedAction();
    }

}
