/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import paulscode.sound.Vector3D;

public class ListenerData {
    public Vector3D position;
    public Vector3D lookAt;
    public Vector3D up;
    public float angle = 0.0f;

    public ListenerData() {
        this.position = new Vector3D(0.0f, 0.0f, 0.0f);
        this.lookAt = new Vector3D(0.0f, 0.0f, -1.0f);
        this.up = new Vector3D(0.0f, 1.0f, 0.0f);
        this.angle = 0.0f;
    }

    public ListenerData(float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
        this.position = new Vector3D(f2, f3, f4);
        this.lookAt = new Vector3D(f5, f6, f7);
        this.up = new Vector3D(f8, f9, f10);
        this.angle = f11;
    }

    public ListenerData(Vector3D vector3D, Vector3D vector3D2, Vector3D vector3D3, float f2) {
        this.position = vector3D.clone();
        this.lookAt = vector3D2.clone();
        this.up = vector3D3.clone();
        this.angle = f2;
    }

    public void setData(float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, float f11) {
        this.position.x = f2;
        this.position.y = f3;
        this.position.z = f4;
        this.lookAt.x = f5;
        this.lookAt.y = f6;
        this.lookAt.z = f7;
        this.up.x = f8;
        this.up.y = f9;
        this.up.z = f10;
        this.angle = f11;
    }

    public void setData(Vector3D vector3D, Vector3D vector3D2, Vector3D vector3D3, float f2) {
        this.position.x = vector3D.x;
        this.position.y = vector3D.y;
        this.position.z = vector3D.z;
        this.lookAt.x = vector3D2.x;
        this.lookAt.y = vector3D2.y;
        this.lookAt.z = vector3D2.z;
        this.up.x = vector3D3.x;
        this.up.y = vector3D3.y;
        this.up.z = vector3D3.z;
        this.angle = f2;
    }

    public void setData(ListenerData listenerData) {
        this.position.x = listenerData.position.x;
        this.position.y = listenerData.position.y;
        this.position.z = listenerData.position.z;
        this.lookAt.x = listenerData.lookAt.x;
        this.lookAt.y = listenerData.lookAt.y;
        this.lookAt.z = listenerData.lookAt.z;
        this.up.x = listenerData.up.x;
        this.up.y = listenerData.up.y;
        this.up.z = listenerData.up.z;
        this.angle = listenerData.angle;
    }

    public void setPosition(float f2, float f3, float f4) {
        this.position.x = f2;
        this.position.y = f3;
        this.position.z = f4;
    }

    public void setPosition(Vector3D vector3D) {
        this.position.x = vector3D.x;
        this.position.y = vector3D.y;
        this.position.z = vector3D.z;
    }

    public void setOrientation(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.lookAt.x = f2;
        this.lookAt.y = f3;
        this.lookAt.z = f4;
        this.up.x = f5;
        this.up.y = f6;
        this.up.z = f7;
    }

    public void setOrientation(Vector3D vector3D, Vector3D vector3D2) {
        this.lookAt.x = vector3D.x;
        this.lookAt.y = vector3D.y;
        this.lookAt.z = vector3D.z;
        this.up.x = vector3D2.x;
        this.up.y = vector3D2.y;
        this.up.z = vector3D2.z;
    }

    public void setAngle(float f2) {
        this.angle = f2;
        this.lookAt.x = -1.0f * (float)Math.sin(this.angle);
        this.lookAt.z = -1.0f * (float)Math.cos(this.angle);
    }
}

