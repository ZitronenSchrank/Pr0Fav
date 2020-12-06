import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class JPlaceHolderTextField extends JTextField implements FocusListener {

    private String placeholder;
    private boolean isEmpty = true;

    public JPlaceHolderTextField() {
        super.addFocusListener(this);
    }

    public JPlaceHolderTextField(final String pText) {
        super(pText);
        isEmpty = false;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
        if(isEmpty){
            this.setForeground(Color.GRAY);
            super.setText(placeholder);;
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (isEmpty) {
            super.setText("");
            this.setForeground(Color.BLACK);
            isEmpty = false;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (this.getText().isEmpty()) {
            this.setForeground(Color.GRAY);
            super.setText(placeholder);
            isEmpty = true;
        }
    }

    @Override
    public String getText() {
        if(isEmpty){
            return "";
        } else {
            return super.getText();
        }

    }

    @Override
    public void setText(String t) {
        isEmpty = false;
        super.setText(t);
    }
}
