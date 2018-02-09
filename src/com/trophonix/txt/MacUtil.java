package com.trophonix.txt;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MacUtil {


    //FullScreenUtilities.setWindowCanFullScreen(window, true);
    static void enableOSXFullscreen(Window window) {
        try {
            Class util = Class.forName("com.apple.eawt.FullScreenUtilities");
            Class params[] = new Class[]{Window.class, Boolean.TYPE};
            Method method = util.getMethod("setWindowCanFullScreen", params);
            method.invoke(util, window, true);
        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException exp) {
            exp.printStackTrace(System.err);
        }
    }

    //Application.getApplication().requestToggleFullScreen(window);
    static void toggleOSXFullscreen(Window window) {
        try {
            Class application = Class.forName("com.apple.eawt.Application");
            Method getApplication = application.getMethod("getApplication");
            Object instance = getApplication.invoke(application);
            Method method = application.getMethod("requestToggleFullScreen", Window.class);
            method.invoke(instance, window);
        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException exp) {
            exp.printStackTrace(System.err);
        }
    }

    //Application.getApplication().setQuitStrategy(QuitStrategy.CLOSE_ALL_WINDOWS);
    static void enableOSXQuitStrategy() {
        try {
            Class application = Class.forName("com.apple.eawt.Application");
            Method getApplication = application.getMethod("getApplication");
            Object instance = getApplication.invoke(application);
            Class strategy = Class.forName("com.apple.eawt.QuitStrategy");
            Enum closeAllWindows = Enum.valueOf(strategy, "CLOSE_ALL_WINDOWS");
            Method method = application.getMethod("setQuitStrategy", strategy);
            method.invoke(instance, closeAllWindows);
        } catch (ClassNotFoundException | NoSuchMethodException |
                SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException exp) {
            exp.printStackTrace(System.err);
        }
    }

}
