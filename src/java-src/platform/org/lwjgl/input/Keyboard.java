package org.lwjgl.input;

import java.util.ArrayDeque;
import java.util.Arrays;

/** Vanilla key numbers with ordered SDL key and Unicode text events. */
public final class Keyboard {
    private static final int[] KEYS={0,30,48,46,32,18,33,34,35,23,36,37,38,50,49,24,25,16,19,31,20,22,47,17,45,21,44,
        11,2,3,4,5,6,7,8,9,10,1,28,15,14,57,203,205,200,208,42,54,29,157,56,184,
        59,60,61,62,63,64,65,66,67,68,87,88,40,51,12,52,53,39,13,26,43,27,41,210,211,199,207,201,209};
    private static final String[] NAMES=new String[256];
    private static final boolean[] down=new boolean[256];
    private static final ArrayDeque<Event> events=new ArrayDeque<Event>();
    private static Event current;
    private static boolean repeat,created;
    static {
        for(int i=0;i<26;++i)NAMES[KEYS[i+1]]=String.valueOf((char)('A'+i));
        for(int i=0;i<10;++i)NAMES[KEYS[i+27]]=String.valueOf((char)('0'+i));
        String[] names={"ESCAPE","RETURN","TAB","BACK","SPACE","LEFT","RIGHT","UP","DOWN","LSHIFT","RSHIFT","LCONTROL","RCONTROL","LMENU","RMENU",
            "F1","F2","F3","F4","F5","F6","F7","F8","F9","F10","F11","F12","APOSTROPHE","COMMA","MINUS","PERIOD","SLASH","SEMICOLON","EQUALS","LBRACKET","BACKSLASH","RBRACKET","GRAVE","INSERT","DELETE","HOME","END","PRIOR","NEXT"};
        for(int i=0;i<names.length;++i)NAMES[KEYS[i+37]]=names[i];
    }
    private Keyboard() { }
    private static final class Event {
        final int key;final char character;final boolean down;
        Event(int key,char character,boolean down){this.key=key;this.character=character;this.down=down;}
    }
    public static void create(){if(created)return;created=true;reset();}
    public static void destroy(){created=false;events.clear();Arrays.fill(down,false);current=null;}
    public static void reset(){
        events.clear();current=null;
        for(int i=0;i<down.length;++i)if(down[i]){down[i]=false;events.add(new Event(i,(char)0,false));}
    }
    public static void key(int code,boolean pressed,boolean repeated){
        if(!created||code<=0||code>=KEYS.length)return;
        int key=KEYS[code];down[key]=pressed; // The runtime menu's shortcut is OpenRemix's own.
        if(repeated&&!repeat)return;
        char text=key==14?'\b':key==28?'\r':key==15?'\t':0;
        events.add(new Event(key,pressed?text:0,pressed));
    }
    public static void text(int scalar){
        if(!created||!Character.isValidCodePoint(scalar))return;
        for(char c:Character.toChars(scalar))events.add(new Event(0,c,true));
    }
    public static boolean next(){current=events.poll();return current!=null;}
    public static int getEventKey(){return current==null?0:current.key;}
    public static char getEventCharacter(){return current==null?0:current.character;}
    public static boolean getEventKeyState(){return current!=null&&current.down;}
    public static boolean isKeyDown(int key){return key>=0&&key<down.length&&down[key];}
    public static void enableRepeatEvents(boolean enabled){repeat=enabled;}
    public static String getKeyName(int key){return key>=0&&key<NAMES.length&&NAMES[key]!=null?NAMES[key]:"KEY"+key;}
}
