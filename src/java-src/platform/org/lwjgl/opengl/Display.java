package org.lwjgl.opengl;

import mcrtx.bridge.DirectBridge;
import mcrtx.render.DirectRenderer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import java.awt.Canvas;
import java.awt.Component;
import java.awt.Window;

/** Source compatibility for the game's window calls. SDL owns the sole native window. */
public final class Display {
    private static int width=1280,height=720,logicalWidth=1280,logicalHeight=720;
    private static boolean created,focused=true,closed,fullscreen;
    private static Window launcherWindow;
    private Display() { }
    public static void create(){
        if(created)return;
        DirectRenderer.reset();
        try{DirectBridge.start(width,height);}catch(RuntimeException error){destroy();throw error;}
        created=true;
        Keyboard.create();Mouse.create();if(fullscreen)DirectBridge.control(2,1,0,0,null);poll();
    }
    public static void destroy(){
        DirectBridge.stop();Keyboard.destroy();Mouse.destroy();created=false;closed=true;
        if(launcherWindow!=null){final Window window=launcherWindow;launcherWindow=null;java.awt.EventQueue.invokeLater(()->window.dispose());}
    }
    public static void poll(){
        if(!created)return;int[] data=DirectBridge.poll();
        if(data.length<9)throw new IllegalStateException("Invalid native input packet");
        logicalWidth=Math.max(1,data[0]);logicalHeight=Math.max(1,data[1]);
        width=Math.max(1,data[2]);height=Math.max(1,data[3]);focused=data[4]!=0;closed=data[5]!=0;
        if(data[8]!=(data.length-9)/8)throw new IllegalStateException("Truncated native input packet");
        for(int at=9;at<data.length;at+=8){
            int type=data[at];
            if(type==1){Keyboard.reset();Mouse.reset();}
            else if((data[7]&4)!=0){
                if(type==2&&data[at+1]==63)Keyboard.key(data[at+1],data[at+2]!=0,data[at+3]!=0);
            }
            else if(type==2)Keyboard.key(data[at+1],data[at+2]!=0,data[at+3]!=0);
            else if(type==3)Keyboard.text(data[at+1]);
            else Mouse.event(type,data[at+1],data[at+2]!=0,Float.intBitsToFloat(data[at+4]),Float.intBitsToFloat(data[at+5]),Float.intBitsToFloat(data[at+6]),Float.intBitsToFloat(data[at+7]));
        }
    }
    public static void update(){DirectRenderer.present();poll();}
    public static void swapBuffers(){DirectRenderer.present();}
    public static boolean isActive(){return created&&focused;}
    public static boolean isCloseRequested(){return closed;}
    public static DisplayMode getDisplayMode(){return new DisplayMode(width,height);}
    public static DisplayMode getDesktopDisplayMode(){return new DisplayMode(width,height);}
    public static void setDisplayMode(DisplayMode mode){if(!created){width=mode.getWidth();height=mode.getHeight();}}
    public static int getWidth(){return width;}
    public static int getHeight(){return height;}
    public static float scaleX(){return (float)width/logicalWidth;}
    public static float scaleY(){return (float)height/logicalHeight;}
    public static void setFullscreen(boolean value){fullscreen=value;if(created)DirectBridge.control(2,value?1:0,0,0,null);}
    public static void setTitle(String title){/* Startup names the SDL window BetaRT. */}
    public static void setParent(Canvas parent){
        if(parent==null)return;
        if(parent.getWidth()>0&&parent.getHeight()>0){width=parent.getWidth();height=parent.getHeight();}
        Component owner=parent;while(owner!=null&&!(owner instanceof Window))owner=owner.getParent();
        if(owner!=null){launcherWindow=(Window)owner;final Component window=owner;java.awt.EventQueue.invokeLater(()->window.setVisible(false));}
    }
}
