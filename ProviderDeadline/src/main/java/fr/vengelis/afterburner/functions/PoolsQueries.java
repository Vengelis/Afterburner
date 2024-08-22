/**
 * Created by Vengelis_.
 * Date: 2/24/2022
 * Time: 3:58 AM
 * Project: DeadlineAPI
 */

package fr.vengelis.afterburner.functions;



import fr.vengelis.afterburner.handler.ConnectionProperty;
import fr.vengelis.afterburner.utils.DeadlineUtils;

import java.util.List;

public class PoolsQueries {

    private ConnectionProperty cp;

    public PoolsQueries(ConnectionProperty cp) {
        this.cp = cp;
    }

    public String getPoolNames() {
        return this.cp.get("/api/pools");
    }

    public String addPool(String name) {
        String body = "{\"Pool\":\"" + name + "\"}";
        return this.cp.post("/api/pools", body);
    }

    public String addPools(List<String> names) {
        String body = "{\"Pool\":" + DeadlineUtils.ArrayToCommaSeparatedString(names) + '}';
        return this.cp.post("/api/pools", body);
    }

    public String purgePools(String replacementPool, List<String> pools, Boolean overwrite) {
        String body = "{\"ReplacementPool\":\"" +replacementPool+ "\", \"Pool\":" + DeadlineUtils.ArrayToCommaSeparatedString(pools) + ", \"OverWrite\":" +overwrite+'}';
        return this.cp.put("/api/pools", body);
    }

    public String deletePool(String name) {
        return this.cp.delete("/api/pools?Pool=" + name.replace(' ', '+'));
    }

    public String deletePools(List<String> names) {
        return this.cp.delete("/api/pools?Pool=" + DeadlineUtils.ArrayToCommaSeparatedString(names).replace(' ', '+'));
    }
}
