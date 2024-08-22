package fr.vengelis.afterburner.commonfiles.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.commonfiles.AbstractBCF;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ServerFiles extends AbstractBCF {

    private String fileName;
    private Boolean typeFolder;
    private String renameTo;

    public ServerFiles(JsonObject jsonObject) {
        super(jsonObject.get("name").getAsString(), jsonObject.get("enabled").getAsBoolean());
        Map<String, Object> map = new Gson().fromJson(jsonObject.get("settings"), Map.class);
        this.fileName = map.get("file-name").toString();
        this.typeFolder = Boolean.parseBoolean(map.get("is-folder").toString());
        this.renameTo = map.get("rename-to").toString();
    }

    @Override
    public void copy() throws IOException {
        if(!typeFolder) {
            FileUtils.copyFile(
                    new File(ConfigGeneral.PATH_COMMON_FILES.getData() + File.separator + "serverfiles" + File.separator + fileName),
                    new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + renameTo));
        } else {
            FileUtils.copyDirectory(new File(ConfigGeneral.PATH_COMMON_FILES.getData() + File.separator + "serverfiles" + File.separator + fileName),
                    new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + renameTo));
        }
    }

}
