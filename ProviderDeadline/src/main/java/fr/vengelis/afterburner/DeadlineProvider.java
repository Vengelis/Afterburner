package fr.vengelis.afterburner;

import fr.vengelis.afterburner.instructions.impl.JobIdInstruction;
import fr.vengelis.afterburner.instructions.impl.PlayerRequesterInstruction;
import fr.vengelis.afterburner.providers.AfterburnerProvider;
import fr.vengelis.afterburner.configurations.AsConfig;
import fr.vengelis.afterburner.providers.IAfterburnerProvider;
import fr.vengelis.afterburner.providers.ProviderInstructions;
import fr.vengelis.afterburner.handler.DeadlineConnect;
import fr.vengelis.afterburner.instructions.BaseDeadlineInstruction;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@AfterburnerProvider(name = "Deadline")
public class DeadlineProvider implements IAfterburnerProvider, AsConfig {

    private static DeadlineProvider instance;
    private DeadlineConnect DC;
    private final List<BaseDeadlineInstruction> instructions = new ArrayList();

    @Override
    public Object getInstructionValue(ProviderInstructions providerInstructions) {
        return this.instructions.stream()
                .filter(instruction -> instruction.getInstruction().equals(providerInstructions))
                .map(BaseDeadlineInstruction::execute)
                .findFirst()
                .orElse("");
    }

    @Override
    public void loadConfig() throws IOException {
        instance = this;
        Path configPath = Paths.get(this.getWorkingDirectory(), "ProviderDeadline", "config.yml");
        Map<String, Object> data = new Yaml().load(Files.newInputStream(configPath));
        Map<String, Object> api = (Map<String, Object>) data.get("web-api");
        DC = new DeadlineConnect(api.get("host").toString(), (Integer) api.get("port"));

        instructions.add(new JobIdInstruction());
        instructions.add(new PlayerRequesterInstruction());
    }

    public static DeadlineProvider get() {
        return instance;
    }

    public List<BaseDeadlineInstruction> getInstructions() {
        return instructions;
    }

    public DeadlineConnect getDC() {
        return DC;
    }
}
