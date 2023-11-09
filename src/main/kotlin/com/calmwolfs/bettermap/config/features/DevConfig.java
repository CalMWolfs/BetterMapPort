package com.calmwolfs.bettermap.config.features;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class DevConfig {
//todo could move elsewhere
    @ConfigOption(name = "Unable to locate sign", desc = "Hide the annoying Unable to locate sign messages.")
    @Expose
    @ConfigEditorBoolean
    public boolean locateSign = true;

    @ConfigOption(name = "Cleanup logs", desc = "Filter out some errors from the logs.")
    @Expose
    @ConfigEditorBoolean
    public boolean filterLogs = false;

}
