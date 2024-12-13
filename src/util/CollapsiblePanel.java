// from http://www.coderanch.com/t/341737/GUI/java/Expand-Collapse-Panels

package util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

public class CollapsiblePanel extends JPanel {
	/** Comment for <code>serialVersionUID</code>. */
    private static final long serialVersionUID = -7507196157581324501L;
    
    private boolean selected;
	JPanel contentPanel_;
	HeaderPanel headerPanel_;
	// The width given by the parent which we should try to set as a maximum width for the layout.
	private int setWidth_ = Integer.MAX_VALUE;

	private class HeaderPanel extends JPanel implements MouseListener, FocusListener, KeyListener {
		/** Comment for <code>serialVersionUID</code>. */
        private static final long serialVersionUID = 3553276313551309624L;
        
        String text_;
		Font font;
		BufferedImage open, closed;
		final int OFFSET = 30, PAD = 5;

		public HeaderPanel(String text) {
			addMouseListener(this);
			addKeyListener(this);
			text_ = text;
			getAccessibleContext().setAccessibleName(text);
			font = UIManager.getFont("Label.font").deriveFont(Font.BOLD);
			setFocusable(true);
			addFocusListener(this);
			setPreferredSize(new Dimension(200, 24));
			setMinimumSize(new Dimension(0, 24));
			try {
				closed = ImageIO.read(getClass().getResourceAsStream("/toolbarButtonGraphics/navigation/Forward24.gif"));
				open = ImageIO.read(getClass().getResourceAsStream("/toolbarButtonGraphics/navigation/Down24.gif"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
        protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			int h = getHeight();
			if (selected) {
                g2.drawImage(open, PAD, 0, h, h, this);
            } else {
                g2.drawImage(closed, PAD, 0, h, h, this);
            }
            // Uncomment once you have your own images
			g2.setFont(font);
			FontRenderContext frc = g2.getFontRenderContext();
			LineMetrics lm = font.getLineMetrics(text_, frc);
			float height = lm.getAscent() + lm.getDescent();
			float x = OFFSET;
			float y = (h + height) / 2 - lm.getDescent();
			g2.drawString(text_, x, y);
		}

		@Override
        public void mouseClicked(MouseEvent e) {
			requestFocus();
			toggleSelection();
		}

		@Override
        public void mouseEntered(MouseEvent e) { }

		@Override
        public void mouseExited(MouseEvent e) { }

		@Override
        public void mousePressed(MouseEvent e) { }

		@Override
        public void mouseReleased(MouseEvent e) { }

		@Override
		public void focusGained(FocusEvent focusEvent) {
			setBorder(BorderFactory.createDashedBorder(Color.BLACK));
		}

		@Override
		public void focusLost(FocusEvent focusEvent) {
			setBorder(BorderFactory.createEmptyBorder());
		}

		@Override
		public void keyTyped(KeyEvent keyEvent) {
		}

		@Override
		public void keyPressed(KeyEvent keyEvent) {
			int c = keyEvent.getKeyCode();
			if (c == KeyEvent.VK_SPACE || c==KeyEvent.VK_ENTER) {
				toggleSelection();
			}
		}

		@Override
		public void keyReleased(KeyEvent keyEvent) {
		}
	}

	public CollapsiblePanel(String text, JPanel panel) {
		super(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.anchor = GridBagConstraints.FIRST_LINE_START;

		selected = true;
		headerPanel_ = new HeaderPanel(text == null ? "" : text);

		contentPanel_ = panel;
		contentPanel_.setBorder(new MatteBorder(0,20,0,0,contentPanel_.getBackground()));

		add(headerPanel_, gbc);
		add(contentPanel_, gbc);
		contentPanel_.setVisible(selected);
	}

	public JPanel getHeader() {
		return headerPanel_;
	}

	public void toggleSelection() {
		selected = !selected;
		contentPanel_.setVisible(selected);

		validate();
		javax.swing.JFrame top = (javax.swing.JFrame)getTopLevelAncestor();
		if (top != null) top.pack();

		headerPanel_.repaint();
	}

	public void setExpanded(boolean isExpanded) {
        if (selected != isExpanded) {
            toggleSelection();
        }
    }

    public void setMaximumWidth(int w) {
		setWidth_ = w;
		invalidate();
	}

	@Override
	public Dimension getMaximumSize() {
		Dimension d = super.getPreferredSize();
		d.width = setWidth_;
		return d;
	}
}



