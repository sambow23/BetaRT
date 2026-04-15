/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import java.util.Locale;
import paulscode.sound.ICodec;
import paulscode.sound.SoundSystemConfig;

class SoundSystemConfig$Codec {
    public String extensionRegX = "";
    public Class iCodecClass;

    public SoundSystemConfig$Codec(String string, Class clazz) {
        if (string != null && string.length() > 0) {
            this.extensionRegX = ".*";
            for (int i2 = 0; i2 < string.length(); ++i2) {
                String string2 = string.substring(i2, i2 + 1);
                this.extensionRegX = this.extensionRegX + "[" + string2.toLowerCase(Locale.ENGLISH) + string2.toUpperCase(Locale.ENGLISH) + "]";
            }
            this.extensionRegX = this.extensionRegX + "$";
        }
        this.iCodecClass = clazz;
    }

    public ICodec getInstance() {
        if (this.iCodecClass == null) {
            return null;
        }
        Object var1_1 = null;
        try {
            var1_1 = this.iCodecClass.newInstance();
        }
        catch (InstantiationException instantiationException) {
            this.instantiationErrorMessage();
            return null;
        }
        catch (IllegalAccessException illegalAccessException) {
            this.instantiationErrorMessage();
            return null;
        }
        catch (ExceptionInInitializerError exceptionInInitializerError) {
            this.instantiationErrorMessage();
            return null;
        }
        catch (SecurityException securityException) {
            this.instantiationErrorMessage();
            return null;
        }
        if (var1_1 == null) {
            this.instantiationErrorMessage();
            return null;
        }
        return var1_1;
    }

    private void instantiationErrorMessage() {
        SoundSystemConfig.access$000("Unrecognized ICodec implementation in method 'getInstance'.  Ensure that the implementing class has one public, parameterless constructor.");
    }
}

