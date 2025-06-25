package br.sergio.tcg;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.discord.slash.SlashCommandListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class Main {

    public static final ExecutorService VIRTUAL;

    public static void main(String[] args) {
        if (!DiscordService.init()) {
            return;
        }

        var service = DiscordService.getInstance();
        var listener = createSlashCommandListener();
        service.registerSlashCommands(listener.getCommands().values(), true);
        service.registerListener(listener);

        log.info("Tardis Card Game online!");

        // Antes de fazer o boot, é necessário primeiro entender
        // como será o fluxo do jogo, isto é, como uma partida
        // é criada, como ela roda, como ela termina e como esse
        // ciclo se repete.
        // Tendo isso feito, só então o boot e o shutdown são planejados,
        // pois eles devem se adaptar a essa lógica.
    }

    private static SlashCommandListener createSlashCommandListener() {
        var commands = new ArrayList<SlashCommand>();
        ServiceLoader.load(SlashCommand.class).forEach(cmd -> {
            commands.add(cmd);
            log.info("Loaded command: {}", cmd.getName());
        });
        return new SlashCommandListener(commands);
    }

    static {
        VIRTUAL = Executors.newVirtualThreadPerTaskExecutor();
        Runtime.getRuntime().addShutdownHook(Thread.ofVirtual().unstarted(() -> {
            VIRTUAL.shutdown();
            try {
                if (!VIRTUAL.awaitTermination(5, TimeUnit.SECONDS)) {
                    VIRTUAL.shutdownNow();
                    log.warn("VIRTUAL main thread executor service timed out on normal shutdown and was forcibly closed.");
                } else {
                    log.info("VIRTUAL main thread executor service closed gracefully.");
                }
            } catch (InterruptedException e) {
                log.error("Interrupted while waiting for the VIRTUAL main thread executor service to shutdown.", e);
            }
        }));
    }

}
