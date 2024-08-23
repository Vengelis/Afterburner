package fr.vengelis.afterburner.commonfiles;

import java.io.IOException;

public interface BaseCommonFile {

    String getName();
    boolean isEnabled();
    void setEnabled(boolean value);
    void copy() throws IOException;

}
