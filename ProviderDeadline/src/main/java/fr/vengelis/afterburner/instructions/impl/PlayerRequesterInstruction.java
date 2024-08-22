package fr.vengelis.afterburner.instructions.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.DeadlineProvider;
import fr.vengelis.afterburner.instructions.BaseDeadlineInstruction;
import fr.vengelis.afterburner.providers.ProviderInstructions;

public class PlayerRequesterInstruction extends BaseDeadlineInstruction {

    public PlayerRequesterInstruction() {
        super(ProviderInstructions.PLAYER_REQUESTER);
    }

    @Override
    public String execute() {
        String slaveInfo = DeadlineProvider.get().getDC().getSlavesQueries().getSlaveInfo(AfterburnerApp.get().getMachineName());
        JsonArray slaveArray = new JsonParser().parse(slaveInfo).getAsJsonArray();
        String jobId = slaveArray.get(0).getAsJsonObject().get("JobId").getAsString();
        String jobInfo = DeadlineProvider.get().getDC().getJobsQueries().getJob(jobId);
        JsonArray jobArray = new JsonParser().parse(jobInfo).getAsJsonArray();
        JsonObject plugInfo = jobArray.get(0).getAsJsonObject().get("Props").getAsJsonObject().get("PlugInfo").getAsJsonObject();

        return plugInfo.has("PlayerRequester") ? plugInfo.get("PlayerRequester").getAsString() : AfterburnerApp.get().getMachineName();
    }
}
