package com.calmwolfs.bettermap.config;

import com.calmwolfs.BetterMapMod;
import com.calmwolfs.bettermap.config.features.AboutConfig;
import com.google.gson.annotations.Expose;
import io.github.moulberry.moulconfig.Config;
import io.github.moulberry.moulconfig.annotations.Category;

public class Features extends Config {

    @Override
    public void saveNow() {
        BetterMapMod.configManager.saveConfig(ConfigFileType.FEATURES, "close-gui");
    }

    @Override
    public String getTitle() {
        return "BetterMap " + BetterMapMod.getVersion() + " by §6CalMWolfs§r, §6Soopy§r, §6Tenios§r and §6Bloom§r, config by §5Moulberry §rand §5nea89";
    }

    @Expose
    @Category(name = "About", desc = "Settings for the mod that don't relate to any features")
    public AboutConfig about = new AboutConfig();

    @Expose
    public Storage storage = new Storage();
}
