package com.trophonix.txt;

import javax.swing.*;
import java.awt.*;

public class CustomMenuBar extends JMenuBar {

    @Override
    public void setForeground(Color fg) {
        setForegroundRecursively(this, fg);
        super.setForeground(fg);
    }

    @Override
    public void setBackground(Color bg) {
        setBackgroundRecursively(this, bg);
        super.setBackground(bg);
    }

    private static void setForegroundRecursively(Component parent, Color fg) {
        if (parent instanceof Container) {
            for (Component c: ((Container)parent).getComponents()) {
                c.setForeground(fg);
                setForegroundRecursively(c, fg);
            }
            if (parent instanceof JMenu) {
                JMenu jMenu = (JMenu)parent;
                for (Component item : jMenu.getMenuComponents()) {
                    item.setForeground(fg);
                    setForegroundRecursively(item, fg);
                }
            }
        }
    }

    private static void setBackgroundRecursively(Object parent, Color bg) {
        if (parent instanceof Container) {
            for (Component c: ((Container)parent).getComponents()) {
                c.setBackground(bg);
                setBackgroundRecursively(c, bg);
            }
            if (parent instanceof JMenu) {
                JMenu jMenu = (JMenu)parent;
                for (Component item : jMenu.getMenuComponents()) {
                    item.setBackground(bg);
                    setBackgroundRecursively(item, bg);
                }
            }
        }
    }

}
