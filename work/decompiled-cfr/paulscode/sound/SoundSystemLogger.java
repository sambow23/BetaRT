/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

public class SoundSystemLogger {
    public void message(String string, int n2) {
        String string2 = "";
        for (int i2 = 0; i2 < n2; ++i2) {
            string2 = string2 + "    ";
        }
        String string3 = string2 + string;
        System.out.println(string3);
    }

    public void importantMessage(String string, int n2) {
        String string2 = "";
        for (int i2 = 0; i2 < n2; ++i2) {
            string2 = string2 + "    ";
        }
        String string3 = string2 + string;
        System.out.println(string3);
    }

    public boolean errorCheck(boolean bl2, String string, String string2, int n2) {
        if (bl2) {
            this.errorMessage(string, string2, n2);
        }
        return bl2;
    }

    public void errorMessage(String string, String string2, int n2) {
        String string3 = "";
        for (int i2 = 0; i2 < n2; ++i2) {
            string3 = string3 + "    ";
        }
        String string4 = string3 + "Error in class '" + string + "'";
        String string5 = "    " + string3 + string2;
        System.out.println(string4);
        System.out.println(string5);
    }

    public void printStackTrace(Exception exception, int n2) {
        this.printExceptionMessage(exception, n2);
        this.importantMessage("STACK TRACE:", n2);
        if (exception == null) {
            return;
        }
        StackTraceElement[] stackTraceElementArray = exception.getStackTrace();
        if (stackTraceElementArray == null) {
            return;
        }
        for (int i2 = 0; i2 < stackTraceElementArray.length; ++i2) {
            StackTraceElement stackTraceElement = stackTraceElementArray[i2];
            if (stackTraceElement == null) continue;
            this.message(stackTraceElement.toString(), n2 + 1);
        }
    }

    public void printExceptionMessage(Exception exception, int n2) {
        this.importantMessage("ERROR MESSAGE:", n2);
        if (exception.getMessage() == null) {
            this.message("(none)", n2 + 1);
        } else {
            this.message(exception.getMessage(), n2 + 1);
        }
    }
}

