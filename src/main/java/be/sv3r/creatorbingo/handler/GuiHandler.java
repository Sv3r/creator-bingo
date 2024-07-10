package be.sv3r.creatorbingo.handler;

import be.sv3r.creatorbingo.CreatorBingo;
import be.sv3r.creatorbingo.transcripts.Server;
import be.sv3r.creatorbingo.transcripts.ServerState;
import be.sv3r.creatorbingo.util.MessageUtils;
import com.velocitypowered.api.proxy.Player;
import dev.simplix.protocolize.api.Protocolize;
import dev.simplix.protocolize.api.chat.ChatElement;
import dev.simplix.protocolize.api.inventory.Inventory;
import dev.simplix.protocolize.api.item.ItemStack;
import dev.simplix.protocolize.api.player.ProtocolizePlayer;
import dev.simplix.protocolize.data.ItemType;
import dev.simplix.protocolize.data.inventory.InventoryType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GuiHandler {
    private static Inventory inventory;

    public static void initialize() {
        inventory = new Inventory(InventoryType.GENERIC_9X4);
        inventory.title(ChatElement.ofLegacyText("§c§lCreatorSMP Bingo"));

        for (int i = 0; i < 9; ++i) {
            inventory.item(i, new ItemStack(ItemType.GRAY_STAINED_GLASS_PANE));
            inventory.item(i + 27, new ItemStack(ItemType.GRAY_STAINED_GLASS_PANE));
        }

        ItemStack item = new ItemStack(ItemType.SLIME_BALL);
        item.displayName(ChatElement.ofLegacyText("§aMaak server aan"));
        inventory.item(4, item);

        inventory.onClick((inventoryClick) -> {
            int index = inventoryClick.slot();
            UUID uuid = inventoryClick.player().uniqueId();

            if (index == 4) {
                Server server = ServerHandler.requestServer();
                if (server != null) {
                    ServerHandler.sendPlayerToServer(server, uuid);
                } else {
                    Optional<Player> player = CreatorBingo.getServer().getPlayer(uuid);
                    player.ifPresent(value -> MessageUtils.sendMessage(value, MessageUtils.getFullMessage()));
                }
            }

            int serverIndex = index - 9;
            if (serverIndex <= ServerHandler.getRegisteredServers().size() - 1 && serverIndex >= 0) {
                Server server;
                server = ServerHandler.getRegisteredServers().get(serverIndex);
                if (!server.getState().equals(ServerState.DONE)) {
                    ServerHandler.sendPlayerToServer(server, uuid);
                }
            }
        });
    }

    public static void updateGUI() {
        List<Server> servers = ServerHandler.getRegisteredServers();
        for (int i = 9; i < 24; i++) {
            inventory.item(i, ItemStack.NO_DATA);
        }

        for (int i = 0; i < servers.size(); i++) {
            Server server = servers.get(i);
            ServerState state = server.getState();

            ItemStack item = new ItemStack(state.getItemType());
            item.displayName(ChatElement.ofLegacyText("Bingo " + (i + 1)));
            item.addToLore(ChatElement.ofLegacyText(state.getDescription()));
            item.addToLore(ChatElement.ofLegacyText("§8Spelers: §7" + server.getPlayers()));
            inventory.item(i + 9, item);
        }
    }

    public static void showGui(Player proxyPlayer) {
        ProtocolizePlayer player = Protocolize.playerProvider().player(proxyPlayer.getUniqueId());
        player.closeInventory();
        player.openInventory(inventory);
    }
}
