package fr.vengelis.afterburner.interconnection.instructions.impl;

import com.google.gson.JsonObject;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
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
        AfterburnerSlaveApp.get().getEventManager().call(event2);
        if(event2.isCancelled()) return new JsonObject();

        JsonObject afterburnerInfo = new JsonObject();

        // REQUEST
        JsonObject requestInfos = new JsonObject();
        for (ProviderInstructions providerInstructions : ProviderInstructions.values()) {
            requestInfos.addProperty(providerInstructions.name(), AfterburnerSlaveApp.get().getProviderManager().getResultInstruction(providerInstructions).toString());
        }
        requestInfos.addProperty("machineName", AfterburnerSlaveApp.get().getMachineName());
        afterburnerInfo.add("request", requestInfos);

        // RESOURCES
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(
                OperatingSystemMXBean.class);
        double cpuUsage = osBean.getSystemLoadAverage() <= 0 ? osBean.getSystemLoadAverage() * 100 : 0; // false
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

        afterburnerInfo.add("resources", resourcesInfo);

        // RUNNING
        JsonObject runningInfos = new JsonObject();
        runningInfos.addProperty("total", AfterburnerSlaveApp.get().getTotalTimeRunning());  // false - TODO : Fonction absolument pas faite
        afterburnerInfo.add("running", runningInfos);

        // REPREPARED
        JsonObject repreparedInfos = new JsonObject();
        repreparedInfos.addProperty("enabled", AfterburnerSlaveApp.get().isReprepareEnabled());
        repreparedInfos.addProperty("count", AfterburnerSlaveApp.get().getRepreparedCount());
        afterburnerInfo.add("reprepared", repreparedInfos);

        // LOGS
//        JsonObject logsInfos = new JsonObject();
//        logsInfos.addProperty("history", new Gson().toJson(AfterburnerApp.get().getLogHistory()));
//        logsInfos.addProperty("skippers", new Gson().toJson(AfterburnerApp.get().getLogSkipperManager().getSkipperList().stream().map(Skipper::getPattern).collect(Collectors.toList())));
//        afterburnerInfo.add("logs", logsInfos);

        ReturnGetInfosEvent event3 = new ReturnGetInfosEvent(afterburnerInfo);
        AfterburnerSlaveApp.get().getEventManager().call(event3);
        if(event3.isCancelled()) return new JsonObject();
        return event3.getDatas();
    }

    public void print() {
        JsonObject info = execute();

        JsonObject cpuInfo = info.getAsJsonObject("resources").getAsJsonObject("cpu");
        double cpuUsage = cpuInfo.get("usage").getAsDouble();
        int availableCpu = cpuInfo.get("available").getAsInt();
        System.out.println("CPU Usage: " + cpuUsage + "%");
        System.out.println("Available CPUs: " + availableCpu);

        JsonObject ramInfo = info.getAsJsonObject("resources").getAsJsonObject("ram");
        long totalMemory = ramInfo.get("total").getAsLong();
        long freeMemory = ramInfo.get("free").getAsLong();
        long usedMemory = ramInfo.get("used").getAsLong();
        long maxMemory = ramInfo.get("max").getAsLong();
        System.out.println("Total Memory: " + totalMemory / (1024 * 1024) + " MB");
        System.out.println("Free Memory: " + freeMemory / (1024 * 1024) + " MB");
        System.out.println("Used Memory: " + usedMemory / (1024 * 1024) + " MB");
        System.out.println("Max Memory: " + maxMemory / (1024 * 1024) + " MB");

        JsonObject runningInfo = info.getAsJsonObject("running");
        long totalRunningTime = runningInfo.get("total").getAsLong();
        System.out.println("Total Running Time: " + totalRunningTime + " ms");

        JsonObject repreparedInfo = info.getAsJsonObject("reprepared");
        boolean isReprepareEnabled = repreparedInfo.get("enabled").getAsBoolean();
        int reprepareCount = repreparedInfo.get("count").getAsInt();
        System.out.println("Reprepare Enabled: " + isReprepareEnabled);
        System.out.println("Reprepare Count: " + reprepareCount);

//        JsonObject logsInfo = info.getAsJsonObject("logs");
//        String logHistory = logsInfo.get("history").getAsString();
//        String logSkippers = logsInfo.get("skippers").getAsString();
//        System.out.println("Log History: " + logHistory);
//        System.out.println("Log Skippers: " + logSkippers);
    }
}
