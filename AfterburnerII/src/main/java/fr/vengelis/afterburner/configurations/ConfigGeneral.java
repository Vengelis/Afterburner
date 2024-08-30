package fr.vengelis.afterburner.configurations;

import fr.vengelis.afterburner.providers.IAfterburnerProvider;
import fr.vengelis.afterburner.providers.ProviderInstructions;

import java.util.HashMap;


/**
 *
 * TODO : - Remplacer cette enum par un gestionnaire d'objets Settings
 *        - Fusionner les enums de settings et privilégier des catégories
 *        - Chaque module du projet doit être catégorisé en module qui ira chercher dans la liste des settings ses settings
 *          (exemple : pour redis, faire en sorte que ça soit un module avec sa catégorie)
 *        - Faire en sorte qu'Afterburner enregistre dynamiquement les valeurs des configs de manière dynamique
 *        - Rebase le projet sur ce système
 *
 * */
public enum ConfigGeneral {
    CONFIG_VERSION(null),
    READY(null),

    PATH_RENDERING_DIRECTORY(null),
    PATH_TEMPLATE(null),
    PATH_WORLDS_BATCHED(null),
    PATH_COMMON_FILES(null),
    PATH_JAVA(null),

    QUERY_AUTO_BIND(null),
    QUERY_HOST(null),
    QUERY_PORT(null),
    QUERY_PASSWORD(null),

    QUERY_BROADCASTER_ENABLED(null),
    QUERY_BROADCASTER_HOST(null),
    QUERY_BROADCASTER_PORT(null),
    QUERY_BROADCASTER_TOKEN(null),
    QUERY_BROADCASTER_HTTPS(null),

    REDIS_ENABLED(null),
    REDIS_HOST(null),
    REDIS_PORT(null),
    REDIS_USER(null),
    REDIS_PASSWORD(null),
    REDIS_DATABASE(null),

    PROVIDERS(new HashMap<ProviderInstructions, IAfterburnerProvider>()),
    ;

    private Object data;

    ConfigGeneral(Object i) {
        data = i;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isDeprecated(int version) {
        return version != 3;
    }
}
