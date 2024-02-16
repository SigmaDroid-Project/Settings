
package com.android.settings.deviceinfo;

import android.os.SystemProperties;

public class VersionUtils {
    public static String getSigmaVersion(){
        return SystemProperties.get("org.sigma.version.display","");
    }
}
