package br.sergio.tcg;

import br.sergio.tcg.discord.DiscordService;
import br.sergio.tcg.discord.slash.SlashCommand;
import br.sergio.tcg.discord.slash.SlashCommandListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.ServiceLoader;

@Slf4j
public class Main {

    public static void main(String[] args) {
        if (!DiscordService.init()) {
            return;
        }

        var service = DiscordService.getInstance();
        service.registerListener(createSlashCommandListener());

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

}
