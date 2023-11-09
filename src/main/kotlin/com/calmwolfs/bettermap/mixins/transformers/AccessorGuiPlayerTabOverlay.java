package com.calmwolfs.bettermap.mixins.transformers;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiPlayerTabOverlay.class)
public interface AccessorGuiPlayerTabOverlay {
    @Accessor("footer")
    IChatComponent bettermap_getFooter();

    @Accessor("header")
    IChatComponent bettermap_getHeader();
}