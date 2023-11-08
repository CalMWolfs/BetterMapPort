package com.calmwolfs.bettermap.config.gui.buttonthing;

import net.minecraft.client.gui.Gui;

public abstract class GuiElement extends Gui {
    public abstract void render(int x, int y);

    public abstract int getWidth();

    public abstract int getHeight();
}