package org.lwjgl.input;

import mcrtx.bridge.DirectBridge;
import org.lwjgl.opengl.Display;
import java.util.ArrayDeque;

public final class Mouse {
    private static final boolean[] down=new boolean[5];
    private static final ArrayDeque<Event> events=new ArrayDeque<Event>();
    private static float x,y,dx,dy,wheel;
    private static boolean grabbed,created;
    private static Event current;
    private static final class Event {
        int button=-1,wheel,x,y;boolean pressed;
        Event(){x=Math.round(Mouse.x);y=Math.round(Mouse.y);}
    }
    private Mouse() { }
    public static void create(){if(created)return;created=true;reset();}
    public static void destroy(){created=false;reset();}
    public static void reset(){
        events.clear();current=null;dx=dy=wheel=0;
        for(int i=0;i<down.length;++i)if(down[i]){down[i]=false;Event e=new Event();e.button=i;events.add(e);}
    }
    public static void event(int type,int code,boolean pressed,float px,float py,float mx,float my){
        if(!created)return;
        if(type==4||type==5){x=px*Display.scaleX();y=Display.getHeight()-1-py*Display.scaleY();}
        if(type==5){dx+=mx;dy-=my;}
        Event e=new Event();
        if(type==4){int button=code==1?0:code==4?1:code==2?2:code==8?3:code==16?4:-1;
            if(button<0)return;e.button=button;e.pressed=pressed;down[button]=pressed;
        }else if(type==6){wheel+=py*120;int amount=(int)wheel;wheel-=amount;e.wheel=amount;}
        events.add(e);
    }
    public static boolean next(){current=events.poll();return current!=null;}
    public static int getEventButton(){return current==null?-1:current.button;}
    public static boolean getEventButtonState(){return current!=null&&current.pressed;}
    public static int getEventDWheel(){return current==null?0:current.wheel;}
    public static int getEventX(){return current==null?getX():current.x;}
    public static int getEventY(){return current==null?getY():current.y;}
    public static int getX(){return Math.round(x);}
    public static int getY(){return Math.round(y);}
    public static int getDX(){int value=(int)dx;dx-=value;return value;}
    public static int getDY(){int value=(int)dy;dy-=value;return value;}
    public static boolean isButtonDown(int button){return button>=0&&button<down.length&&down[button];}
    public static void setGrabbed(boolean value){grabbed=value;dx=dy=0;DirectBridge.control(1,value?1:0,0,0,null);}
    public static boolean isGrabbed(){return grabbed;}
    public static void setCursorPosition(int px,int py){x=px;y=py;DirectBridge.control(5,0,px/Display.scaleX(),(Display.getHeight()-1-py)/Display.scaleY(),null);}
}
