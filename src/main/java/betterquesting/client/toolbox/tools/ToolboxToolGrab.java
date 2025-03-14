package betterquesting.client.toolbox.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import betterquesting.api.client.toolbox.IToolboxTool;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.questing.IQuestLineEntry;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.client.gui.controls.PanelButtonQuest;
import betterquesting.api2.client.gui.panels.lists.CanvasQuestLine;
import betterquesting.client.gui2.editors.designer.PanelToolController;
import betterquesting.client.toolbox.ToolboxTabMain;
import betterquesting.network.handlers.NetChapterEdit;
import betterquesting.questing.QuestLineDatabase;

public class ToolboxToolGrab implements IToolboxTool {

    private CanvasQuestLine gui;

    private final List<GrabEntry> grabList = new ArrayList<>();

    @Override
    public void initTool(CanvasQuestLine gui) {
        this.gui = gui;
        grabList.clear();
    }

    @Override
    public void disableTool() {
        for (GrabEntry grab : grabList) {
            IQuestLineEntry qle = gui.getQuestLine()
                .get(
                    grab.btn.getStoredValue()
                        .getKey());

            if (qle != null) {
                grab.btn.rect.x = qle.getPosX();
                grab.btn.rect.y = qle.getPosY();
            }
        }

        grabList.clear();
    }

    @Override
    public void refresh(CanvasQuestLine gui) {
        if (grabList.isEmpty()) {
            return;
        }

        List<GrabEntry> tmp = new ArrayList<>();

        for (GrabEntry grab : grabList) {
            for (PanelButtonQuest btn : PanelToolController.selected) {
                if (btn.getStoredValue()
                    .getKey()
                    .equals(
                        grab.btn.getStoredValue()
                            .getKey())) {
                    tmp.add(new GrabEntry(btn, grab.offX, grab.offY));
                    break;
                }
            }
        }

        grabList.clear();
        grabList.addAll(tmp);
    }

    @Override
    public void drawCanvas(int mx, int my, float partialTick) {
        if (grabList.size() <= 0) return;

        int snap = Math.max(1, ToolboxTabMain.INSTANCE.getSnapValue());
        int dx = mx;
        int dy = my;
        dx = ((dx % snap) + snap) % snap;
        dy = ((dy % snap) + snap) % snap;
        dx = mx - dx;
        dy = my - dy;

        for (GrabEntry grab : grabList) {
            grab.btn.rect.x = dx + grab.offX;
            grab.btn.rect.y = dy + grab.offY;
        }
    }

    @Override
    public void drawOverlay(int mx, int my, float partialTick) {
        ToolboxTabMain.INSTANCE.drawGrid(gui);
    }

    @Override
    public List<String> getTooltip(int mx, int my) {
        if (grabList.size() <= 0) return null;

        for (GrabEntry grab : grabList) {
            if (grab.offX == 0 && grab.offY == 0) {
                List<String> list = new ArrayList<>();
                list.add("X: " + grab.btn.rect.x);
                list.add("Y: " + grab.btn.rect.y);
                return list;
            }
        }

        return Collections.emptyList();
    }

    @Override
    public void onSelection(List<PanelButtonQuest> buttons) {}

    @Override
    public boolean onMouseClick(int mx, int my, int click) {
        if (click == 1 && grabList.size() > 0) // Reset tool
        {
            for (GrabEntry grab : grabList) {
                IQuestLineEntry qle = gui.getQuestLine()
                    .get(
                        grab.btn.getStoredValue()
                            .getKey());

                if (qle != null) {
                    grab.btn.rect.x = qle.getPosX();
                    grab.btn.rect.y = qle.getPosY();
                }
            }

            grabList.clear();
            return true;
        } else if (click != 0 || !gui.getTransform()
            .contains(mx, my)) // Not a click we're listening for
        {
            return false;
        }

        if (grabList.size() > 0) // Apply positioning
        {
            IQuestLine qLine = gui.getQuestLine();
            UUID lID = QuestLineDatabase.INSTANCE.lookupKey(qLine);
            for (GrabEntry grab : grabList) {
                IQuestLineEntry qle = gui.getQuestLine()
                    .get(
                        grab.btn.getStoredValue()
                            .getKey());
                if (qle != null) {
                    qle.setPosition(grab.btn.rect.x, grab.btn.rect.y);
                }
            }

            // Send quest line edits
            NBTTagCompound chPayload = new NBTTagCompound();
            NBTTagList cdList = new NBTTagList();
            NBTTagCompound tagEntry = new NBTTagCompound();
            NBTConverter.UuidValueType.QUEST_LINE.writeId(lID, tagEntry);
            tagEntry.setTag("config", qLine.writeToNBT(new NBTTagCompound(), null));
            cdList.appendTag(tagEntry);
            chPayload.setTag("data", cdList);
            chPayload.setInteger("action", 0);
            NetChapterEdit.sendEdit(chPayload);

            grabList.clear();
            return true;
        }

        PanelButtonQuest btnClicked = gui.getButtonAt(mx, my);

        if (btnClicked != null) // Pickup the group or the single one if none are selected
        {
            if (PanelToolController.selected.size() > 0) {
                if (!PanelToolController.selected.contains(btnClicked)) return false;

                for (PanelButtonQuest btn : PanelToolController.selected) {
                    grabList.add(new GrabEntry(btn, btn.rect.x - btnClicked.rect.x, btn.rect.y - btnClicked.rect.y));
                }
            } else {
                grabList.add(new GrabEntry(btnClicked, 0, 0));
            }

            return true;
        }

        return false;
    }

    @Override
    public boolean onMouseRelease(int mx, int my, int click) {
        return false;
    }

    @Override
    public boolean onMouseScroll(int mx, int my, int scroll) {
        return false;
    }

    @Override
    public boolean onKeyPressed(char c, int keyCode) {
        return grabList.size() > 0;
    }

    @Override
    public boolean clampScrolling() {
        return grabList.size() <= 0;
    }

    @Override
    public boolean useSelection() {
        return grabList.size() <= 0;
    }

    private class GrabEntry {

        private final PanelButtonQuest btn;
        private final int offX;
        private final int offY;

        private GrabEntry(PanelButtonQuest btn, int offX, int offY) {
            this.btn = btn;
            this.offX = offX;
            this.offY = offY;
        }
    }
}
