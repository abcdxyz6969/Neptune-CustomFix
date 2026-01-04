package dev.lrxh.neptune.feature.hotbar.impl;

import java.util.List;

public class CustomItem extends Item {

    private final List<String> commands;
    private final List<String> consoleCommands;

    public CustomItem(String displayName, String material, List<String> lore, byte slot,
                      List<String> commands, List<String> consoleCommands, int customModelData) {
        super((ItemAction) null, displayName, material, lore, true, slot, customModelData);
        this.commands = commands;
        this.consoleCommands = consoleCommands;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getConsoleCommands() {
        return consoleCommands;
    }

    public String getCommand() {
        if (commands == null || commands.isEmpty()) return "none";
        return commands.get(0);
    }
}
