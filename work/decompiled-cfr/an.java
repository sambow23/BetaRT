/*
 * Decompiled with CFR 0.152.
 */
import java.io.Reader;
import java.util.Arrays;

public final class an {
    public void a(Reader reader, wg wg2) {
        lj lj2 = new lj(reader);
        char c2 = (char)lj2.c();
        switch (c2) {
            case '{': {
                lj2.a(c2);
                wg2.b();
                this.b(lj2, wg2);
                break;
            }
            case '[': {
                lj2.a(c2);
                wg2.b();
                this.a(lj2, wg2);
                break;
            }
            default: {
                throw new xe("Expected either [ or { but got [" + c2 + "].", lj2);
            }
        }
        int n2 = this.l(lj2);
        if (n2 != -1) {
            throw new xe("Got unexpected trailing character [" + (char)n2 + "].", lj2);
        }
        wg2.c();
    }

    private void a(lj lj2, wg wg2) {
        char c2 = (char)this.l(lj2);
        if (c2 != '[') {
            throw new xe("Expected object to start with [ but got [" + c2 + "].", lj2);
        }
        wg2.d();
        char c3 = (char)this.l(lj2);
        lj2.a(c3);
        if (c3 != ']') {
            this.d(lj2, wg2);
        }
        boolean bl2 = false;
        block4: while (!bl2) {
            char c4 = (char)this.l(lj2);
            switch (c4) {
                case ',': {
                    this.d(lj2, wg2);
                    continue block4;
                }
                case ']': {
                    bl2 = true;
                    continue block4;
                }
            }
            throw new xe("Expected either , or ] but got [" + c4 + "].", lj2);
        }
        wg2.e();
    }

    private void b(lj lj2, wg wg2) {
        char c2 = (char)this.l(lj2);
        if (c2 != '{') {
            throw new xe("Expected object to start with { but got [" + c2 + "].", lj2);
        }
        wg2.f();
        char c3 = (char)this.l(lj2);
        lj2.a(c3);
        if (c3 != '}') {
            this.c(lj2, wg2);
        }
        boolean bl2 = false;
        block4: while (!bl2) {
            char c4 = (char)this.l(lj2);
            switch (c4) {
                case ',': {
                    this.c(lj2, wg2);
                    continue block4;
                }
                case '}': {
                    bl2 = true;
                    continue block4;
                }
            }
            throw new xe("Expected either , or } but got [" + c4 + "].", lj2);
        }
        wg2.g();
    }

    private void c(lj lj2, wg wg2) {
        char c2 = (char)this.l(lj2);
        if ('\"' != c2) {
            throw new xe("Expected object identifier to begin with [\"] but got [" + c2 + "].", lj2);
        }
        lj2.a(c2);
        wg2.a(this.i(lj2));
        char c3 = (char)this.l(lj2);
        if (c3 != ':') {
            throw new xe("Expected object identifier to be followed by : but got [" + c3 + "].", lj2);
        }
        this.d(lj2, wg2);
        wg2.h();
    }

    private void d(lj lj2, wg wg2) {
        char c2 = (char)this.l(lj2);
        switch (c2) {
            case '\"': {
                lj2.a(c2);
                wg2.c(this.i(lj2));
                break;
            }
            case 't': {
                char[] cArray = new char[3];
                int n2 = lj2.b(cArray);
                if (n2 != 3 || cArray[0] != 'r' || cArray[1] != 'u' || cArray[2] != 'e') {
                    lj2.a(cArray);
                    throw new xe("Expected 't' to be followed by [[r, u, e]], but got [" + Arrays.toString(cArray) + "].", lj2);
                }
                wg2.i();
                break;
            }
            case 'f': {
                char[] cArray = new char[4];
                int n3 = lj2.b(cArray);
                if (n3 != 4 || cArray[0] != 'a' || cArray[1] != 'l' || cArray[2] != 's' || cArray[3] != 'e') {
                    lj2.a(cArray);
                    throw new xe("Expected 'f' to be followed by [[a, l, s, e]], but got [" + Arrays.toString(cArray) + "].", lj2);
                }
                wg2.j();
                break;
            }
            case 'n': {
                char[] cArray = new char[3];
                int n4 = lj2.b(cArray);
                if (n4 != 3 || cArray[0] != 'u' || cArray[1] != 'l' || cArray[2] != 'l') {
                    lj2.a(cArray);
                    throw new xe("Expected 'n' to be followed by [[u, l, l]], but got [" + Arrays.toString(cArray) + "].", lj2);
                }
                wg2.k();
                break;
            }
            case '-': 
            case '0': 
            case '1': 
            case '2': 
            case '3': 
            case '4': 
            case '5': 
            case '6': 
            case '7': 
            case '8': 
            case '9': {
                lj2.a(c2);
                wg2.b(this.a(lj2));
                break;
            }
            case '{': {
                lj2.a(c2);
                this.b(lj2, wg2);
                break;
            }
            case '[': {
                lj2.a(c2);
                this.a(lj2, wg2);
                break;
            }
            default: {
                throw new xe("Invalid character at start of value [" + c2 + "].", lj2);
            }
        }
    }

    private String a(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        char c2 = (char)lj2.c();
        if ('-' == c2) {
            stringBuilder.append('-');
        } else {
            lj2.a(c2);
        }
        stringBuilder.append(this.b(lj2));
        return stringBuilder.toString();
    }

