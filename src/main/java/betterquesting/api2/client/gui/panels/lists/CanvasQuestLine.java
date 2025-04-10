package betterquesting.api2.client.gui.panels.lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Maps;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.enums.EnumQuestState;
import betterquesting.api.properties.NativeProps;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuest.RequirementType;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.storage.BQ_Settings;
import betterquesting.api2.cache.QuestCache;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.IGuiRect;
import betterquesting.api2.client.gui.panels.content.PanelGeneric;
import betterquesting.api2.client.gui.panels.content.PanelLine;
import betterquesting.api2.client.gui.panels.content.PanelLine.ShouldDrawPredicate;
import betterquesting.api2.client.gui.resources.colors.GuiColorPulse;
import betterquesting.api2.client.gui.resources.colors.IGuiColor;
import betterquesting.api2.client.gui.resources.lines.IGuiLine;
import betterquesting.api2.client.gui.resources.textures.SimpleTexture;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetLine;

/**
 * My class for lazy quest line setup on a scrolling canvas
 */
public class CanvasQuestLine extends CanvasScrolling {

    private final List<PanelButtonQuest> btnList = new ArrayList<>();

    private final int buttonId;
    private IQuestLine lastQL;
    private int zoomToFitMargin = 24;

    public CanvasQuestLine(IGuiRect rect, int buttonId) {
        super(rect);
        this.setupAdvanceScroll(true, true, 3000);
        this.enableBlocking(false);
        this.buttonId = buttonId;
    }

    public Collection<PanelButtonQuest> getQuestButtons() {
        return Collections.unmodifiableCollection(this.btnList);
    }

    public PanelButtonQuest getButtonAt(int mx, int my) {
        float zs = zoomScale.readValue();
        int tx = getTransform().getX();
        int ty = getTransform().getY();
        int smx = (int) ((mx - tx) / zs) + lsx;
        int smy = (int) ((my - ty) / zs) + lsy;

        for (PanelButtonQuest btn : btnList) {
            if (btn.rect.contains(smx, smy)) return btn;
        }

        return null;
    }

    public IQuestLine getQuestLine() {
        return lastQL;
    }

    public void refreshQuestLine() {
        setQuestLine(lastQL);
    }

    /**
     * Loads in quests and connecting lines
     * 
     * @param line The quest line to load
     */
    public void setQuestLine(IQuestLine line) {
        // Rest contents
        this.resetCanvas();
        this.btnList.clear();
        lastQL = line;

        if (line == null) {
            return;
        }

        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        UUID pid = QuestingAPI.getQuestingUUID(player);

        String bgString = line.getProperty(NativeProps.BG_IMAGE);

        if (!StringUtils.isNullOrEmpty(bgString)) {
            int bgSize = line.getProperty(NativeProps.BG_SIZE);
            this.addPanel(
                new PanelGeneric(
                    new GuiRectangle(0, 0, bgSize, bgSize, 1),
                    new SimpleTexture(new ResourceLocation(bgString), new GuiRectangle(0, 0, 256, 256))));
        }

        HashMap<UUID, PanelButtonQuest> questBtns = new HashMap<>();

        for (Map.Entry<UUID, IQuestLineEntry> qle : line.entrySet()) {
            IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB)
                .get(qle.getKey());

            if (!QuestCache.isQuestShown(quest, pid, player)) continue;

            GuiRectangle rect = new GuiRectangle(
                qle.getValue()
                    .getPosX(),
                qle.getValue()
                    .getPosY(),
                qle.getValue()
                    .getSizeX(),
                qle.getValue()
                    .getSizeY());
            PanelButtonQuest paBtn = new PanelButtonQuest(rect, buttonId, "", Maps.immutableEntry(qle.getKey(), quest));

            this.addPanel(paBtn);
            this.btnList.add(paBtn);
            questBtns.put(qle.getKey(), paBtn);
        }

