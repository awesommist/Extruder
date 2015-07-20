/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraft.util.StatCollector;
import dynamics.api.IValueReceiver;
import dynamics.gui.SyncedGuiContainer;
import dynamics.gui.component.BaseComponent;
import dynamics.gui.component.BaseComposite;
import dynamics.gui.component.GuiComponentLabel;
import dynamics.gui.component.GuiComponentSprite;
import dynamics.gui.component.GuiComponentSprite.Sprites;
import dynamics.gui.logic.ValueCopyAction;

public class GuiExtruder extends SyncedGuiContainer<ContainerExtruder> {

    public GuiExtruder(ContainerExtruder container) {
        super (container, 176, 244, "extruder.gui.extruder");
    }

    @Override
    protected BaseComposite createRoot() {
        final EntityExtruder extruder = getContainer().getOwner();

        BaseComposite main = super.createRoot();

        main.addComponent(new GuiComponentLabel(7, 71, StatCollector.translateToLocal("extruder.gui.extruder.mined")));

        main.addComponent(new GuiComponentSprite(14, 37, Sprites.extruderLine, BaseComponent.TEXTURE_SHEET));

        if (Config.useFuel) {
            main.addComponent(new GuiComponentLabel(104, 24, StatCollector.translateToLocal("extruder.gui.extruder.fuel")));
            final GuiComponentLabel fuelLevel = new GuiComponentLabel(131, 24, "0");
            fuelLevel.setMaxWidth(100);
            addSyncUpdateListener(ValueCopyAction.create(extruder.getFuelProvider(), new IValueReceiver<Integer>() {
                @Override
                public void setValue(Integer value) {
                    fuelLevel.setText(String.valueOf(extruder.getFuelProvider().getValue()));
                }
            }));
            main.addComponent(fuelLevel);
        }

        return main;
    }
}