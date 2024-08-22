/**
 * Created by Vengelis_.
 * Date: 1/6/2023
 * Time: 12:16 AM
 * Project: Lunatrix
 */

package fr.vengelis.afterburner.handler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DeadlineSend {

    public static String send(String address, String message, String requestType, String body, Boolean useAuth, String username, String password, Boolean useTls, String caCert, Boolean insecure) {
        try {
            String httpString;
            if (useTls) {
                httpString = "https://";
            } else {
                httpString = "http://";
            }

            if (!address.startsWith(httpString)) {
                address = httpString + address;
            }

            URL url = new URL(address + message);
            HttpURLConnection connexion = (HttpURLConnection)url.openConnection();
            connexion.setRequestMethod(requestType);

            //  TODO : Auth method
//            if (useAuth) {
//                String userPassword = username + ":" + password;
//                byte[] userPasswordEncoded = Base64.encodeBase64(userPassword.getBytes(StandardCharsets.UTF_8));
//                String authHeader = "Basic " + new String(userPasswordEncoded);
//                connexion.setRequestProperty("Authorization", authHeader);
//            }

            if (body != null) {
                connexion.setRequestProperty("Content-Type", "application/json");
                connexion.setDoOutput(true);
                OutputStream os = connexion.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
                osw.write(body);
                osw.close();
                os.close();
            }

            if (useTls) {
                return "Error: The protocol TLS was not supported on this version of DeadlineAPI";
            } else {
                connexion.connect();
                InputStream is = connexion.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder();

                String line;
                while((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }

                rd.close();

                return response.toString();
            }
        } catch (Exception var17) {
            return var17.getMessage();
        }
    }

}
