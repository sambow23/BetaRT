import mcrtx.render.DirectEnvironment;

public final class DirectEnvironmentTest {
    private static void near(float a,float b){if(Math.abs(a-b)>1e-5)throw new AssertionError(a+" != "+b);}
    public static void main(String[] args){
        float[] noon=DirectEnvironment.lighting(true,.5f,1,0,0,0,0);
        near(noon[0],.21404114f);near(noon[1],1);near(noon[2],0);
        near(noon[3],0);near(noon[4],-1);near(noon[5],0);near(noon[6],(float)Math.PI);
        float[] dusk=DirectEnvironment.lighting(true,0,0,0,.25f,0,0);
        near(dusk[4],0);near(dusk[5],-1);near(dusk[6],0);
        float[] dawn=DirectEnvironment.lighting(true,0,0,0,.75f,0,0);
        near(dawn[4],0);near(dawn[5],1);near(dawn[6],0);
        near(DirectEnvironment.linear(.04045f),.04045f/12.92f);
        near(DirectEnvironment.linear(-1),0);near(DirectEnvironment.linear(2),1);
        // Both sides of either horizon approach zero continuously.
        for(float angle:new float[]{.24999f,.25001f,.74999f,.75001f}) {
            float power=DirectEnvironment.lighting(true,0,0,0,angle,0,0)[6];
            if(power<0||power>.001f)throw new AssertionError("Horizon discontinuity: "+power);
        }
        float angle=.22f;
        near(DirectEnvironment.lighting(true,0,0,0,angle,0,0)[6],
            (float)(Math.PI*2*Math.cos(angle*2*Math.PI)));
        near(DirectEnvironment.lighting(true,0,0,0,0,1,0)[6],0);
        near(DirectEnvironment.lighting(true,0,0,0,0,0,1)[6],0);
        near(DirectEnvironment.lighting(true,0,0,0,.5f,0,0)[6],0);
        near(DirectEnvironment.lighting(true,0,0,0,0,.5f,.5f)[6],(float)Math.PI*.25f);
        float[] disabled=DirectEnvironment.lighting(false,1,1,1,0,0,0);
        for(int i:new int[]{0,1,2,6,7,8})near(disabled[i],0);
        System.out.println("Linear sky, visible sun direction, noon calibration, night, weather and disabled dimensions passed");
    }
}
