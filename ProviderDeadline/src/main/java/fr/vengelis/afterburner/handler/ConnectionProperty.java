/**
 * Created by Vengelis_.
 * Date: 1/6/2023
 * Time: 12:39 AM
 * Project: Lunatrix
 */

package fr.vengelis.afterburner.handler;

public class ConnectionProperty {

    private String address;
    private Boolean useAuth;
    private String user;
    private String password;
    private Boolean useTls;
    private String caCert;
    private Boolean insecure;

    public ConnectionProperty(String address) {
        this.address = address;
        this.useAuth = false;
        this.useTls = false;
    }

    public ConnectionProperty(String address, Boolean useAuth, String user, String password) {
        this.address = address;
        this.useAuth = useAuth;
        this.user = user;
        this.password = password;
        this.useTls = false;
    }

    public ConnectionProperty(String address, Boolean useAuth, String user, String password, Boolean useTls, String caCert, Boolean insecure) {
        this.address = address;
        this.useAuth = useAuth;
        this.user = user;
        this.password = password;
        this.useTls = useTls;
        this.caCert = caCert;
        this.insecure = insecure;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAuthentification() {
        return this.user + ":" + this.password;
    }

    public void setAuthentification(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public Boolean authentificationEnabled() {
        return this.useAuth;
    }

    public void EnableAuthentification(Boolean enable) {
        this.useAuth = enable;
    }

    public String get(String command) {
        return DeadlineSend.send(this.address, command, "GET", (String)null, this.useAuth, this.user, this.password, this.useTls, this.caCert, this.insecure);
    }

    public String put(String command, String body) {
        return DeadlineSend.send(this.address, command, "PUT", body, this.useAuth, this.user, this.password, this.useTls, this.caCert, this.insecure);
    }

    public String delete(String command) {
        return DeadlineSend.send(this.address, command, "DELETE", (String)null, this.useAuth, this.user, this.password, this.useTls, this.caCert, this.insecure);
    }

    public String post(String command, String body) {
        return DeadlineSend.send(this.address, command, "POST", body, this.useAuth, this.user, this.password, this.useTls, this.caCert, this.insecure);
    }

}
