package com.konasclient.konas.setting;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockListSetting {
    private List<Block> blocks;

    private List<String> blocksString;

    public BlockListSetting(String... blockNames) {
        blocks = new ArrayList<>();
        blocksString = new ArrayList<>();

        for (String name : blockNames) {
            if (!blocksString.contains(name.toUpperCase(Locale.ENGLISH)) && getBlockFromName(name) != null) {
                blocksString.add(name.toUpperCase(Locale.ENGLISH));
            }
        }
    }

    public BlockListSetting(ArrayList<String> blockNames) {
        blocks = new ArrayList<>();
        blocksString = new ArrayList<>();

        for (String name : blockNames) {
            if (!blocksString.contains(name.toUpperCase(Locale.ENGLISH)) && getBlockFromName(name) != null) {
                blocksString.add(name.toUpperCase(Locale.ENGLISH));
            }
        }
    }

    public void addBlockStrings(ArrayList<String> blockNames) {
        for (String name : blockNames) {
            if (!blocksString.contains(name.toUpperCase(Locale.ENGLISH)) && getBlockFromName(name) != null) {
                blocksString.add(name.toUpperCase(Locale.ENGLISH));
            }
        }
    }

    public boolean addBlock(String blockName) {
        if (!blocksString.contains(blockName.toUpperCase(Locale.ENGLISH)) && getBlockFromName(blockName) != null ) {
            blocksString.add(blockName.toUpperCase(Locale.ENGLISH));
            return true;
        }
        return false;
    }

    public boolean removeBlock(String blockName) {
        return blocksString.remove(blockName.toUpperCase(Locale.ENGLISH));
    }

    public void refreshBlocks() {
        blocks.clear();
        blocksString.forEach(str -> {
            Block block = getBlockFromName(str);
            if (block != null) {
                blocks.add(block);
            }
        });
    }

    public List<String> getBlocksAsString() {
        List<String> str = new ArrayList<>();
        blocks.forEach(block -> {
            str.add(block.getName().getString());
        });
        return str;
    }

    private Block getBlockFromName(String name) {
        for (Block block : Registry.BLOCK) {
            if (block.getName().getString().equalsIgnoreCase(name)) {
                return block;
            }
        }
        return null;
    }

    public List<Block> getBlocks() {
        return blocks;
    }
}
