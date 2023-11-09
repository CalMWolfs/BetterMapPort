package com.calmwolfs.bettermap.config.features;

import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.annotations.ConfigEditorBoolean;
import io.github.moulberry.moulconfig.annotations.ConfigOption;

public class DevConfig {
    @ConfigOption(name = "Cleanup logs", desc = "Filter out some errors from the logs that are not relevant.")
    @Expose
    @ConfigEditorBoolean
    public boolean filterLogs = false;
}
