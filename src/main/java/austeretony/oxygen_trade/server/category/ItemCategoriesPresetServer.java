package austeretony.oxygen_trade.server.category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import austeretony.oxygen_core.common.util.JsonUtils;
import austeretony.oxygen_core.server.preset.PresetServer;
import austeretony.oxygen_core.server.preset.PresetsManagerServer;
import austeretony.oxygen_trade.common.main.TradeMain;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ItemCategoriesPresetServer implements PresetServer {

    private long versionId;

    private final List<OfferCategoryServer> categories = new ArrayList<>(3);

    private final ByteBuf compressed = Unpooled.buffer();

    @Override
    public int getId() {
        return TradeMain.ITEM_CATEGORIES_PRESET_ID;
    }

    @Override
    public String getDomain() {
        return "trade";
    }

    @Override
    public String getDisplayName() {
        return "item_categories";
    }

    @Override
    public long getVersionId() {
        return this.versionId;
    }

    @Override
    public boolean load(String folder) {
        if (!this.loadVersionId(folder))
            return false;
        String pathStr = folder + "item_categories.json";
        Path path = Paths.get(pathStr);   
        this.categories.clear();
        if (Files.exists(path)) {
            try {      
                for (JsonElement categoryEntry : JsonUtils.getExternalJsonData(pathStr).getAsJsonArray())
                    this.categories.add(OfferCategoryServer.deserialize(categoryEntry.getAsJsonObject()));
                this.compress();
                return true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }       
        } else {
            try {
                JsonArray config = JsonUtils.getInternalJsonData("assets/oxygen_trade/item_categories.json").getAsJsonArray();
                for (JsonElement categoryEntry : config)
                    this.categories.add(OfferCategoryServer.deserialize(categoryEntry.getAsJsonObject()));
                JsonUtils.createExternalJsonFile(pathStr, config);
                this.compress();
                return true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return false;
    }

    private boolean loadVersionId(String folder) {
        String pathStr = folder + "item_categories_version.txt";
        Path path = Paths.get(pathStr);
        if (Files.exists(path)) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathStr))) {  
                this.versionId = Long.parseLong(bufferedReader.readLine());
                return true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }           
        } else {
            this.versionId = Long.parseLong(PresetsManagerServer.PRESET_DATE_FORMAT.format(new Date()));
            try {               
                Files.createDirectories(path.getParent());             
                try (PrintStream printStream = new PrintStream(new File(pathStr))) {
                    printStream.println(this.versionId);
                } 
                return true;
            } catch (IOException exception) {      
                exception.printStackTrace();
            }
        }
        return false;
    }

    private void compress() {
        this.compressed.writeLong(this.versionId);
        this.compressed.writeByte(this.categories.size());
        for (OfferCategoryServer category : this.categories)
            category.write(this.compressed);
    }

    @Override
    public void write(ByteBuf buffer) {
        buffer.writeBytes(this.compressed, 0, this.compressed.writerIndex());
    }
}