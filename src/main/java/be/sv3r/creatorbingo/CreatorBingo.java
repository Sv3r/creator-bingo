package be.sv3r.creatorbingo;

import be.sv3r.creatorbingo.handler.GuiHandler;
import be.sv3r.creatorbingo.handler.ServerHandler;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.dvs.versioning.BasicVersioning;
import dev.dejvokep.boostedyaml.route.Route;
import dev.dejvokep.boostedyaml.settings.dumper.DumperSettings;
import dev.dejvokep.boostedyaml.settings.general.GeneralSettings;
import dev.dejvokep.boostedyaml.settings.loader.LoaderSettings;
import dev.dejvokep.boostedyaml.settings.updater.UpdaterSettings;
import lombok.Getter;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Plugin(
        id = "creatorbingo",
        name = "Creator Bingo",
        version = "1.0.0",
        description = "CreatorSMP bingo",
        authors = {"Sv3r"},
        dependencies = {@Dependency(
                id = "protocolize"
        )}
)
public class CreatorBingo {
    private final Logger logger;

    @Getter
    private static CreatorBingo instance;
    @Getter
    private static ProxyServer server;
    @Getter
    private static String prefix;
    @Getter
    private static String apiKey;
    @Getter
    private static String apiUrl;
    @Getter
    private static YamlDocument config;

    private static final MinecraftChannelIdentifier CHANNEL_IDENTIFIER = MinecraftChannelIdentifier.from("bingo:main");

    @Inject
    public CreatorBingo(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        CreatorBingo.instance = this;
        CreatorBingo.server = server;
        this.logger = logger;

        setupConfig(dataDirectory);
        loadConfigValues();

        GuiHandler.initialize();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        server.getChannelRegistrar().register(CHANNEL_IDENTIFIER);
        server.getScheduler().buildTask(this, () -> {
            ServerHandler.updateServers();
            GuiHandler.updateGUI();
        }).repeat(5L, TimeUnit.SECONDS).schedule();
    }

    @Subscribe
    @SuppressWarnings("UnstableApiUsage")
    public void onPluginMessageFromPlugin(PluginMessageEvent event) {
        if (!(event.getSource() instanceof ServerConnection)) return;
        if (event.getIdentifier() != CHANNEL_IDENTIFIER) return;

        ByteArrayDataInput dataInput = ByteStreams.newDataInput(event.getData());
        String subChannel = dataInput.readUTF();
        String uuid = dataInput.readUTF();

        if (subChannel.equals("BingoPlayer")) {
            Optional<Player> player = server.getPlayer(UUID.fromString(uuid));
            player.ifPresent(GuiHandler::showGui);
        }
    }

    private void setupConfig(Path dataDirectory) {
        String filename = "config.yml";

        try {
            config = YamlDocument.create(
                    new File(dataDirectory.toFile(), filename),
                    Objects.requireNonNull(getClass().getResourceAsStream("/" + filename)),
                    GeneralSettings.DEFAULT,
                    LoaderSettings.builder().setAutoUpdate(true).build(),
                    DumperSettings.DEFAULT,
                    UpdaterSettings.builder()
                            .setVersioning(new BasicVersioning("config-version"))
                            .setOptionSorting(UpdaterSettings.OptionSorting.SORT_BY_DEFAULTS).build()
            );

            config.update();
            config.save();
        } catch (IOException e) {
            logger.error("Could not create/load plugin configuration! This plugin will shutdown");
            shutdownPlugin();
        }
    }

    private void loadConfigValues() {
        prefix = config.getString(Route.from("prefix"));
        apiKey = config.getString(Route.from("api-key"));
        apiUrl = config.getString(Route.from("api-url"));
    }

    private void shutdownPlugin() {
        Optional<PluginContainer> container = server.getPluginManager().getPlugin("creatorbingo");
        container.ifPresent(pluginContainer -> pluginContainer.getExecutorService().shutdown());
    }
}