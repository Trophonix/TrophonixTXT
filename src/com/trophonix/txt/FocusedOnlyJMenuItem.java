package com.trophonix.txt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FocusedOnlyJMenuItem extends JMenuItem {

    private Component focus;

    FocusedOnlyJMenuItem(String text, Component focus) {
        super(text);
        this.focus = focus;
    }

    @Override
    public void setAccelerator(KeyStroke keyStroke) {
        super.setAccelerator(keyStroke);
        Action action = getActionMap().get("onClick");
        getActionMap().put("onClick", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final KeyboardFocusManager kfm =
                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
                if (kfm.getFocusOwner().equals(focus)) {
                    action.actionPerformed(e);
                }
            }
        });
    }

//    /** {@inheritDoc} */
//    @Override
//    public void addActionListener(ActionListener listener) {
//        super.addActionListener(e -> {
//            final KeyboardFocusManager kfm =
//                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
//            if (kfm.getFocusOwner().equals(focus)) {
//                listener.actionPerformed(e);
//            }
//        });
//    }

}
