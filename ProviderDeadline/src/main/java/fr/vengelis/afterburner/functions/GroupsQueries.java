/**
 * Created by Vengelis_.
 * Date: 2/24/2022
 * Time: 2:43 AM
 * Project: DeadlineAPI
 */

package fr.vengelis.afterburner.functions;



import fr.vengelis.afterburner.handler.ConnectionProperty;
import fr.vengelis.afterburner.utils.DeadlineUtils;

import java.util.List;

public class GroupsQueries {

    private ConnectionProperty cp;

    public GroupsQueries(ConnectionProperty connectionProperty) {
        this.cp = connectionProperty;
    }

    public String getGroupNames() {
        return cp.get("/api/groups");
    }

    public String addGroup(String name) {
        String body = "{\"Group\":\"" + name + "\"}";
        return cp.post("/api/groups", body);
    }

    public String addGroups(List<String> names) {
        String body = "{\"Group\":\"" + DeadlineUtils.ArrayToCommaSeparatedString(names) + "\"}";
        return cp.post("/api/groups", body);
    }

    public String purgeGroups(String replacementGroup, List<String> groups, Boolean overwrite) {
        String body = "{\"ReplacementGroup\":\"" +replacementGroup+ "\", \"Group\":" + DeadlineUtils.ArrayToCommaSeparatedString(groups) + ", \"OverWrite\":" + overwrite +'}';
        return cp.put("/api/groups", body);
    }

    public String deleteGroup(String name) {
        return cp.delete("/api/groups?Group=" + name.replace(' ', '+'));
    }

    public String deleteGroups(List<String> names) {
        return cp.delete("/api/groups?Group=" + DeadlineUtils.ArrayToCommaSeparatedString(names).replace(' ', '+'));
    }
}
