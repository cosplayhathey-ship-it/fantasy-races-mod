package com.cosplayhathey.fantasyracesmod.ai.provider;

import com.cosplayhathey.fantasyracesmod.FantasyRacesMod;
import com.cosplayhathey.fantasyracesmod.ai.provider.AIProvider;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

/**
 * Player2AppProvider: calls an external Player2-like app over HTTP using a configurable URL.
 *
 * Configuration file: config/fantasy_races_mod/player2app.properties
 * Keys:
 *  enabled=true|false
 *  url=http://127.0.0.1:4315
 *  path=/api/v1/chat
 *  authType=none|bearer|header
 *  authValue=<token or header value>
 */
public class Player2AppProvider implements AIProvider {
    private static final String CONFIG_DIR = "config/fantasy_races_mod";
    private static final String CONFIG_FILE = "player2app.properties";

    private boolean enabled = false;
    private String baseUrl = "http://127.0.0.1:4315";
    private String path = "/api/v1/chat";
    private String authType = "none";
    private String authValue = "";
    private int timeoutMs = 15000;

    public Player2AppProvider() {
        loadConfig();
    }

    private void loadConfig() {
        try {
            Path dir = Path.of(CONFIG_DIR);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path file = dir.resolve(CONFIG_FILE);
            Properties p = new Properties();
            if (!Files.exists(file)) {
                p.setProperty("enabled", "false");
                p.setProperty("url", "http://127.0.0.1:4315");
                p.setProperty("path", "/api/v1/chat");
                p.setProperty("authType", "none");
                p.setProperty("authValue", "");
                try (OutputStream os = Files.newOutputStream(file)) {
                    p.store(os, "Player2AppProvider configuration: enable and set url/path and optional auth");
                }
            } else {
                try (FileInputStream fis = new FileInputStream(file.toFile())) {
                    p.load(fis);
                }
            }
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
            baseUrl = p.getProperty("url", baseUrl);
            path = p.getProperty("path", path);
            authType = p.getProperty("authType", "none").toLowerCase();
            authValue = p.getProperty("authValue", "");
        } catch (IOException e) {
            FantasyRacesMod.LOGGER.warn("Player2AppProvider: failed to load config", e);
            enabled = false;
        }
    }

    @Override
    public boolean isAvailable() {
        return enabled && baseUrl != null && !baseUrl.isBlank();
    }

    @Override
    public String getResponse(String npcId, String playerMessage, List<String> history) {
        if (!isAvailable()) return null;
        HttpURLConnection conn = null;
        try {
            URL url = new URL(baseUrl + path);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if ("bearer".equals(authType)) {
                conn.setRequestProperty("Authorization", "Bearer " + authValue);
            } else if ("header".equals(authType) && authValue != null && !authValue.isBlank()) {
                // expect authValue in form "HeaderName:HeaderValue"
                int idx = authValue.indexOf(":");
                if (idx > 0) {
                    String hn = authValue.substring(0, idx).trim();
                    String hv = authValue.substring(idx + 1).trim();
                    conn.setRequestProperty(hn, hv);
                }
            }

            // Build simple JSON: { "prompt": "...", "history": [ ... ] }
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append("\"prompt\":");
            // prompt includes npc id + history + player message
            StringBuilder prompt = new StringBuilder();
            prompt.append("NPC:").append(npcId).append("\n");
            if (history != null) {
                for (String h : history) {
                    prompt.append(h).append("\n");
                }
            }
            prompt.append("Player: ").append(playerMessage).append("\n");
            sb.append('"').append(escapeJson(prompt.toString())).append('"');
            sb.append(',');
            sb.append("\"history\":");
            sb.append('[');
            if (history != null) {
                boolean first = true;
                for (String h : history) {
                    if (!first) sb.append(',');
                    sb.append('"').append(escapeJson(h)).append('"');
                    first = false;
                }
            }
            sb.append(']');
            sb.append('}');

            byte[] out = sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(out.length);
            conn.connect();
            try (OutputStream os = conn.getOutputStream()) {
                os.write(out);
            }

            int code = conn.getResponseCode();
            BufferedReader br;
            if (code >= 200 && code < 300) br = new BufferedReader(new InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            else br = new BufferedReader(new InputStreamReader(conn.getErrorStream() != null ? conn.getErrorStream() : conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) resp.append(line).append('\n');
            String body = resp.toString().trim();
            if (body.isEmpty()) return null;
            // Try to parse very simply: look for {"reply":"..."} or {"choices":[{"text":"..."}]}
            String reply = parseReplyFromJson(body);
            return reply;
        } catch (Throwable t) {
            FantasyRacesMod.LOGGER.warn("Player2AppProvider: error calling external app", t);
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private static String parseReplyFromJson(String json) {
        // naive parsing: look for "reply":"..."
        int idx = json.indexOf("\"reply\"");
        if (idx >= 0) {
            int colon = json.indexOf(':', idx);
            if (colon > 0) {
                int start = json.indexOf('"', colon + 1);
                if (start >= 0) {
                    int end = json.indexOf('"', start + 1);
                    if (end > start) return json.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"");
                }
            }
        }
        // look for choices[0].text
        idx = json.indexOf("\"choices\"");
        if (idx >= 0) {
            int txt = json.indexOf("\"text\"", idx);
            if (txt >= 0) {
                int colon = json.indexOf(':', txt);
                if (colon > 0) {
                    int start = json.indexOf('"', colon + 1);
                    if (start >= 0) {
                        int end = json.indexOf('"', start + 1);
                        if (end > start) return json.substring(start + 1, end).replace("\\n", "\n").replace("\\\"", "\"");
                    }
                }
            }
        }
        // fallback: return whole body
        return json;
    }
}
