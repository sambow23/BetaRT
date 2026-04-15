package mcrtx.bridge;

public final class CameraPose {
    public float px;
    public float py;
    public float pz;
    public float fx;
    public float fy;
    public float fz;
    public float ux = 0.0f;
    public float uy = 1.0f;
    public float uz = 0.0f;
    public float rx = 1.0f;
    public float ry = 0.0f;
    public float rz = 0.0f;
    public float fovYDegrees = 70.0f;
    public float aspect = 16.0f / 9.0f;
    public float nearPlane = 0.05f;
    public float farPlane = 1024.0f;
}