        for (Map.Entry<UUID, PanelButtonQuest> entry : questBtns.entrySet()) {
            Map.Entry<UUID, IQuest> quest = entry.getValue()
                .getStoredValue();

            Map<UUID, IQuest> reqMap = QuestingAPI.getAPI(ApiReference.QUEST_DB)
                .filterKeys(
                    quest.getValue()
                        .getRequirements());

            if (reqMap.isEmpty()) {
                continue;
            }

            boolean main = quest.getValue()
                .getProperty(NativeProps.MAIN);
            EnumQuestState qState = quest.getValue()
                .getState(player);
            IGuiLine lineRender = null;
            IGuiColor defaultTxLineCol = null;

            switch (qState) {
                case LOCKED:
                    lineRender = PresetLine.QUEST_LOCKED.getLine();
                    defaultTxLineCol = PresetColor.QUEST_LINE_LOCKED.getColor();
                    break;
                case UNLOCKED:
                    lineRender = PresetLine.QUEST_UNLOCKED.getLine();
                    defaultTxLineCol = PresetColor.QUEST_LINE_UNLOCKED.getColor();
                    break;
                case UNCLAIMED:
                    lineRender = PresetLine.QUEST_PENDING.getLine();
                    defaultTxLineCol = PresetColor.QUEST_LINE_PENDING.getColor();
                    break;
                case COMPLETED:
                    lineRender = PresetLine.QUEST_COMPLETE.getLine();
                    defaultTxLineCol = PresetColor.QUEST_LINE_COMPLETE.getColor();
                    break;
                case REPEATABLE:
                    lineRender = PresetLine.QUEST_REPEATABLE.getLine();
                    defaultTxLineCol = PresetColor.QUEST_LINE_REPEATABLE.getColor();
                    break;
            }

            for (Map.Entry<UUID, IQuest> req : reqMap.entrySet()) {
                PanelButtonQuest parBtn = questBtns.get(req.getKey());
                IGuiColor txLineCol = defaultTxLineCol;

                if (parBtn != null) {
                    RequirementType type = quest.getValue()
                        .getRequirementType(req.getKey());
                    ShouldDrawPredicate predicate = null;
                    switch (type) {
                        case NORMAL:
                            break;
                        case IMPLICIT:
                            if (BQ_Settings.alwaysDrawImplicit) break;
                            predicate = (mx, my, partialTicks) -> questBtns.get(req.getKey()).rect.contains(mx, my)
                                || questBtns.get(quest.getKey()).rect.contains(mx, my)
                                || Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                                || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
                            txLineCol = new GuiColorPulse(
                                txLineCol,
                                PresetColor.QUEST_LINE_IMPLICIT_MIXIN.getColor(),
                                2F,
                                0F);
                            break;
                        default:
                            // bail early
                            continue;
                    }
                    this.addPanel(
                        new PanelLine(
                            parBtn.getTransform(),
                            entry.getValue()
                                .getTransform(),
                            lineRender,
                            main ? 8 : 4,
                            txLineCol,
                            1,
                            predicate));
                }
            }
        }

        fitToWindow();
    }

    public void fitToWindow() {
        // Used later to center focus the quest line within the window
        boolean flag = false;
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;

        for (PanelButtonQuest btn : btnList) {
            GuiRectangle rect = btn.rect;

            if (!flag) {
                minX = rect.getX();
                minY = rect.getY();
                maxX = minX + rect.getWidth();
                maxY = minY + rect.getHeight();
                flag = true;
            } else {
                minX = Math.min(minX, rect.getX());
                minY = Math.min(minY, rect.getY());
                maxX = Math.max(maxX, rect.getX() + rect.getWidth());
                maxY = Math.max(maxY, rect.getY() + rect.getHeight());
            }
        }

        minX -= zoomToFitMargin;
        minY -= zoomToFitMargin;
        maxX += zoomToFitMargin;
        maxY += zoomToFitMargin;

        this.setZoom(
            Math.min(
                getTransform().getWidth() / (float) (maxX - minX),
                getTransform().getHeight() / (float) (maxY - minY)));
        this.refreshScrollBounds();

        IGuiRect bounds = getScrollBounds();
        this.setScrollX(bounds.getX() + bounds.getWidth() / 2);
        this.setScrollY(bounds.getY() + bounds.getHeight() / 2);
        this.updatePanelScroll();
    }

    public void centerOn(PanelButtonQuest btn) {
        int x = btn.rect.x;
        int y = btn.rect.y;
        int width = btn.rect.w;
        int height = btn.rect.h;
        int btnCenterX = x + width / 2;
        int btnCenterY = y + height / 2;

        this.setScrollX(btnCenterX - scrollWindow.w / 2);
        this.setScrollY(btnCenterY - scrollWindow.h / 2);
    }
}
