package fr.alexdoru.megawallsenhancementsmod.gui.guiscreens;

import fr.alexdoru.megawallsenhancementsmod.config.ConfigHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public abstract class MyGuiScreen extends GuiScreen {

    private static final ResourceLocation SHADER = new ResourceLocation("fkcounter", "shaders/blur.json");
    protected final int ButtonsHeight = 20;
    protected GuiScreen parent = null;
    private int usersGuiScale;

    @Override
    public void initGui() {
        usersGuiScale = mc.gameSettings.guiScale;
        mc.gameSettings.guiScale = 2;
        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
        this.width = scaledresolution.getScaledWidth();
        this.height = scaledresolution.getScaledHeight();
        try {
            mc.entityRenderer.loadShader(SHADER);
        } catch (Exception ignored) {}
        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        mc.gameSettings.guiScale = usersGuiScale;
        ConfigHandler.saveConfig();
        try {
            mc.entityRenderer.stopUseShader();
        } catch (Exception ignored) {}
        super.onGuiClosed();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public String getSuffix(boolean enabled) {
        return enabled ? EnumChatFormatting.GREEN + "Enabled" : EnumChatFormatting.RED + "Disabled";
    }

    public void drawCenteredTitle(String title, int dilatation, float xPos, float yPos, int color) {
        GlStateManager.pushMatrix();
        {
            GlStateManager.translate(xPos - (mc.fontRendererObj.getStringWidth(title) * dilatation) / 2.0f, yPos, 0);
            GlStateManager.scale(dilatation, dilatation, dilatation);
            mc.fontRendererObj.drawStringWithShadow(title, 0, 0, color);
        }
        GlStateManager.popMatrix();
    }

    /**
     * Call this at the end of the drawScreen method to draw the tooltips defined in getTooltopText
     */
    public void drawTooltips(int mouseX, int mouseY) {
        for (final GuiButton button : this.buttonList) {
            if (button.isMouseOver()) {
                drawHoveringText(getTooltipText(button.id), mouseX, mouseY);
                return;
            }
        }
    }

    @Deprecated
    public int getYposForButton(int relativePosition) {
        return getyCenter() - ButtonsHeight / 2 + (ButtonsHeight + 4) * relativePosition;
    }

    protected int getButtonYPos(int i) {
        return this.height / 8 + (ButtonsHeight + 4) * (i + 1);
    }

    /**
     * Override this method and make a switch with all the buttons tooltips
     */
    public List<String> getTooltipText(int id) {
        return new ArrayList<>();
    }

    public int getxCenter() {
        return this.width / 2;
    }

    public int getyCenter() {
        return this.height / 2;
    }

}
