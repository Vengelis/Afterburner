package fr.vengelis.afterburner.commonfiles.impl.minecraftserver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.commonfiles.AbstractBCF;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class McWorlds extends AbstractBCF {

    private final String folderName;
    private final String finalWorldName;

    public McWorlds(JsonObject jsonObject) {
        super(jsonObject.get("name").getAsString(), jsonObject.get("enabled").getAsBoolean());
        Map<String, Object> map = new Gson().fromJson(jsonObject.get("settings"), Map.class);
        this.folderName = map.get("folder-name").toString();
        this.finalWorldName = map.get("final-name").toString();
    }

    @Override
    public void copy() throws IOException {
        FileUtils.copyDirectory(
                new File(ConfigGeneral.PATH_COMMON_FILES.getData().toString() + File.separator + "worlds" + File.separator + folderName),
                new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + this.finalWorldName));
    }

    public String getFolderName() {
        return folderName;
    }
}
