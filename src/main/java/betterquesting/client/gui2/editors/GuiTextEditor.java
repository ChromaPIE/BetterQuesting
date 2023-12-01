package betterquesting.client.gui2.editors;

import betterquesting.api.client.gui.misc.IVolatileScreen;
import betterquesting.api.misc.ICallback;
import betterquesting.api2.client.gui.GuiScreenCanvas;
import betterquesting.api2.client.gui.controls.IPanelButton;
import betterquesting.api2.client.gui.controls.PanelButton;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.client.gui.controls.PanelTextField;
import betterquesting.api2.client.gui.controls.filters.FieldFilterString;
import betterquesting.api2.client.gui.events.IPEventListener;
import betterquesting.api2.client.gui.events.PEventBroadcaster;
import betterquesting.api2.client.gui.events.PanelEvent;
import betterquesting.api2.client.gui.events.types.PEventButton;
import betterquesting.api2.client.gui.misc.GuiAlign;
import betterquesting.api2.client.gui.misc.GuiPadding;
import betterquesting.api2.client.gui.misc.GuiRectangle;
import betterquesting.api2.client.gui.misc.GuiTransform;
import betterquesting.api2.client.gui.panels.CanvasTextured;
import betterquesting.api2.client.gui.panels.bars.PanelVScrollBar;
import betterquesting.api2.client.gui.panels.content.PanelTextBox;
import betterquesting.api2.client.gui.panels.lists.CanvasScrolling;
import betterquesting.api2.client.gui.themes.presets.PresetColor;
import betterquesting.api2.client.gui.themes.presets.PresetTexture;
import betterquesting.api2.utils.QuestTranslation;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;

public class GuiTextEditor extends GuiScreenCanvas implements IPEventListener, IVolatileScreen
{
    private final ICallback<String> callback;
    private final String textIn;
    
    private PanelTextField<String> flText;
    
    public GuiTextEditor(GuiScreen parent, String text, ICallback<String> callback)
    {
        super(parent);
        
        textIn = text;
        this.callback = callback;
    }
    
    @Override
    public void initPanel()
    {
        super.initPanel();
        
        PEventBroadcaster.INSTANCE.register(this, PEventButton.class);
        Keyboard.enableRepeatEvents(true);
    
        // Background panel
        CanvasTextured cvBackground = new CanvasTextured(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(0, 0, 0, 0), 0), PresetTexture.PANEL_MAIN.getTexture());
        this.addPanel(cvBackground);
    
        cvBackground.addPanel(new PanelButton(new GuiTransform(GuiAlign.BOTTOM_CENTER, -100, -16, 200, 16, 0), 0, QuestTranslation.translate("gui.back")));
    
        PanelTextBox txTitle = new PanelTextBox(new GuiTransform(GuiAlign.TOP_EDGE, new GuiPadding(0, 16, 0, -32), 0), QuestTranslation.translate("betterquesting.title.edit_text")).setAlignment(1);
        txTitle.setColor(PresetColor.TEXT_HEADER.getColor());
        cvBackground.addPanel(txTitle);
        
        flText = new PanelTextField<>(new GuiTransform(GuiAlign.FULL_BOX, new GuiPadding(124, 32, 24, 32), 0), flText != null ? flText.getRawText() : textIn, FieldFilterString.INSTANCE);
        cvBackground.addPanel(flText);
        flText.setMaxLength(Integer.MAX_VALUE);
        flText.enableWrapping(true);
        flText.lockFocus(true);
    
        CanvasScrolling cvFormatList = new CanvasScrolling(new GuiTransform(GuiAlign.LEFT_EDGE, new GuiPadding(16, 32, -116, 32), 0));
        cvBackground.addPanel(cvFormatList);
        
        EnumChatFormatting[] tfValues = EnumChatFormatting.values();
        // Specify how many macro buttons are manually added, before the buttons for the default colors and formatting
        int macroCount = 10;

        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 0, 100, 16), 1, "§9§nHyperlink§r", "§9§n[url][/url]§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 1, 100, 16), 1, "§3PS:§r", "§3PS: text§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 2, 100, 16), 1, "§cNote§r", "§cNote: text§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 3, 100, 16), 1, "§4§lWarning§r", "§4§lWARNING:§r§4 text§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 4, 100, 16), 1, "§6§oKeyword§r", "§6§otext§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 5, 100, 16), 1, "§2Positive Focus§r", "§2text§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 6, 100, 16), 1, "§5Neutral Focus§r", "§5text§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 7, 100, 16), 1, "§2§nEU Gained§r", "§2§n100 EU§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 8, 100, 16), 1, "§c§nEU Spent§r", "§c§n100 EU§r"));
        cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, 16 * 9, 100, 16), 1, "§d§oRecipe Time§r", "§d§o100s§r"));
        for(int i = 0; i < tfValues.length; i++)
        {
            cvFormatList.addPanel(new PanelButtonStorage<>(new GuiRectangle(0, (i + macroCount) * 16, 100, 16), 1, tfValues[i].getFriendlyName(), tfValues[i].toString()));
        }
    
        PanelVScrollBar scFormatScroll = new PanelVScrollBar(new GuiTransform(GuiAlign.RIGHT_EDGE, new GuiPadding(0, 0, -8, 0), 0));
        cvBackground.addPanel(scFormatScroll);
        scFormatScroll.getTransform().setParent(cvFormatList.getTransform());
        cvFormatList.setScrollDriverY(scFormatScroll);
        scFormatScroll.setActive(cvFormatList.getScrollBounds().getHeight() > 0);
    }
    
    @Override
    public void onPanelEvent(PanelEvent event)
    {
        if(event instanceof PEventButton)
        {
            onButtonPress((PEventButton)event);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void onButtonPress(PEventButton event)
    {
        IPanelButton btn = event.getButton();
    
        if(btn.getButtonID() == 0) // Exit
        {
            mc.displayGuiScreen(this.parent);
        } else if(btn.getButtonID() == 1 && btn instanceof PanelButtonStorage)
        {
            String format = ((PanelButtonStorage<String>)btn).getStoredValue();
            flText.writeText(format);
        }
    }
    
    @Override
    public void onGuiClosed()
    {
        super.onGuiClosed();
        
        if(callback != null)
        {
            callback.setValue(flText.getRawText());
        }
    }
}
