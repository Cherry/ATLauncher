/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.UUID;

import com.atlauncher.App;
import com.atlauncher.data.Settings;
import com.atlauncher.data.mojang.auth.AuthenticationRequest;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.data.mojang.auth.InvalidateRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Authentication {
    public static AuthenticationResponse checkAccount(String username, String password) {
        String uuid = UUID.randomUUID() + "";
        Gson gson = new Gson();
        StringBuilder response = null;
        try {
            URL url = new URL("https://authserver.mojang.com/authenticate");
            String request = gson.toJson(new AuthenticationRequest(username, password, uuid));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

            connection.setRequestProperty("Content-Length", "" + request.getBytes().length);

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            DataOutputStream writer = new DataOutputStream(connection.getOutputStream());
            writer.write(request.getBytes(Charset.forName("UTF-8")));
            writer.flush();
            writer.close();

            // Read the result

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } catch (IOException e) {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }
            response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            reader.close();
        } catch (IOException e) {
            App.settings.logStackTrace(e);
            return null;
        }
        AuthenticationResponse result = null;
        if (response != null) {
            try {
                result = gson.fromJson(response.toString(), AuthenticationResponse.class);
            } catch (JsonSyntaxException e) {
                App.settings.logStackTrace(e);
            }
            if (result != null) {
                result.setUUID(uuid);
            }
        }
        return result;
    }
}
