/*
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2012 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.ui.icon;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Animated icon of a spinning dial used to notify users that an application is performing a getTask.
 * <p>
 * This behaves as any animated icon except for one thing: when the animation is stopped using
 * {@link #setAnimated(boolean)}, the dial won't be displayed anymore until the animation is
 * resumed.
 *
 * <p>
 * This heavily borrows code from Technomage's <code>furbelow</code> package, distributed
 * under the GNU Lesser General Public License.<br>
 * The original source code can be found <a href="http://furbelow.svn.sourceforge.net/viewvc/furbelow/trunk/src/furbelow">here</a>.
 *
 * @author twall, Nicolas Rinaudo
 */
public class SpinningDial extends AnimatedIcon {
    /** Default creation animation status. */
    public  static final boolean DEFAULT_ANIMATE    = false;
    /** Dial's default color. */
    public  static final Color DEFAULT_COLOR        = Color.BLACK;
    /** Minimum alpha-transparency value that must be applied to the dial's color as it fades out. */
    private static final int   MIN_ALPHA            = 32;
    /** Icon's default width and height. */
    public  static final int   DEFAULT_SIZE         = 16;
    /** Default number of spokes in the dial. */
    public  static final int   DEFAULT_SPOKES       = 12;
    /** Dial's full size, will be scaled down at paint time. */
    private static final int   FULL_SIZE            = 256;
    /** Width of each of the dial's strokes. */
    private static final float DEFAULT_STROKE_WIDTH = FULL_SIZE / 10f;
    /** Scale down factor for the dial. */
    private static final float FRACTION             = 0.6f;



    /** Icon's width. */
    private final int     width;
    /** Icon's height. */
    private final int     height;
    /** All images that compose the spinning dial. */
    private final Image[] frames;
    /** Color used to paint the dial. */
    private Color   color;
    /** Width of each stroke. */
    private float   strokeWidth;



    /**
     * Creates a new spinning dial.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     *
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     */
    public SpinningDial() {this(DEFAULT_SIZE, DEFAULT_SIZE);}

    /**
     * Creates a new spinning dial.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     *
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(boolean animate) {this(DEFAULT_SIZE, DEFAULT_SIZE, animate);}

    /**
     * Creates a new spinning dial with the specified color.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     *
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     *
     * @param c color in which to paint the dial.
     */
    public SpinningDial(Color c) {this(DEFAULT_SIZE, DEFAULT_SIZE, c);}

    /**
     * Creates a new spinning dial with the specified color.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_SIZE} for its width and height.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     *
     * @param c       color in which to paint the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(Color c, boolean animate) {this(DEFAULT_SIZE, DEFAULT_SIZE, c, animate);}

    /**
     * Creates a new spinning dial with the specified dimensions.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     *
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     *
     * @param w width of the icon.
     * @param h height of the icon.
     */
    public SpinningDial(int w, int h) {this(w, h, DEFAULT_SPOKES);}

    /**
     * Creates a new spinning dial with the specified dimensions.
     * <p>
     * The new instance will be initialised using default values:
     * <ul>
     *   <li>{@link #DEFAULT_COLOR} for its color.</li>
     *   <li>{@link #DEFAULT_SPOKES} for its number of spokes.</li>
     * </ul>
     *
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, boolean animate) {this(w, h, DEFAULT_SPOKES, animate);}

    /**
     * Creates a new spinning dial with the specified dimensions and color.
     * <p>
     * The new instance will use {@link #DEFAULT_SPOKES} for its number of spokes.
     *
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     *
     * @param w width of the icon.
     * @param h height of the icon.
     * @param c color in which to paint the dial.
     */
    public SpinningDial(int w, int h, Color c) {this(w, h, DEFAULT_SPOKES, c);}

    /**
     * Creates a new spinning dial with the specified dimensions and color.
     * <p>
     * The new instance will use {@link #DEFAULT_SPOKES} for its number of spokes.
     *
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param c       color in which to paint the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, Color c, boolean animate) {this(w, h, DEFAULT_SPOKES, c, animate);}

    /**
     * Creates a new spinning dial with the specified dimensions and number of spokes.
     * <p>
     * The new instance will use {@link #DEFAULT_COLOR} for its color.
     *
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     *
     * @param w      width of the icon.
     * @param h      height of the icon.
     * @param spokes number of spokes that compose the dial.
     */
    public SpinningDial(int w, int h, int spokes) {this(w, h, spokes, DEFAULT_COLOR);}

    /**
     * Creates a new spinning dial with the specified dimensions and number of spokes.
     * <p>
     * The new instance will use {@link #DEFAULT_COLOR} for its color.
     *
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param spokes  number of spokes that compose the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, int spokes, boolean animate) {this(w, h, spokes, DEFAULT_COLOR, animate);}

    /**
     * Creates a new spinning dial with the specified characteristics.
     * <p>
     * A dial created that way will not be displayed until {@link #setAnimated(boolean)} is
     * called to animate it.
     *
     * @param w      width of the icon.
     * @param h      height of the icon.
     * @param spokes number of spokes that compose the dial.
     * @param c      color in which to paint the dial.
     */
    public SpinningDial(int w, int h, int spokes, Color c) {this(w, h, spokes, c, DEFAULT_ANIMATE);}

