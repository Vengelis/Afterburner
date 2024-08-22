/**
 * Created by Vengelis_.
 * Date: 1/6/2023
 * Time: 12:40 AM
 * Project: Lunatrix
 */

package fr.vengelis.afterburner.utils;

import java.util.Iterator;
import java.util.List;

public class DeadlineUtils {

    public static String ArrayToCommaSeparatedString(List<String> iterable) {
        if (iterable.isEmpty()) {
            return "";
        } else {
            String returnable = "";

            String item;
            for(Iterator var2 = iterable.iterator(); var2.hasNext(); returnable = returnable + item + ",") {
                item = (String)var2.next();
            }

            return returnable;
        }
    }

}
