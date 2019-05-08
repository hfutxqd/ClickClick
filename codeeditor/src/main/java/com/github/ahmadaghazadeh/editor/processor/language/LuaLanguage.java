package com.github.ahmadaghazadeh.editor.processor.language;

import com.github.ahmadaghazadeh.editor.processor.utils.text.ArrayUtils;

import java.util.regex.Pattern;

public class LuaLanguage extends Language {
    private static final Pattern SYNTAX_NUMBERS = Pattern.compile("(\\b(\\d*[.]?\\d+)\\b)");

    public final Pattern getSyntaxNumbers() {
        return SYNTAX_NUMBERS;
    }

    private static final Pattern SYNTAX_SYMBOLS = Pattern.compile(
            "(!|\\+|-|\\*|<|>|=|\\?|\\||:|%|&)");

    public final Pattern getSyntaxSymbols() {
        return SYNTAX_SYMBOLS;
    }

    private static final Pattern SYNTAX_BRACKETS = Pattern.compile("(\\(|\\)|\\{|\\}|\\[|\\])");

    public final Pattern getSyntaxBrackets() {
        return SYNTAX_BRACKETS;
    }

    private static final Pattern SYNTAX_KEYWORDS = Pattern.compile(
            "(?<=\\b)((and)|(break)|(do)|(else)|(elseif)|(end)|(false)|(for)" +
                    "|(function)|(if)|(in)|(local)|(nil)|(not)|(or)|(repeat)" +
                    "|(return)|(then)|(true)|(until)|(while)|(null)" +
                    ")(?=\\b)"); // CASE_INSENSITIVE

    public final Pattern getSyntaxKeywords() {
        return SYNTAX_KEYWORDS;
    }

    private static final Pattern SYNTAX_METHODS = Pattern.compile(
            "(?<=(function) )(\\w+)", Pattern.CASE_INSENSITIVE);

    public final Pattern getSyntaxMethods() {
        return SYNTAX_METHODS;
    }

    private static final Pattern SYNTAX_STRINGS = Pattern.compile("\"(.*?)\"|'(.*?)'");

    public final Pattern getSyntaxStrings() {
        return SYNTAX_STRINGS;
    }

    private static final Pattern SYNTAX_COMMENTS = Pattern.compile("/\\*(?:.|[\\n\\r])*?\\*/|//.*");

    public final Pattern getSyntaxComments() {
        return SYNTAX_COMMENTS;
    }

    private static final char[] LANGUAGE_BRACKETS = new char[]{'{', '[', '(', '}', ']', ')'}; //do not change

    public final char[] getLanguageBrackets() {
        return LANGUAGE_BRACKETS;
    }

    /**
     * Слова для автопродолжения кода.
     */

