/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import net.minecraftforge.common.config.Configuration;
import dynamics.DynamicLib;
import dynamics.config.ItemInstances;
import dynamics.config.game.ModStartupHelper;
import dynamics.config.game.RegisterItem;
import dynamics.config.properties.ConfigProcessing;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLMissingMappingsEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = Extruder.MODID, name = Extruder.NAME, version = Extruder.VERSION, dependencies = Extruder.DEPENDENCIES)
public class Extruder {

    public static final String MODID = "extruder";
    public static final String NAME = "Extruder Mod";
    public static final String VERSION = "$VERSION$.$BUILD$";
    public static final String PROXY_SERVER = "extruder.ServerProxy";
    public static final String PROXY_CLIENT = "extruder.ClientProxy";
    public static final String DEPENDENCIES = "required-after:dynamiclib";

    public static final int ENTITY_EXTRUDER_ID = 777;

    @Instance(MODID)
    public static Extruder instance;

    @SidedProxy(clientSide = PROXY_CLIENT, serverSide = PROXY_SERVER)
    public static IExtruderProxy proxy;

    public static class Items implements ItemInstances {
        @RegisterItem(name = "extruder")
        public static ItemExtruder extruder;
    }

    private final ModStartupHelper startupHelper = new ModStartupHelper(MODID) {
        @Override
        protected void populateConfig(Configuration config) {
            ConfigProcessing.processAnnotations(MODID, config, Config.class);
        }
    };

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        startupHelper.registerItemsHolder(Items.class);
        startupHelper.preInit(event.getSuggestedConfigurationFile());

        Config.register();
        FMLCommonHandler.instance().bus().register(Extruder.instance);

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, DynamicLib.proxy.wrapHandler(new ExtruderGuiHandler()));

        EntityRegistry.registerModEntity(EntityExtruder.class, "extruder", ENTITY_EXTRUDER_ID, Extruder.instance, 64, 1, true);

        proxy.preInit();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit();
    }

    @EventHandler
    public void handleRenames(FMLMissingMappingsEvent event) {
        startupHelper.handleRenames(event);
    }
}