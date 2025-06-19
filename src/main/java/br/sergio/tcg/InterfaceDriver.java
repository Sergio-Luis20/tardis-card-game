package br.sergio.tcg;

import br.sergio.tcg.game.GameSession;
import br.sergio.tcg.model.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface InterfaceDriver {

    /**
     * Sends some options to the player, from which it can select whichever it wants.
     *
     * @param player the player.
     * @param prompt the prompt.
     * @param options the options to select.
     * @param type the type class of the options.
     * @param allowEmpty true if the player is allowed to not select any option at all.
     * @return the selected options; can be empty depending on <code>allowEmpty</code>.
     * @param <T> the type of the options.
     */
    <T> CompletableFuture<List<T>> chooseMany(Player player, String prompt, List<T> options, Class<T> type, boolean allowEmpty);

    /**
     * Sends some options to the player, from which it must choose one.
     *
     * @param player the player.
     * @param prompt the prompt.
     * @param options the options to choose one.
     * @param type the type class of the options.
     * @param allowNull true if the player is allowed to not select any option at all.
     * @return the selected option; can be null depending on <code>allowNull</code>.
     * @param <T> the type of the options.
     */
    <T> CompletableFuture<T> chooseOne(Player player, String prompt, List<T> options, Class<T> type, boolean allowNull);

    /**
     * Asks something to a player. The answer is never null.
     *
     * @param player the player.
     * @param prompt the prompt.
     * @return <code>true</code> if the player accepted, <code>false</code> otherwise.
     */
    CompletableFuture<Boolean> confirm(Player player, String prompt);

    /**
     * Asks the player to input some number. This method requires a parser that
     * converts the string representation of the number to the number type T. You
     * can throw a NumberFormatException in the parser if you want; that will send the
     * exception message to the player indicating that the string it passed is invalid
     * and ask it to try again. If you throw any other exception, then you should catch
     * it by yourself in the returning CompletableFuture; no messages will be sent to
     * the player in this case.
     *
     * @param player the player.
     * @param prompt the prompt.
     * @param parser the number parser.
     * @return the number.
     * @param <T> the type of the number.
     */
    <T extends Number> CompletableFuture<T> inputNumber(Player player, String prompt, Function<String, T> parser);

    /**
     * Asks the player to input some text. The returned text can be empty, but never null.
     *
     * @param player the player.
     * @param prompt the prompt.
     * @return the text typed by the player.
     */
    CompletableFuture<String> inputText(Player player, String prompt);

    /**
     * Sends a message to a player.
     *
     * @param player the player.
     * @param message the message.
     * @return null when completed.
     */
    CompletableFuture<Void> sendMessage(Player player, String message);

    /**
     * Broadcasts a message to all players.
     *
     * @param message the message.
     * @return null when completed.
     */
    CompletableFuture<Void> sendMessage(GameSession session, String message);

    GameSession createSession(Player host, UUID id);

}
