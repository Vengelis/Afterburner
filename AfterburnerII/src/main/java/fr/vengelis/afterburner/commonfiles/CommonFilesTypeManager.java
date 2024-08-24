package fr.vengelis.afterburner.commonfiles;

import fr.vengelis.afterburner.commonfiles.impl.minecraftserver.McPlugins;
import fr.vengelis.afterburner.commonfiles.impl.minecraftserver.McWorlds;
import fr.vengelis.afterburner.commonfiles.impl.minecraftserver.ServerFiles;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.handler.SuperPreInitHandler;

import java.util.*;

/**
 * This class manages the types of common files in the application.
 * It contains a list of Class objects that extend the BaseCommonFile class.
 * The list is unmodifiable and can only be modified by adding new common file types.
 * <p>
 * The CommonFile system is used to transfer so-called "common" files such as a plugin or a map to the server folder.
 * <p>
 * By default, common file types are available in Afterburner such as McPlugins or McWorlds.
 * It is possible to integrate your own common files to complete the automatic sending of files/folders in the template system
 * <p>
 * It provides four public methods:
 * <ul>
 *     <li>register(Class<? extends BaseCommonFile> commonFileObject): This method adds a new common file type to the 'commonFilesType' list.</li>
 *     <li>get(): This method returns an unmodifiable list of the 'commonFilesType' property.</li>
 *     <li>get(String className): This method returns a Class object from the 'commonFilesType' list that matches the provided class name. If no match is found, it returns null.</li>
 * </ul>
 */
public class CommonFilesTypeManager implements SuperPreInitHandler {

    private final List<Class<? extends BaseCommonFile>> commonFilesType = new ArrayList<>();
    private boolean alreadyInit = false;

    public CommonFilesTypeManager() {
        HandlerRecorder.get().register(this);
    }

    public void init() {
        if(alreadyInit) return;
        alreadyInit = true;
        register(McPlugins.class);
        register(McWorlds.class);
        register(ServerFiles.class);
    }

    /**
     * This method adds a new common file type to the 'commonFilesType' list.
     * @param commonFileObject Extended class of AbstractBCF class.
     */
    public void register(Class<? extends BaseCommonFile> commonFileObject) {
        commonFilesType.add(commonFileObject);
    }

    /**
     * This method returns an unmodifiable list of the 'commonFilesType' property.
     * @return List
     */
    public List<Class<? extends BaseCommonFile>> get() {
        return Collections.unmodifiableList(commonFilesType);
    }

    /**
     * This method returns a Class object from the 'commonFilesType' list that matches the provided class name. If no match is found, it returns null.
     * @param className Extended class of AbstractBCF class.
     * @return Class extended of AbstractBCF class
     */
    public Class<? extends BaseCommonFile> get(String className) {
        return commonFilesType.stream()
                .filter(commonFileType -> commonFileType.getSimpleName().equalsIgnoreCase(className))
                .findFirst()
                .orElse(null);
    }

}
