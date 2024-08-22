/**
 * Created by Vengelis_.
 * Date: 1/6/2023
 * Time: 12:46 AM
 * Project: Lunatrix
 */

package fr.vengelis.afterburner.handler;

import fr.vengelis.afterburner.functions.GroupsQueries;
import fr.vengelis.afterburner.functions.JobsQueries;
import fr.vengelis.afterburner.functions.PoolsQueries;
import fr.vengelis.afterburner.functions.SlavesQueries;

public class DeadlineConnect {

    private ConnectionProperty cp;
    private final JobsQueries jobs;
    private final SlavesQueries slaves;
    private final GroupsQueries groups;
    private final PoolsQueries pools;

//    TODO : Limits Queries

    public DeadlineConnect(String host, Integer port) {
        String address = host + ":" + port.toString();
        this.cp = new ConnectionProperty(address);
        this.jobs = new JobsQueries(this.cp);
        this.slaves = new SlavesQueries(this.cp);
        this.groups = new GroupsQueries(this.cp);
        this.pools = new PoolsQueries(this.cp);
    }
    public JobsQueries getJobsQueries() {
        return this.jobs;
    }

    public SlavesQueries getSlavesQueries() {
        return this.slaves;
    }

    public GroupsQueries getGroupsQueries() {
        return groups;
    }

    public PoolsQueries getPoolsQueries() {
        return pools;
    }
}
