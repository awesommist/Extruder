/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import dynamics.gui.BaseGuiContainer;

public class GuiExtruder extends BaseGuiContainer<ContainerExtruder> {

    public GuiExtruder(ContainerExtruder container) {
        super (container, 176, 244, "extruder.gui.extruder");
    }
}