package fr.vengelis.afterburner.interconnection.instructions.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.events.impl.RequestGetInfosEvent;
import fr.vengelis.afterburner.events.impl.ReturnGetInfosEvent;
import fr.vengelis.afterburner.interconnection.instructions.BaseCommunicationInstruction;
import fr.vengelis.afterburner.providers.ProviderInstructions;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

public class GetAtbInfosInstruction extends BaseCommunicationInstruction<JsonObject> {

    @Override
    public JsonObject execute() {
        RequestGetInfosEvent event2 = new RequestGetInfosEvent();
        AfterburnerApp.get().getEventManager().call(event2);
        if(event2.isCancelled()) return new JsonObject();

        JsonObject afterburnerInfo = new JsonObject();

        // REQUEST
        JsonObject requestInfos = new JsonObject();
        for (ProviderInstructions providerInstructions : ProviderInstructions.values()) {
            requestInfos.addProperty(providerInstructions.name(), AfterburnerApp.get().getProviderManager().getResultInstruction(providerInstructions).toString());
        }
        requestInfos.addProperty("machineName", AfterburnerApp.get().getMachineName());
        afterburnerInfo.add("request", requestInfos);

        // RESOURCES
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        double cpuUsage = osBean.getSystemLoadAverage() * 100;
        int availableCpu = osBean.getAvailableProcessors();
        long totalMemory = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = Runtime.getRuntime().maxMemory();

        JsonObject resourcesInfo = new JsonObject();

        JsonObject cpuInfos = new JsonObject();
        cpuInfos.addProperty("usage", cpuUsage);
        cpuInfos.addProperty("available", availableCpu);
        resourcesInfo.add("cpu", cpuInfos);

        JsonObject ramInfos = new JsonObject();
        ramInfos.addProperty("total", totalMemory);
        ramInfos.addProperty("free", freeMemory);
        ramInfos.addProperty("used", usedMemory);
        ramInfos.addProperty("max", maxMemory);
        resourcesInfo.add("ram", ramInfos);

        // RUNNING
        JsonObject runningInfos = new JsonObject();
        runningInfos.addProperty("total", AfterburnerApp.get().getTotalTimeRunning());
        afterburnerInfo.add("running", runningInfos);

        // REPREPARED
        JsonObject repreparedInfos = new JsonObject();
        repreparedInfos.addProperty("enabled", AfterburnerApp.get().isReprepareEnabled());
        repreparedInfos.addProperty("count", AfterburnerApp.get().getRepreparedCount());
        afterburnerInfo.add("reprepared", repreparedInfos);

        // LOGS
        JsonObject logsInfos = new JsonObject();
        logsInfos.addProperty("history", new Gson().toJson(AfterburnerApp.get().getLogHistory()));
        logsInfos.addProperty("skippers", new Gson().toJson(AfterburnerApp.get().getLogSkipperManager().getSkipperList()));
        afterburnerInfo.add("logs", logsInfos);

        ReturnGetInfosEvent event3 = new ReturnGetInfosEvent(afterburnerInfo);
        AfterburnerApp.get().getEventManager().call(event3);
        if(event3.isCancelled()) return new JsonObject();
        return event3.getDatas();
    }
}