    /**
     * Creates a new spinning dial with the specified characteristics.
     * @param w       width of the icon.
     * @param h       height of the icon.
     * @param spokes  number of spokes that compose the dial.
     * @param c       color in which to paint the dial.
     * @param animate whether to animate the dial immediately or not.
     */
    public SpinningDial(int w, int h, int spokes, Color c, boolean animate) {
        super(spokes, 1000 / spokes);

        // Initialises the icon.
        width       = w;
        height      = h;
        color       = c;
        frames      = new Image[getFrameCount()];
        strokeWidth = DEFAULT_STROKE_WIDTH;

        // Animates the icon if necessary.
        if(animate)
            setAnimated(true);
    }

    /**
     * Sets the width of the strokes used to paint each of the dial's spokes.
     * @param width width of the strokes used to paint each of the dial's spokes.
     */
    public synchronized void setStrokeWidth(float width) {strokeWidth = width;}

    /**
     * Returns the width of the strokes used to paint each of the dial's spokes.
     * @return the width of the strokes used to paint each of the dial's spokes.
     */
    public synchronized float getStrokeWidth() {return strokeWidth;}



    // - Color management ----------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Sets the color used to draw the dial.
     * @param c color in which to paint the dial.
     */
    public synchronized void setColor(Color c) {
        // Ignores calls that don't actually change anything.
        if(!color.equals(c)) {
            color = c;

            // Resets stored images to make sure they get repainted
            // with the right color.
            for(int i = 0; i < frames.length; i++)
                frames[i] = null;
        }
    }

    /**
     * Returns the color used to paint the dial.
     * @return the color used to paint the dial.
     */
    public synchronized Color getColor() {return color;}

    /**
     * Computes the dial color according to the specified alpha-transparency value.
     * @param alpha transparency value that must be applied to the dial's color.
     */
    protected Color getSpokeColor(int alpha) {return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(MIN_ALPHA, alpha));}



    // - Size methods --------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns the icon's height.
     * @return the icon's height.
     */
    @Override
    public int getIconHeight() {return height;}

    /**
     * Returns the icon's width.
     * @return the icon's width.
     */
    @Override
    public int getIconWidth() {return width;}



    // - Rendering methods ---------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Initialises graphics for painting one of the dial's frames.
     * @param graphics graphics instance to initialise.
     */
    private void initialiseGraphics(Graphics2D graphics) {
        float scale;

        scale = (float)Math.min(width, height) / FULL_SIZE;

        graphics.setComposite(AlphaComposite.Clear);
        graphics.fillRect(0, 0, width, height);
        graphics.setComposite(AlphaComposite.Src);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics.translate((float)width / 2, (float)height / 2);
        graphics.scale(scale, scale);
    }

    /**
     * Paints the current frame on the specified component.
     * @param c        component on which to paint the dial.
     * @param graphics graphic context to use when painting the dial.
     * @param x        horizontal coordinate at which to paint the dial.
     * @param y        vertical coordinate at which to paint the dial.
     */
    @Override
    public synchronized void paintFrame(Component c, Graphics graphics, int x, int y) {
        int currentFrame;

        // Ignores paint calls while not animated.
        if(isAnimated()) {
            // Checks whether the current frame has already been generated or not, generates
            // it if not.
            if((frames[currentFrame = getFrame()]) == null) {
                // Initialises the frame.
                // Note: getGraphicsConfiguration() returns null if the component has not yet been added to a container
                GraphicsConfiguration gc = c != null ? c.getGraphicsConfiguration() : null;
                Image frame;
                if (gc != null)
                    frame = gc.createCompatibleImage(width, height, Transparency.TRANSLUCENT);
                else
                    frame = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

                // Initialises the frame's g.
                Graphics2D g = (Graphics2D)frame.getGraphics();
                initialiseGraphics(g);

                // Draws each spoke in the dial.
                int alpha  = 255;
                int radius = FULL_SIZE / 2 - 1 - (int)(strokeWidth / 2);
                for (int i = 0; i < getFrameCount(); i++) {
                    double cos = Math.cos((Math.PI * 2) - (Math.PI * 2 * (i - currentFrame)) / getFrameCount());
                    double sin = Math.sin((Math.PI * 2) - (Math.PI * 2 * (i - currentFrame)) / getFrameCount());

                    g.setColor(getSpokeColor(alpha));
                    g.drawLine((int)(radius * FRACTION * cos), (int)(radius * FRACTION * sin),
                               (int)(radius * cos), (int)(radius * sin));
                    alpha = Math.max(MIN_ALPHA, (alpha * 3) / 4);
                }
                g.dispose();

                // Stores the newly generated frame.
                frames[currentFrame] = frame;
            }

            // Draws the current frame.
            graphics.drawImage(frames[currentFrame], x, y, null);
        }
    }

    /**
     * Starts / stops the spinning dial.
     * <p>
     * If <code>a</code> is <code>false</code>, the animation will stop and the
     * the dial won't be displayed anymore until the animation resumes.
     *
     * @param a whether to start or stop the animation.
     */
    @Override
    public void setAnimated(boolean a) {
        super.setAnimated(a);

        // Makes sure the dial disapears when the animation is stopped.
        if (!a) {
            repaint();
        }
    }
}
