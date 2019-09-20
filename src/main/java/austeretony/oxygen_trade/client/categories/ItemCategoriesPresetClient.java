package austeretony.oxygen_trade.client.categories;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import austeretony.oxygen_core.client.preset.PresetClient;
import austeretony.oxygen_core.common.util.JsonUtils;
import austeretony.oxygen_trade.common.main.TradeMain;
import io.netty.buffer.ByteBuf;

public class ItemCategoriesPresetClient implements PresetClient {

    public static final OfferCategoryClient COMMON_CATEGORY = new OfferCategoryClient("oxygen_trade.category.common");

    static {
        COMMON_CATEGORY.subCategories.add(new SubCategoryCommon("oxygen_trade.category.common"));
    }

    private long versionId;

    private final List<OfferCategoryClient> categories = new ArrayList<>(3);

    private boolean verified;

    public boolean isVerified() {
        return this.verified;
    }

    public List<OfferCategoryClient> getCategories() {
        return this.categories;
    }

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
    public boolean loadVersionId(String folder) {
        this.versionId = 0L;
        String pathStr = folder + "/item_categories_version.txt";
        Path path = Paths.get(pathStr);
        if (Files.exists(path)) {
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathStr))) {  
                this.versionId = Long.parseLong(bufferedReader.readLine());
                return true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }           
        }
        return false;
    }

    @Override
    public boolean load(String folder) {
        String pathStr = folder + "/item_categories.json";
        Path path = Paths.get(pathStr);     
        if (Files.exists(path)) {
            this.categories.clear();
            try {      
                this.categories.add(COMMON_CATEGORY);
                for (JsonElement categoryEntry : JsonUtils.getExternalJsonData(pathStr).getAsJsonArray())
                    this.categories.add(OfferCategoryClient.deserialize(categoryEntry.getAsJsonObject()));
                this.sortCategories();
                this.verified = true;
                return true;
            } catch (IOException exception) {
                exception.printStackTrace();
            }       
        }
        return false;
    }

    private void sortCategories() {
        Collections.sort(this.categories, (c1, c2)->(c1.localizedName().compareTo(c2.localizedName())));
    }

    @Override
    public boolean save(String folder) {
        String pathStr = folder + "/item_categories.json";
        Path path = Paths.get(pathStr);
        if (!Files.exists(path)) {
            try {                   
                Files.createDirectories(path.getParent());              
            } catch (IOException exception) {               
                exception.printStackTrace();
            }
        }
        try {
            JsonArray config = new JsonArray();
            for (OfferCategoryClient category : this.categories)
                config.add(category.serialize());  
            JsonUtils.createExternalJsonFile(pathStr, config);
            return this.saveVersionId(folder);
        } catch (IOException exception) {      
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean reloadAfterSave() {
        return true;
    }

    private boolean saveVersionId(String folder) {
        String idPathStr = folder + "/item_categories_version.txt";
        Path idPath = Paths.get(idPathStr);
        try {               
            Files.createDirectories(idPath.getParent());             
            try (PrintStream printStream = new PrintStream(new File(idPathStr))) {
                printStream.println(this.versionId);
            } 
            return true;
        } catch (IOException exception) {      
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public void read(ByteBuf buffer) {
        this.categories.clear();
        this.versionId = buffer.readLong();
        int amount = buffer.readByte();
        for (int i = 0; i < amount; i++)
            this.categories.add(OfferCategoryClient.read(buffer));
    }
}