    private String b(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        char c2 = (char)lj2.c();
        if ('0' == c2) {
            stringBuilder.append('0');
            stringBuilder.append(this.f(lj2));
            stringBuilder.append(this.g(lj2));
        } else {
            lj2.a(c2);
            stringBuilder.append(this.c(lj2));
            stringBuilder.append(this.e(lj2));
            stringBuilder.append(this.f(lj2));
            stringBuilder.append(this.g(lj2));
        }
        return stringBuilder.toString();
    }

    private char c(lj lj2) {
        char c2;
        char c3 = (char)lj2.c();
        switch (c3) {
            case '1': 
            case '2': 
            case '3': 
            case '4': 
            case '5': 
            case '6': 
            case '7': 
            case '8': 
            case '9': {
                c2 = c3;
                break;
            }
            default: {
                throw new xe("Expected a digit 1 - 9 but got [" + c3 + "].", lj2);
            }
        }
        return c2;
    }

    private char d(lj lj2) {
        char c2;
        char c3 = (char)lj2.c();
        switch (c3) {
            case '0': 
            case '1': 
            case '2': 
            case '3': 
            case '4': 
            case '5': 
            case '6': 
            case '7': 
            case '8': 
            case '9': {
                c2 = c3;
                break;
            }
            default: {
                throw new xe("Expected a digit 1 - 9 but got [" + c3 + "].", lj2);
            }
        }
        return c2;
    }

    private String e(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl2 = false;
        block3: while (!bl2) {
            char c2 = (char)lj2.c();
            switch (c2) {
                case '0': 
                case '1': 
                case '2': 
                case '3': 
                case '4': 
                case '5': 
                case '6': 
                case '7': 
                case '8': 
                case '9': {
                    stringBuilder.append(c2);
                    continue block3;
                }
            }
            bl2 = true;
            lj2.a(c2);
        }
        return stringBuilder.toString();
    }

    private String f(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        char c2 = (char)lj2.c();
        if (c2 == '.') {
            stringBuilder.append('.');
            stringBuilder.append(this.d(lj2));
            stringBuilder.append(this.e(lj2));
        } else {
            lj2.a(c2);
        }
        return stringBuilder.toString();
    }

    private String g(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        char c2 = (char)lj2.c();
        if (c2 == '.' || c2 == 'E') {
            stringBuilder.append('E');
            stringBuilder.append(this.h(lj2));
            stringBuilder.append(this.d(lj2));
            stringBuilder.append(this.e(lj2));
        } else {
            lj2.a(c2);
        }
        return stringBuilder.toString();
    }

    private String h(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        char c2 = (char)lj2.c();
        if (c2 == '+' || c2 == '-') {
            stringBuilder.append(c2);
        } else {
            lj2.a(c2);
        }
        return stringBuilder.toString();
    }

    private String i(lj lj2) {
        StringBuilder stringBuilder = new StringBuilder();
        char c2 = (char)lj2.c();
        if ('\"' != c2) {
            throw new xe("Expected [\"] but got [" + c2 + "].", lj2);
        }
        boolean bl2 = false;
        block4: while (!bl2) {
            char c3 = (char)lj2.c();
            switch (c3) {
                case '\"': {
                    bl2 = true;
                    continue block4;
                }
                case '\\': {
                    char c4 = this.j(lj2);
                    stringBuilder.append(c4);
                    continue block4;
                }
            }
            stringBuilder.append(c3);
        }
        return stringBuilder.toString();
    }

    private char j(lj lj2) {
        char c2;
        char c3 = (char)lj2.c();
        switch (c3) {
            case '\"': {
                c2 = '\"';
                break;
            }
            case '\\': {
                c2 = '\\';
                break;
            }
            case '/': {
                c2 = '/';
                break;
            }
            case 'b': {
                c2 = '\b';
                break;
            }
            case 'f': {
                c2 = '\f';
                break;
            }
            case 'n': {
                c2 = '\n';
                break;
            }
            case 'r': {
                c2 = '\r';
                break;
            }
            case 't': {
                c2 = '\t';
                break;
            }
            case 'u': {
                c2 = (char)this.k(lj2);
                break;
            }
            default: {
                throw new xe("Unrecognised escape character [" + c3 + "].", lj2);
            }
        }
        return c2;
    }

    private int k(lj lj2) {
        int n2;
        char[] cArray = new char[4];
        int n3 = lj2.b(cArray);
        if (n3 != 4) {
            throw new xe("Expected a 4 digit hexidecimal number but got only [" + n3 + "], namely [" + String.valueOf(cArray, 0, n3) + "].", lj2);
        }
        try {
            n2 = Integer.parseInt(String.valueOf(cArray), 16);
        }
        catch (NumberFormatException numberFormatException) {
            lj2.a(cArray);
            throw new xe("Unable to parse [" + String.valueOf(cArray) + "] as a hexidecimal number.", numberFormatException, lj2);
        }
        return n2;
    }

    private int l(lj lj2) {
        int n2;
        boolean bl2 = false;
        do {
            n2 = lj2.c();
            switch (n2) {
                case 9: 
                case 10: 
                case 13: 
                case 32: {
                    break;
                }
                default: {
                    bl2 = true;
                }
            }
        } while (!bl2);
        return n2;
    }
}

