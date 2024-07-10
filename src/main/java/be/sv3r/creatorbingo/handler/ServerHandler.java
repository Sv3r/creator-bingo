package be.sv3r.creatorbingo.handler;

import be.sv3r.creatorbingo.CreatorBingo;
import be.sv3r.creatorbingo.transcripts.Server;
import be.sv3r.creatorbingo.transcripts.ServerState;
import be.sv3r.creatorbingo.util.MessageUtils;
import be.sv3r.creatorbingo.util.RestUtils;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import lombok.Getter;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ServerHandler {
    @Getter
    private static final List<Server> servers = new ArrayList<>();
    @Getter
    private static final List<Server> registeredServers = new ArrayList<>();

    public static void sendPlayerToServer(Server serverTranscript, UUID uuid) {
        ProxyServer proxyServer = CreatorBingo.getServer();
        Optional<Player> player = proxyServer.getPlayer(uuid);
        if (player.isEmpty()) return;

        MessageUtils.sendMessage(player.get(), MessageUtils.getConnectMessage());
        Optional<RegisteredServer> server = proxyServer.getServer(String.valueOf(serverTranscript.getId()));

        proxyServer.getScheduler()
                .buildTask(CreatorBingo.getInstance(), scheduledTask -> {
                    server.ifPresent(registeredServer -> {
                        player.get().createConnectionRequest(registeredServer).connect();

                        Optional<ServerConnection> playerServer = player.get().getCurrentServer();
                        if (playerServer.isPresent()) {
                            if (playerServer.get().getServer().equals(registeredServer)) {
                                scheduledTask.cancel();
                            }
                        }
                    });
                })
                .repeat(Duration.ofSeconds(1L))
                .schedule();
    }

    public static void updateServers() {
        synchronized (servers) {
            List<Server> receivedServers;

            try {
                receivedServers = RestUtils.sendServerGetRequest();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            servers.clear();
            registeredServers.clear();
            servers.addAll(receivedServers);

            for (Server server : servers) {
                ServerState state = server.getState();

                switch (state) {
                    case WAITING, RUNNING -> {
                        if (!registeredServers.contains(server)) {
                            registeredServers.add(server);
                            registerServer(server);
                        }
                    }
                    case DONE -> {
                        unregisterServer(server);
                    }
                }
            }
        }
    }

    public static Server requestServer() {
        Server server;
        try {
            server = RestUtils.sendServerPostRequest();
            if (server != null) {
                registerServer(server);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return server;
    }

    public static void registerServer(Server server) {
        InetAddress address = ipToAddress(server.getIp());
        ServerInfo serverInfo = new ServerInfo(String.valueOf(server.getId()), new InetSocketAddress(address, server.getPort()));
        CreatorBingo.getServer().registerServer(serverInfo);
        if (!registeredServers.contains(server)) {
            registeredServers.add(server);
        }
    }

    public static void unregisterServer(Server server) {
        Optional<RegisteredServer> registeredServer = CreatorBingo.getServer().getServer(String.valueOf(server.getId()));
        registeredServer.ifPresent(deletedServer -> CreatorBingo.getServer().unregisterServer(deletedServer.getServerInfo()));
        registeredServers.remove(server);
    }

    private static InetAddress ipToAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
