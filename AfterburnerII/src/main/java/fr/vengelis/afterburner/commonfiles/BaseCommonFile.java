package fr.vengelis.afterburner.commonfiles;

import java.io.IOException;

public interface BaseCommonFile {

    String getName();
    boolean isEnabled();
    void copy() throws IOException;

}
