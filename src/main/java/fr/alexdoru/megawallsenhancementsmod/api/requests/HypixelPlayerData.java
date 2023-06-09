package fr.alexdoru.megawallsenhancementsmod.api.requests;

import com.google.gson.JsonObject;
import fr.alexdoru.megawallsenhancementsmod.api.HttpClient;
import fr.alexdoru.megawallsenhancementsmod.api.apikey.HypixelApiKeyUtil;
import fr.alexdoru.megawallsenhancementsmod.api.exceptions.ApiException;
import fr.alexdoru.megawallsenhancementsmod.utils.JsonUtil;

public class HypixelPlayerData {

    private final JsonObject playerData;
    private final String uuid;

    public HypixelPlayerData(String uuid) throws ApiException {
        final HttpClient httpClient = new HttpClient("https://api.hypixel.net/player?key=" + HypixelApiKeyUtil.getApiKey() + "&uuid=" + uuid);
        final JsonObject obj = httpClient.getJsonResponse();
        final JsonObject playerdata = JsonUtil.getJsonObject(obj, "player");
        if (playerdata == null) {
            throw new ApiException("This player never joined Hypixel, it might be a nick.");
        }
        this.playerData = playerdata;
        this.uuid = uuid;
    }

    public JsonObject getPlayerData() {
        return this.playerData;
    }

    public String getUuid() {
        return this.uuid;
    }
}
