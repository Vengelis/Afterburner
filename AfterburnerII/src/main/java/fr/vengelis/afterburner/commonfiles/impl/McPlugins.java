package fr.vengelis.afterburner.commonfiles.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.commonfiles.AbstractBCF;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class McPlugins extends AbstractBCF {


    private final String fileName;
    private final boolean configFolderEnabled;
    private final String configFolderName;

    public McPlugins(JsonObject jsonObject) {
        super(jsonObject.get("name").getAsString(), jsonObject.get("enabled").getAsBoolean());
        Map<String, Object> map = new Gson().fromJson(jsonObject.get("settings"), Map.class);
        this.fileName = map.get("file-name").toString();
        this.configFolderEnabled = Boolean.parseBoolean(map.get("config-folder").toString());
        this.configFolderName = (this.configFolderEnabled ? map.get("config-folder-name").toString() : null);
    }

    @Override
    public void copy() throws IOException {
        FileUtils.copyFile(
                new File(ConfigGeneral.PATH_COMMON_FILES.getData() + File.separator + "plugins" + File.separator + fileName),
                new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + "plugins" +
                        File.separator + fileName));
        if(configFolderEnabled) {
            FileUtils.copyDirectory(new File(ConfigGeneral.PATH_COMMON_FILES.getData() + File.separator + "plugins" + File.separator + configFolderName),
                    new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + "plugins" + File.separator + configFolderName));
        }
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isConfigFolderEnabled() {
        return configFolderEnabled;
    }

    public String getConfigFolderName() {
        return configFolderName;
    }
}
