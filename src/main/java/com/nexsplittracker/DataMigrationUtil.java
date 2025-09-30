package com.nexsplittracker;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataMigrationUtil
{
    private static final Logger logger = Logger.getLogger(DataMigrationUtil.class.getName());
    private static final String CURRENT_VERSION = "2.0";
    private final Gson gson;

    public DataMigrationUtil(Gson gson)
    {
        this.gson = gson;
    }

    public static class DataFile
    {
        public String version;
        public List<ItemDataV2> data;


        public DataFile(String version, List<ItemDataV2> data)
        {
            this.version = version;
            this.data = data;
        }
    }

    public List<ItemDataV2> loadData(String filePath) throws IOException
    {
        File file = new File(filePath);
        if (!file.exists())
        {
            logger.info("Data file does not exist, starting with empty data: " + filePath);
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(filePath))
        {
            // First determine if this is a versioned file or legacy format
            JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);

            if (jsonElement == null)
            {
                logger.info("Empty or invalid JSON file, starting with empty data");
                return new ArrayList<>();
            }

            if (jsonElement.isJsonObject())
            {
                // New versioned format
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                if (jsonObject.has("version") && jsonObject.has("data"))
                {
                    return loadVersionedData(jsonObject);
                }
            }

            if (jsonElement.isJsonArray())
            {
                // Legacy format - array of ItemData objects
                logger.info("Detected legacy data format, performing migration");
                return migrateLegacyData(jsonElement.getAsJsonArray(), filePath);
            }

            logger.warning("Unknown data format, starting with empty data");
            return new ArrayList<>();
        }
        catch (JsonParseException e)
        {
            logger.log(Level.SEVERE, "Error parsing JSON data file: " + filePath, e);
            throw new IOException("Failed to parse data file", e);
        }
    }

    private List<ItemDataV2> loadVersionedData(JsonObject jsonObject)
    {
        String version = jsonObject.get("version").getAsString();
        logger.info("Loading data file version: " + version);

        JsonArray dataArray = jsonObject.getAsJsonArray("data");
        Type listType = new TypeToken<ArrayList<ItemDataV2>>(){}.getType();
        List<ItemDataV2> data = gson.fromJson(dataArray, listType);

        return data != null ? data : new ArrayList<>();
    }

    private List<ItemDataV2> migrateLegacyData(JsonArray jsonArray, String filePath) throws IOException
    {
        // Create backup before migration
        createBackup(filePath);

        // Parse legacy data
        Type legacyListType = new TypeToken<ArrayList<ItemData>>(){}.getType();
        List<ItemData> legacyData = gson.fromJson(jsonArray, legacyListType);

        if (legacyData == null)
        {
            return new ArrayList<>();
        }

        // Convert to new format
        List<ItemDataV2> migratedData = new ArrayList<>();
        for (ItemData oldItem : legacyData)
        {
            migratedData.add(new ItemDataV2(oldItem));
        }

        logger.info("Migrated " + migratedData.size() + " items from legacy format");

        // Save in new format
        saveData(migratedData, filePath);

        return migratedData;
    }

    public void saveData(List<ItemDataV2> data, String filePath) throws IOException
    {
        DataFile dataFile = new DataFile(CURRENT_VERSION, data);

        try (FileWriter writer = new FileWriter(filePath))
        {
            gson.toJson(dataFile, writer);
            writer.flush();
            logger.info("Data saved to file: " + filePath + " (version: " + CURRENT_VERSION + ")");
        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Error saving data to file: " + filePath, e);
            throw e;
        }
    }

    private void createBackup(String originalFilePath) throws IOException
    {
        File originalFile = new File(originalFilePath);
        if (!originalFile.exists())
        {
            return;
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupPath = originalFilePath.replace(".json", "_backup_" + timestamp + ".json");
        File backupFile = new File(backupPath);

        try
        {
            Files.copy(originalFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            logger.info("Created backup: " + backupPath);
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING, "Failed to create backup before migration", e);
            throw new IOException("Failed to create backup before migration", e);
        }
    }

    public boolean isLegacyFormat(String filePath)
    {
        try (FileReader reader = new FileReader(filePath))
        {
            JsonElement jsonElement = gson.fromJson(reader, JsonElement.class);
            return jsonElement != null && jsonElement.isJsonArray();
        }
        catch (Exception e)
        {
            return false;
        }
    }
}