    private static final String[] BLOCK_KEYWORDS = new String[] {
            "defineBlock", "defineLiquidBlock", "getAllBlockIds", "getDestroyTime",
            "getFriction", "setShape", "getRenderType", "getTextureCoords", "setColor",
            "setDestroyTime", "setExplosionResistance", "setFriction", "setRedstoneConsumer",
            "setLightLevel", "setLightOpacity", "setRenderLayer", "setRenderType"
    };
    private static final String[] ENTITY_KEYWORDS = new String[] {
            "getAll", "getAnimalAge", "getArmor", "getArmorCustomName", "getArmorDamage",
            "getEntityTypeId", "getExtraData", "getHealth", "getItemEntityCount",
            "getItemEntityData", "getItemEntityId", "getMaxHealth", "getMobSkin", "getNameTag",
            "getPitch()", "getRenderType", "getRider", "getRiding", "getTarget", "getUniqueId",
            "getVelX()", "getVelY()", "getVelZ()", "getYaw()", "isSneaking()", "remove",
            "removeAllEffects", "removeEffect", "rideAnimal", "setArmor", "setArmorCustomName",
            "setCape", "setCollisionSize", "setExtraData", "setFireTicks", "setHealth",
            "setImmobile", "setMaxHealth", "setMobSkin", "setNameTag", "setPosition",
            "setPositionRelative", "setCarriedItem", "setRenderType", "setRot", "setSneaking",
            "setTarget", "setVelX", "setVelY", "setVelZ", "spawnMob", "addEffect"
    };
    private static final String[] ITEM_KEYWORDS = new String[] {
            "getMaxDamage", "getMaxStackSize", "defineArmor", "defineThrowable",
            "getCustomThrowableRenderType", "addCraftRecipe", "setMaxDamage", "addFurnaceRecipe",
            "getName", "getTextureCoords", "getUseAnimation", "internalNameToId", "isValidItem",
            "setCategory", "setEnchantType", "addShapedRecipe", "setHandEquipped", "setProperties",
            "setStackedByData", "setUseAnimation", "translatedNameToId"
    };
    private static final String[] LEVEL_KEYWORDS = new String[] {
            "biomeIdToName", "canSeeSky", "setSpawnerTypeId", "destroyBlock", "explode",
            "getAddress", "getBiome", "getBiomeName", "getBrightness", "getGameMode",
            "getGrassColor", "getDifficulty", "setDifficulty", "getTile", "getData", "getTime",
            "getWorldDir()", "getWorldName", "setGameMode", "setGrassColor", "getLightningLevel()",
            "getRainLevel()", "setNightMode", "setSpawn", "setTile", "setTime", "spawnMob",
            "getSignText", "setSignText", "addParticle", "playSound", "playSoundEnt",
            "setBlockExtraData", "dropItem", "getChestSlot", "getChestSlotCount",
            "getChestSlotData", "setChestSlot", "setChestSlotCustomName", "setSpawnerEntityType",
            "setLightningLevel", "setRainLevel", "getFurnaceSlot", "getFurnaceSlotCount",
            "getFurnaceSlotData", "setFurnaceSlot"
    };
    private static final String[] MODPE_KEYWORDS = new String[] {
            "getOS()", "dumpVtable", "getI18n", "getBytesFromTexturePack", "getLanguage",
            "getMinecraftVersion", "langEdit", /*"leaveGame"*/"openInputStreamFromTexturePack",
            "overrideTexture", "readData", "removeData", "saveData", "resetFov", "resetImages",
            "setFoodItem", "setFov", "setGameSpeed", "setItem", "showTipMessage",
            "setUiRenderDebug", "takeScreenshot", "setGuiBlocks", "setItems", "setTerrain",
            "selectLevel"
    };
    private static final String[] PLAYER_KEYWORDS = new String[] {
            "addExp", "addItemInventory", "addItemCreativeInv", "canFly()", "clearInventorySlot",
            "enchant", "getEnchantments", "getArmorSlot", "getArmorSlotDamage", /*"getCarriedItem"*/
            "getCarriedItemCount", "getCarriedItemData", "getDimension", "getEntity",
            "getExhaustion", "getExp", "getHunger", "getInventorySlot", "getInventorySlotCount",
            "getInventorySlotData", "getItemCustomName", "setInventorySlot", "getLevel", "setLevel",
            "setSaturation", "setSelectedSlotId", "setItemCustomName", "getName",
            "getPointedBlockId()", "getPointedBlockData()", "getPointedBlockSide()",
            "getPointedBlockX()", "getPointedBlockY()", "getPointedBlockZ()", "getPointedEntity()",
            "getPointedVecX()", "getPointedVecY()", "getPointedVecZ()", "getSaturation", "getScore",
            "getSelectedSlotId()", /*"getX", "getY", "getZ",*/ "isFlying()", "setCanFly",
            "setFlying", /*"setHealth"*/"setArmorSlot", "setExhaustion", "setExp",
            /*"addItemCreativeInv",*/ "setHunger", "isPlayer()"
    };
    private static final String[] SERVER_KEYWORDS = new String[] {
            /*"getAddress",*/ "getAllPlayerNames()", "getAllPlayers()", "getPort()", "joinServer",
            "sendChat"
    };
    private static final String[] HOOKS_KEYWORDS = new String[] {
            "useItem", /*"destroyBlock",*/ "newLevel", "procCmd", "selectLevelHook",
            /*"leaveGame",*/ "attackHook", "modTick", "eatHook", "explodeHook", "deathHook",
            "entityAddedHook", "entityRemovedHook", "entityHurtHook", "projectileHitEntityHook",
            "playerAddExpHook", "playerExpLevelChangeHook", "redstoneUpdateHook",
            "startDestroyBlock", "continueDestroyBlock", "blockEventHook", "levelEventHook",
            "serverMessageReceiveHook", "screenChangeHook", "chatReceiveHook", "chatHook"
    };
    private static final String[] JS_KEYWORDS = new String[] {
            "function"
    };
    private static final String[] GLOBAL_KEYWORDS = new String[] {
            "clientMessage", "getPlayerX()", "getPlayerY()", "getPlayerZ()", "getPlayerEnt()"
    };

    /**
     * Соединение всех массивов в один. Этот массив и будет использоваться для
     * получения слов в редакторе.
     */
    private static final String[] ALL_KEYWORDS = ArrayUtils.join(String.class,
            BLOCK_KEYWORDS, ENTITY_KEYWORDS, ITEM_KEYWORDS, LEVEL_KEYWORDS, GLOBAL_KEYWORDS,
            MODPE_KEYWORDS, PLAYER_KEYWORDS, SERVER_KEYWORDS, HOOKS_KEYWORDS, JS_KEYWORDS);

    public final String[] getAllCompletions() {
        return ALL_KEYWORDS;
    }
}
