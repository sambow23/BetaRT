import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

// Run against the original Beta 1.7.3 jar, without loading a game or graphics context.
public final class ExportVanillaAnimations {
    public static void main(String[] args) throws Exception {
        File output = new File(args[0]);
        output.mkdirs();
        for (String name : new String[] {"cg", "vs", "oh", "if", "hs", "sd"}) {
            Class<?> type = Class.forName(name);
            for (int variant = 0; variant < (name.equals("sd") ? 2 : 1); variant++) {
                Object effect = name.equals("sd")
                        ? type.getConstructor(int.class).newInstance(variant)
                        : type.getConstructor().newInstance();
                int tile = type.getField("b").getInt(effect);
                for (int tick = 0; tick < 64; tick++) type.getMethod("a").invoke(effect);
                BufferedImage atlas = new BufferedImage(512, 16, BufferedImage.TYPE_INT_ARGB);
                for (int frame = 0; frame < 32; frame++) {
                    type.getMethod("a").invoke(effect);
                    byte[] rgba = (byte[]) type.getField("a").get(effect);
                    for (int y = 0; y < 16; y++) for (int x = 0; x < 16; x++) {
                        int i = (y * 16 + x) * 4;
                        int argb = ((rgba[i+3]&255)<<24) | ((rgba[i]&255)<<16)
                                | ((rgba[i+1]&255)<<8) | (rgba[i+2]&255);
                        atlas.setRGB(frame*16+x, y, argb);
                    }
                }
                ImageIO.write(atlas, "png", new File(output, tile + ".png"));
                System.out.println(name + " variant=" + variant + " tile=" + tile);
            }
        }
    }
}
