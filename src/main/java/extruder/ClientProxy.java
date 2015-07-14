/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraftforge.client.MinecraftForgeClient;

import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy implements IExtruderProxy {

    @Override
    public void preInit() {}

    @Override
    public void init() {
        MinecraftForgeClient.registerItemRenderer(Extruder.Items.extruder, new ItemExtruderRenderer());
        RenderingRegistry.registerEntityRenderingHandler(EntityExtruder.class, new EntityExtruderRenderer());
    }

    @Override
    public void postInit() {}
}