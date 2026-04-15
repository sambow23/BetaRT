/*
 * Decompiled with CFR 0.152.
 */
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferStrategy;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class bd
extends Canvas
implements KeyListener,
MouseListener,
MouseMotionListener,
Runnable {
    private int a = 0;
    private int b = 2;
    private boolean c = true;
    private fd d;
    private File e;
    private boolean f = true;
    private List g = Collections.synchronizedList(new LinkedList());
    private re[][] h = new re[64][64];
    private int i;
    private int j;
    private int k;
    private int l;

    public File a() {
        if (this.e == null) {
            this.e = this.a("minecraft");
        }
        return this.e;
    }

    public File a(String string) {
        File file;
        String string2 = System.getProperty("user.home", ".");
        switch (bd.e()) {
            case a: 
            case b: {
                file = new File(string2, '.' + string + '/');
                break;
            }
            case c: {
                String string3 = System.getenv("APPDATA");
                if (string3 != null) {
                    file = new File(string3, "." + string + '/');
                    break;
                }
                file = new File(string2, '.' + string + '/');
                break;
            }
            case d: {
                file = new File(string2, "Library/Application Support/" + string);
                break;
            }
            default: {
                file = new File(string2, string + '/');
            }
        }
        if (!file.exists() && !file.mkdirs()) {
            throw new RuntimeException("The working directory could not be created: " + file);
        }
        return file;
    }

    private static jn e() {
        String string = System.getProperty("os.name").toLowerCase();
        if (string.contains("win")) {
            return jn.c;
        }
        if (string.contains("mac")) {
            return jn.d;
        }
        if (string.contains("solaris")) {
            return jn.b;
        }
        if (string.contains("sunos")) {
            return jn.b;
        }
        if (string.contains("linux")) {
            return jn.a;
        }
        if (string.contains("unix")) {
            return jn.a;
        }
        return jn.e;
    }

    public bd() {
        this.e = this.a();
        for (int i2 = 0; i2 < 64; ++i2) {
            for (int i3 = 0; i3 < 64; ++i3) {
                this.h[i2][i3] = new re(null, i2, i3);
            }
        }
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addKeyListener(this);
        this.setFocusable(true);
        this.requestFocus();
        this.setBackground(Color.red);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void b(String string) {
        this.j = 0;
        this.i = 0;
        this.d = new fd(new fm(new File(this.e, "saves"), string, false), string, new Random().nextLong());
        this.d.f = 0;
        List list = this.g;
        synchronized (list) {
            this.g.clear();
            for (int i2 = 0; i2 < 64; ++i2) {
                for (int i3 = 0; i3 < 64; ++i3) {
                    this.h[i2][i3].a(this.d, i2, i3);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void a(int n2) {
        List list = this.g;
        synchronized (list) {
            this.d.f = n2;
            this.g.clear();
            for (int i2 = 0; i2 < 64; ++i2) {
                for (int i3 = 0; i3 < 64; ++i3) {
                    this.h[i2][i3].a(this.d, i2, i3);
                }
            }
        }
    }

    public void b() {
        new tx(this).start();
        for (int i2 = 0; i2 < 8; ++i2) {
            new Thread(this).start();
        }
    }

    public void c() {
        this.f = false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private re a(int n2, int n3) {
        int n4 = n2 & 0x3F;
        int n5 = n3 & 0x3F;
        re re2 = this.h[n4][n5];
        if (re2.c == n2 && re2.d == n3) {
            return re2;
        }
        List list = this.g;
        synchronized (list) {
            this.g.remove(re2);
        }
        re2.a(n2, n3);
        return re2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        un un2 = new un();
        while (this.f) {
            re re2 = null;
            List list = this.g;
            synchronized (list) {
                if (this.g.size() > 0) {
                    re2 = (re)this.g.remove(0);
                }
            }
            if (re2 != null) {
                if (this.a - re2.g < 2) {
                    un2.a(re2);
                    this.repaint();
                } else {
                    re2.h = false;
                }
            }
            try {
                Thread.sleep(2L);
            }
            catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
    }

    public void update(Graphics graphics) {
    }

    public void paint(Graphics graphics) {
    }

    public void d() {
        BufferStrategy bufferStrategy = this.getBufferStrategy();
        if (bufferStrategy == null) {
            this.createBufferStrategy(2);
            return;
        }
        this.a((Graphics2D)bufferStrategy.getDrawGraphics());
        bufferStrategy.show();
    }

    public void a(Graphics2D graphics2D) {
        int n2;
        Object object;
        ++this.a;
        AffineTransform affineTransform = graphics2D.getTransform();
        graphics2D.setClip(0, 0, this.getWidth(), this.getHeight());
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics2D.translate(this.getWidth() / 2, this.getHeight() / 2);
        graphics2D.scale(this.b, this.b);
        graphics2D.translate(this.i, this.j);
        if (this.d != null) {
            object = this.d.u();
            graphics2D.translate(-(((br)object).a + ((br)object).c), -(-((br)object).a + ((br)object).c) + 64);
        }
        object = graphics2D.getClipBounds();
        graphics2D.setColor(new Color(-15724512));
        graphics2D.fillRect(((Rectangle)object).x, ((Rectangle)object).y, ((Rectangle)object).width, ((Rectangle)object).height);
        int n3 = 16;
        int n4 = 3;
        int n5 = ((Rectangle)object).x / n3 / 2 - 2 - n4;
        int n6 = (((Rectangle)object).x + ((Rectangle)object).width) / n3 / 2 + 1 + n4;
        int n7 = ((Rectangle)object).y / n3 - 1 - n4 * 2;
        int n8 = (((Rectangle)object).y + ((Rectangle)object).height + 16 + 128) / n3 + 1 + n4 * 2;
        for (n2 = n7; n2 <= n8; ++n2) {
            for (int i2 = n5; i2 <= n6; ++i2) {
                int n9 = i2 - (n2 >> 1);
                int n10 = i2 + (n2 + 1 >> 1);
                re re2 = this.a(n9, n10);
                re2.g = this.a;
                if (!re2.e) {
                    if (re2.h) continue;
                    re2.h = true;
                    this.g.add(re2);
                    continue;
                }
                re2.h = false;
                if (re2.f) continue;
                int n11 = i2 * n3 * 2 + (n2 & 1) * n3;
                int n12 = n2 * n3 - 128 - 16;
                graphics2D.drawImage((Image)re2.a, n11, n12, null);
            }
        }
        if (this.c) {
            graphics2D.setTransform(affineTransform);
            n2 = this.getHeight() - 32 - 4;
            graphics2D.setColor(new Color(Integer.MIN_VALUE, true));
            graphics2D.fillRect(4, this.getHeight() - 32 - 4, this.getWidth() - 8, 32);
            graphics2D.setColor(Color.WHITE);
            String string = "F1 - F5: load levels   |   0-9: Set time of day   |   Space: return to spawn   |   Double click: zoom   |   Escape: hide this text";
            graphics2D.drawString(string, this.getWidth() / 2 - graphics2D.getFontMetrics().stringWidth(string) / 2, n2 + 20);
        }
        graphics2D.dispose();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
        int n2 = mouseEvent.getX() / this.b;
        int n3 = mouseEvent.getY() / this.b;
        this.i += n2 - this.k;
        this.j += n3 - this.l;
        this.k = n2;
        this.l = n3;
        this.repaint();
    }

    public void mouseMoved(MouseEvent mouseEvent) {
    }

    public void mouseClicked(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            this.b = 3 - this.b;
            this.repaint();
        }
    }

    public void mouseEntered(MouseEvent mouseEvent) {
    }

    public void mouseExited(MouseEvent mouseEvent) {
    }

    public void mousePressed(MouseEvent mouseEvent) {
        int n2 = mouseEvent.getX() / this.b;
        int n3 = mouseEvent.getY() / this.b;
        this.k = n2;
        this.l = n3;
    }

    public void mouseReleased(MouseEvent mouseEvent) {
    }

    public void keyPressed(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == 48) {
            this.a(11);
        }
        if (keyEvent.getKeyCode() == 49) {
            this.a(10);
        }
        if (keyEvent.getKeyCode() == 50) {
            this.a(9);
        }
        if (keyEvent.getKeyCode() == 51) {
            this.a(7);
        }
        if (keyEvent.getKeyCode() == 52) {
            this.a(6);
        }
        if (keyEvent.getKeyCode() == 53) {
            this.a(5);
        }
        if (keyEvent.getKeyCode() == 54) {
            this.a(3);
        }
        if (keyEvent.getKeyCode() == 55) {
            this.a(2);
        }
        if (keyEvent.getKeyCode() == 56) {
            this.a(1);
        }
        if (keyEvent.getKeyCode() == 57) {
            this.a(0);
        }
        if (keyEvent.getKeyCode() == 112) {
            this.b("World1");
        }
        if (keyEvent.getKeyCode() == 113) {
            this.b("World2");
        }
        if (keyEvent.getKeyCode() == 114) {
            this.b("World3");
        }
        if (keyEvent.getKeyCode() == 115) {
            this.b("World4");
        }
        if (keyEvent.getKeyCode() == 116) {
            this.b("World5");
        }
        if (keyEvent.getKeyCode() == 32) {
            this.j = 0;
            this.i = 0;
        }
        if (keyEvent.getKeyCode() == 27) {
            this.c = !this.c;
        }
        this.repaint();
    }

    public void keyReleased(KeyEvent keyEvent) {
    }

    public void keyTyped(KeyEvent keyEvent) {
    }

    static /* synthetic */ boolean a(bd bd2) {
        return bd2.f;
    }
}

