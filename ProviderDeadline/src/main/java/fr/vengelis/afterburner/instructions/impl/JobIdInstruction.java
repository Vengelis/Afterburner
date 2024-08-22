package fr.vengelis.afterburner.instructions.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.DeadlineProvider;
import fr.vengelis.afterburner.instructions.BaseDeadlineInstruction;
import fr.vengelis.afterburner.providers.ProviderInstructions;

public class JobIdInstruction extends BaseDeadlineInstruction {

        public JobIdInstruction() {
            super(ProviderInstructions.JOB_ID);
        }

        @Override
        public String execute() {
            String slaveInfo = DeadlineProvider.get().getDC().getSlavesQueries().getSlaveInfo(AfterburnerApp.get().getMachineName());
            JsonArray jsonArray = new JsonParser().parse(slaveInfo).getAsJsonArray();
            return jsonArray.get(0).getAsJsonObject().get("JobId").getAsString();
        }
}
