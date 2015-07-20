/**
 * This class was created by <awesommist>. It's distributed as
 * part of the Extruder Mod. Get the Source Code in github:
 * https://github.com/awesommist/Extruder
 */
package extruder;

import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import dynamics.config.properties.ConfigProperty;
import dynamics.config.properties.OnlineModifiable;

public class Config {

    @OnlineModifiable
    @ConfigProperty(name = "destroyBlocks", category = "general", comment = "True if the extruder should destroy the blocks it mines. Default is false")
    public static boolean destroyBlocks = false;

    @OnlineModifiable
    @ConfigProperty(name = "useFuel", category = "general", comment = "True if the extruder should use fuel. Default is true")
    public static boolean useFuel = true;

    @OnlineModifiable
    @ConfigProperty(name = "maxFuelLevel", category = "general", comment = "The max fuel level an extruder can have. Default is 100000")
    public static int maxFuelLevel = 100000;

    @OnlineModifiable
    @ConfigProperty(name = "fuelPerTick", category = "general", comment = "The fuel used per tick by the extruder when running. Default is 1")
    public static int fuelPerTick = 1;

    @OnlineModifiable
    @ConfigProperty(name = "fuelPerBlockMined", category = "general", comment = "The fuel used per each block mined. Default is 350")
    public static int fuelPerBlockMined = 350;

    @OnlineModifiable
    @ConfigProperty(name = "fuelPerBlockExtruded", category = "general", comment = "The fuel used per each block extruded. Default is 200")
    public static int fuelPerBlockExtruded = 200;

    public static void register() {
        @SuppressWarnings("unchecked")
        final List<IRecipe> recipeList = CraftingManager.getInstance().getRecipeList();

        recipeList.add(new ShapelessOreRecipe(Extruder.Items.extruder, Blocks.piston, Blocks.dispenser));
    }
}