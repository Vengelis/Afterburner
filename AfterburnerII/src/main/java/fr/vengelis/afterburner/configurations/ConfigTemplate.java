package fr.vengelis.afterburner.configurations;

import fr.vengelis.afterburner.commonfiles.BaseCommonFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ConfigTemplate {

    CONFIG_VERSION(null),
    PATTERN_NAME(null),
    MAP_PICKER(new ArrayList<MapPicker>()),
    COMMON_FILES(new HashMap<Class<? extends BaseCommonFile>, List<Object>>()),
    EXECUTABLE_TYPE(null),
    EXECUTABLE_MIN_RAM(null),
    EXECUTABLE_MAX_RAM(null),
    EXECUTABLE_NAME(null),
    EXECUTABLE_MORE_ARGS(new ArrayList<String>()),
    SAVE_ENABLED(null),
    SAVE_WORLDS(new HashMap<String, String>()),
    ;

    public static class MapPicker {

        private final boolean enabled;
        private final String library;
        private final String renameTo;

        public MapPicker(boolean enabled, String library, String renameTo) {
            this.enabled = enabled;
            this.library = library;
            this.renameTo = renameTo;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public String getLibrary() {
            return library;
        }

        public String getRenameTo() {
            return renameTo;
        }
    }

    private Object data;

    ConfigTemplate(Object i) {
        data = i;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public boolean isDeprecated(int version) {
        return version != 1;
    }
}
