package dev.lrxh.neptune.configs.impl;

import dev.lrxh.neptune.utils.ConfigFile;
import dev.lrxh.neptune.Neptune;

public class KitEditorLocationConfig extends ConfigFile {

    public KitEditorLocationConfig() {
        super(Neptune.get(), "kiteditorlocation.yml");
    }
}
