package fr.vengelis.afterburner.mprocess.argwrapper;

import fr.vengelis.afterburner.Afterburner;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.configurations.ConfigTemplate;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public abstract class BaseArgumentWrapper implements IArgWrapper{

    private final boolean noRamArgs;
    private String path;

    public void export() throws IOException {
        AfterburnerSlaveApp.get().getExporter().saveResource(new File(Afterburner.WORKING_AREA), "/wrapper/arg-starter/" + getType() + ".yml", false);
    }

    public void load() throws IOException {
        File config = new File(Afterburner.WORKING_AREA + File.separator + "wrapper" + File.separator + "arg-starter" + File.separator + getType() + ".yml");
        InputStream stm = new FileInputStream(config);
        Yaml yaml = new Yaml();
        Map<String, Object> data = yaml.load(stm);
        path = data.get("cmd-path").toString();
    }

    public abstract String getType();
    public String getBaseLauncher() {
        return path;
    }

    protected abstract String getPrefixMinimalRam();
    protected abstract String getPrefixMaximumRam();
    protected abstract String getPrefixExecutable();

    public BaseArgumentWrapper(boolean noRamArgs) {
        this.noRamArgs = noRamArgs;
    }

    public boolean hasNoRamArgs() {
        return noRamArgs;
    }

    public String getFinalMinimalRam() {
        return getPrefixMinimalRam()+ ConfigTemplate.EXECUTABLE_MIN_RAM.getData();
    }

    public String getFinalMaximumRam() {
        return getPrefixMaximumRam() + ConfigTemplate.EXECUTABLE_MAX_RAM.getData();
    }

    public String getFinalExecutable() {
        return getPrefixExecutable() +
                " \"" +
                ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() +
                File.separator +
                ConfigTemplate.EXECUTABLE_NAME.getData() +
                "\"";
    }

}
