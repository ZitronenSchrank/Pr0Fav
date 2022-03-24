package Pr0Fav;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JPlaceHolderPassword extends JPasswordField implements FocusListener {
    private static final long serialVersionUID = 1L;
    private String placeholder;
    private boolean isEmpty = true;
    private char echoChar = '*';

    JPlaceHolderPassword() {
        super.addFocusListener(this);
    }

    public JPlaceHolderPassword(final String pText) {
        super(pText);
        isEmpty = false;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    void setPlaceholder(final String s) {
        placeholder = s;
        if(isEmpty){
            this.setEchoChar((char)0);
            this.setForeground(Color.GRAY);
            this.setText(placeholder);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (isEmpty) {
            this.setText("");
            this.setForeground(Color.BLACK);
            this.setEchoChar(echoChar);
            isEmpty = false;

        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getPassword().length == 0) {
            this.setForeground(Color.GRAY);
            this.setText(placeholder);
            echoChar = this.getEchoChar();
            this.setEchoChar((char)0);
            isEmpty = true;
        }
    }

    @Override
    public char[] getPassword() {
        if(isEmpty){
            return new char[0];
        } else {
            return super.getPassword();
        }
    }

    @Override
    public void setEchoChar(char c) {
        super.setEchoChar(c);
    }

    @Override
    public char getEchoChar() {
        return super.getEchoChar();
    }
}
