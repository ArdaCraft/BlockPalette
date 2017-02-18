package me.dags.blockpalette.gui;

import me.dags.blockpalette.palette.PaletteItem;
import me.dags.blockpalette.palette.PaletteMain;
import me.dags.blockpalette.util.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

/**
 * @author dags <dags@dags.me>
 */
public class Hotbar {

    protected static final ResourceLocation WIDGETS_TEX_PATH = new ResourceLocation("textures/gui/widgets.png");

    private final PaletteMain main;
    private final FontRenderer fontRenderer;
    private final Slot[] slots = new Slot[9];

    private PaletteItem selected = PaletteItem.EMPTY;
    private int left = 0, top = 0;

    public Hotbar(PaletteMain main) {
        this.main = main;
        this.fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        initSlots(new ScaledResolution(Minecraft.getMinecraft()));
    }

    public void initSlots(ScaledResolution resolution) {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        int slotWidth = 20, slotHeight = 20;
        int hotbarLength = (9 * slotWidth) + 2;
        int hCenter = resolution.getScaledWidth() / 2;

        left = hCenter - (hotbarLength / 2);
        top = resolution.getScaledHeight() - slotHeight - 2;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            slots[i] = new Slot(PaletteItem.of(stack));
            slots[i].setPosition(1 + left + (i * slotWidth), 1 + top);
        }
    }


    public void draw(int mouseX, int mouseY) {
        // hotbar
        GlStateManager.color(1F, 1F, 1F, 1F);
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.enableRescaleNormal();
        Minecraft.getMinecraft().getTextureManager().bindTexture(WIDGETS_TEX_PATH);
        net.minecraft.client.gui.Gui.drawModalRectWithCustomSizedTexture(left, top, 0, 0, 182, 22, 256, 256);

        // items
        RenderHelper.enableGUIStandardItemLighting();

        // draw slot items
        for (int i = 0; i < 9; i++) {
            Slot slot = slots[i];
            if (!slot.isEmpty()) {
                int x = slot.xPos() + 2;
                int y = slot.yPos() + 2;
                ItemStack stack = slot.getStack();
                Slot.drawStack(stack, x, y);
                Minecraft.getMinecraft().getRenderItem().renderItemOverlays(fontRenderer, stack, x, y);
            }
        }

        if (!selected.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(mouseX, mouseY, 0F);
            GlStateManager.translate(0 ,0, 500F);
            ItemStack stack = selected.getItemStack();
            Slot.drawHighlightedStack(stack, 0, 0, Config.highlight_scale, 0xFFFFFF);
            Minecraft.getMinecraft().getRenderItem().renderItemOverlays(fontRenderer, stack, -8, -8);
            GlStateManager.popMatrix();
        }

        GlStateManager.disableDepth();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    public void mouseRelease(int mouseX, int mouseY, int button) {
        if (button == 1) {
            return;
        }

        Slot underMouse = main.getPalette().getUnderMouse();
        boolean shift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT);

        if (underMouse == null) {
            for (int i = 0; i < 9; i++) {
                Slot slot = slots[i];

                if (mouseX >= slot.xPos() && mouseX <= slot.xPos() + 20 && mouseY >= slot.yPos() && mouseY <= slot.yPos() + 20) {
                    if (shift) {
                        slots[i] = new Slot(PaletteItem.EMPTY);
                        slots[i].setPosition(slot.xPos(), slot.yPos());
                        return;
                    } else {
                        PaletteItem item = slot.getItem();
                        slots[i] = new Slot(selected);
                        slots[i].setPosition(slot.xPos(), slot.yPos());
                        selected = item;
                        return;
                    }
                }
            }
            selected = PaletteItem.EMPTY;
        } else {
            selected = underMouse.getItem();
        }
    }

    public void close() {
        EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;

        if (!player.capabilities.isCreativeMode) {
            return;
        }

        // Update slots
        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = slots[i].getStack();
            player.inventory.setInventorySlotContents(i, stack);
        }
    }


    public void keyTyped(char c, int keyCode) {
        Slot hovered = main.getPalette().getUnderMouse();
        if (hovered != null) {
            // char typed 1 to 9 -> translate to int value and subtract 1 so in range 0 to 8
            int id = (c - '0') - 1;
            if (id >= 0 && id < slots.length) {
                slots[id] = new Slot(hovered.getItem());
            }
        }
    }
}
