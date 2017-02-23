package de.ellpeck.miningtweaks;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent.OnConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = MiningTweaks.MOD_ID, name = MiningTweaks.NAME, version = MiningTweaks.VERSION, guiFactory = "de.ellpeck.miningtweaks.GuiFactory")
public class MiningTweaks{

    public static final String MOD_ID = "miningtweaks";
    public static final String NAME = "MiningTweaks";
    public static final String VERSION = "@VERSION@";

    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static Configuration config;
    private static String[] hardnessTweaks;
    private static String[] miningLevelTweaks;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event){
        config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        defineConfigs();

        MinecraftForge.EVENT_BUS.register(new ConfigEventHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event){
        for(String s : hardnessTweaks){
            try{
                String[] parts = s.split("@");

                String reg = parts[0];
                float hardness = Float.parseFloat(parts[1]);

                Block block = Block.REGISTRY.getObject(new ResourceLocation(reg));
                block.setHardness(hardness);
            }
            catch(Exception e){
                LOGGER.error("Trying to parse config "+s+" for hardness tweaks failed!", e);
            }
        }

        for(String s : miningLevelTweaks){
            try{
                String[] parts = s.split("@");

                String reg = parts[0];
                int meta = Integer.parseInt(parts[1]);
                String tool = parts[2];
                int level = Integer.parseInt(parts[3]);

                Block block = Block.REGISTRY.getObject(new ResourceLocation(reg));

                if(meta == 32767){
                    block.setHarvestLevel(tool, level);
                }
                else{
                    block.setHarvestLevel(tool, level, block.getStateFromMeta(meta));
                }
            }
            catch(Exception e){
                LOGGER.error("Trying to parse config "+s+" for mining level tweaks failed!", e);
            }
        }
    }

    private static void defineConfigs(){
        hardnessTweaks = config.getStringList("hardness", Configuration.CATEGORY_GENERAL, new String[0], "The blocks whose hardness should be modified. This needs to be the registry name of the block followed by an @ followed by the new hardness, so for example: 'minecraft:stone@5'");
        miningLevelTweaks = config.getStringList("mininglevels", Configuration.CATEGORY_GENERAL, new String[0], "The blocks whose mining level should be modified. This needs to be the registry name of the block followed by an @ followed by the metadata (32767 if it should apply to all ones) followed by an @ followed by the tool class ('pickaxe', 'axe' or 'shovel') followed by an @ followed by the harvest level (0 = wood, 1 = stone, 2 = iron, 3 = diamond, custom levels work), so for example: 'minecraft:stone@0@pickaxe@2'");

        if(config.hasChanged()){
            config.save();
        }
    }

    private static class ConfigEventHandler{

        @SubscribeEvent
        public void onConfigurationChangedEvent(OnConfigChangedEvent event){
            if(MOD_ID.equals(event.getModID())){
                defineConfigs();
            }
        }
    }
}
