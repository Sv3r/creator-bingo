package be.sv3r.creatorbingo.transcripts;

import dev.simplix.protocolize.data.ItemType;
import lombok.Getter;

@Getter
public enum ServerState {
    WAITING(ItemType.BLUE_TERRACOTTA, "§9Klik hier om de game te joinen!"),
    RUNNING(ItemType.RED_TERRACOTTA, "§cKlik hier om de game te spectaten!"),
    DONE(ItemType.ORANGE_TERRACOTTA, "§eDe game is afgelopen!");

    private final ItemType itemType;
    private final String description;

    ServerState(ItemType itemType, String description) {
        this.itemType = itemType;
        this.description = description;
    }

}
