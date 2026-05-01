package org.lwjgl.opengl;

public final class ContextCapabilities {
    public final boolean GL_ARB_occlusion_query;
    public final boolean GL_ARB_vertex_buffer_object;
    public final boolean GL_NV_fog_distance;

    ContextCapabilities(boolean hasOcclusionQuery, boolean hasVertexBufferObject, boolean hasFogDistance) {
        this.GL_ARB_occlusion_query = hasOcclusionQuery;
        this.GL_ARB_vertex_buffer_object = hasVertexBufferObject;
        this.GL_NV_fog_distance = hasFogDistance;
    }
}