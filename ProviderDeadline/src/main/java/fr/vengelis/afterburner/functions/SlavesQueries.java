/**
 * Created by Vengelis_.
 * Date: 1/6/2023
 * Time: 12:43 AM
 * Project: Lunatrix
 */

package fr.vengelis.afterburner.functions;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.vengelis.afterburner.handler.ConnectionProperty;
import fr.vengelis.afterburner.utils.DeadlineUtils;

import java.util.List;

public class SlavesQueries {

    private ConnectionProperty cp;

    public SlavesQueries(ConnectionProperty cp) {
        this.cp = cp;
    }

    public String getSlaveNames() {
        return this.cp.get("/api/slaves?NamesOnly=true");
    }

    public String getSlaves() {
        String script = "/api/slaves";
        return this.cp.get(script);
    }

    public String getSlavesInfoSettings(List<String> names) {
        String script = "/api/slaves?Data=infosettings";
        if (names != null) {
            script = script + "&Name=" + DeadlineUtils.ArrayToCommaSeparatedString(names).replace(' ', '+');
        }

        return this.cp.get(script);
    }

    public String getSlaveInfo(String name) {
        return this.cp.get("/api/slaves?Name=" + name.replace(' ', '+') + "&Data=info");
    }

    public String getSlaveInfos(List<String> names) {
        String script = "/api/slaves?Data=info";
        if (names != null) {
            script = script + "&Name=" + DeadlineUtils.ArrayToCommaSeparatedString(names).replace(' ', '+');
        }

        return this.cp.get(script);
    }

    public String saveSlaveInfo(String info) {
        JsonObject jsonData = (new JsonParser()).parse(info).getAsJsonObject();
        String body = "{\"Command\":\"saveinfo\", \"SlaveInfo\":" + jsonData + "}";
        return this.cp.put("/api/slaves", body);
    }

    public String getSlaveSettings(String name) {
        return this.cp.get("/api/slaves?Name=" + name.replace(' ', '+') + "&Data=settings");
    }

    public String getSlavesSettings(List<String> names) {
        String script = "/api/slaves?Data=settings";
        if (names != null) {
            script = script + "&Name=" + DeadlineUtils.ArrayToCommaSeparatedString(names).replace(' ', '+');
        }

        return this.cp.get(script);
    }

    public String saveSlaveSettings(String info) {
        JsonObject jsonData = (new JsonParser()).parse(info).getAsJsonObject();
        String body = "{\"Command\":\"savesettings\", \"SlaveSettings\":" + jsonData + "}";
        return this.cp.put("/api/slaves", body);
    }

    public String deleteSlave(String name) {
        return this.cp.delete("/api/slaves?Name=" + name.replace(' ', '+'));
    }

    public String addGroupToSlave(String slave, String group) {
        JsonObject jsonDataSlave = (new JsonParser()).parse(slave).getAsJsonObject();
        JsonObject jsonDataGroup = (new JsonParser()).parse(group).getAsJsonObject();
        String body = "{\"Slave\":" + jsonDataSlave + ", \"Group\":" + jsonDataGroup + '}';
        return this.cp.put("/api/groups", body);
    }

    public String addPoolToSlave(String slave, String pool) {
        JsonObject jsonDataSlave = (new JsonParser()).parse(slave).getAsJsonObject();
        JsonObject jsonDataPool = (new JsonParser()).parse(pool).getAsJsonObject();
        String body = "{\"Slave\":" + jsonDataSlave + ", \"Pool\":" + jsonDataPool + '}';
        return this.cp.put("/api/pools", body);
    }

}
