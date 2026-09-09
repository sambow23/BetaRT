import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.Enumeration;

/** Ask Java 8 to verify every packaged class, including rarely used vanilla paths. */
public final class DirectClassLoadingTest {
    public static void main(String[] args)throws Exception {
        int count=0;
        try(JarFile jar=new JarFile(args[0])){
            Enumeration<JarEntry> entries=jar.entries();
            while(entries.hasMoreElements()){
                String name=entries.nextElement().getName();if(!name.endsWith(".class"))continue;
                Class<?> type=Class.forName(name.substring(0,name.length()-6).replace('/','.'),false,DirectClassLoadingTest.class.getClassLoader());
                type.getDeclaredMethods();type.getDeclaredConstructors();type.getDeclaredFields();++count;
            }
        }
        System.out.println("Java 8 verified all "+count+" packaged classes without OpenGL");
    }
}
