package be.sv3r.creatorbingo.util;

import be.sv3r.creatorbingo.CreatorBingo;
import com.velocitypowered.api.proxy.Player;
import dev.dejvokep.boostedyaml.route.Route;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class MessageUtils {
    private static final Component prefix;
    @Getter
    private static final Component connectMessage;
    @Getter
    private static final Component fullMessage;

    static {
        prefix = Component.text(CreatorBingo.getConfig().getString(Route.from("prefix")));
        connectMessage = Component.text("Aan het verbinden...", NamedTextColor.GREEN);
        fullMessage = Component.text("Er zijn geen servers meer beschikbaar", NamedTextColor.RED);
    }

    public static void sendMessage(Player player, Component message) {
        player.sendMessage(prefix.append(message));
    }
}
