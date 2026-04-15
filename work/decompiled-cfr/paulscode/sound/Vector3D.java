/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

public class Vector3D {
    public float x;
    public float y;
    public float z;

    public Vector3D() {
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }

    public Vector3D(float f2, float f3, float f4) {
        this.x = f2;
        this.y = f3;
        this.z = f4;
    }

    public Vector3D clone() {
        return new Vector3D(this.x, this.y, this.z);
    }

    public Vector3D cross(Vector3D vector3D, Vector3D vector3D2) {
        return new Vector3D(vector3D.y * vector3D2.z - vector3D2.y * vector3D.z, vector3D.z * vector3D2.x - vector3D2.z * vector3D.x, vector3D.x * vector3D2.y - vector3D2.x * vector3D.y);
    }

    public Vector3D cross(Vector3D vector3D) {
        return new Vector3D(this.y * vector3D.z - vector3D.y * this.z, this.z * vector3D.x - vector3D.z * this.x, this.x * vector3D.y - vector3D.x * this.y);
    }

    public float dot(Vector3D vector3D, Vector3D vector3D2) {
        return vector3D.x * vector3D2.x + vector3D.y * vector3D2.y + vector3D.z * vector3D2.z;
    }

    public float dot(Vector3D vector3D) {
        return this.x * vector3D.x + this.y * vector3D.y + this.z * vector3D.z;
    }

    public Vector3D add(Vector3D vector3D, Vector3D vector3D2) {
        return new Vector3D(vector3D.x + vector3D2.x, vector3D.y + vector3D2.y, vector3D.z + vector3D2.z);
    }

    public Vector3D add(Vector3D vector3D) {
        return new Vector3D(this.x + vector3D.x, this.y + vector3D.y, this.z + vector3D.z);
    }

    public Vector3D subtract(Vector3D vector3D, Vector3D vector3D2) {
        return new Vector3D(vector3D.x - vector3D2.x, vector3D.y - vector3D2.y, vector3D.z - vector3D2.z);
    }

    public Vector3D subtract(Vector3D vector3D) {
        return new Vector3D(this.x - vector3D.x, this.y - vector3D.y, this.z - vector3D.z);
    }

    public void normalize() {
        double d2 = Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
        this.x = (float)((double)this.x / d2);
        this.y = (float)((double)this.y / d2);
        this.z = (float)((double)this.z / d2);
    }
}

