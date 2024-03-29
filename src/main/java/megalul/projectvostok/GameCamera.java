package megalul.projectvostok;

import glit.Glit;
import glit.graphics.camera.PerspectiveCamera;
import glit.math.vecmath.vector.Vec3f;
import megalul.projectvostok.chunk.Chunk;
import megalul.projectvostok.chunk.data.ChunkPos;
import megalul.projectvostok.options.KeyMapping;

import static megalul.projectvostok.chunk.ChunkUtils.HEIGHT;
import static megalul.projectvostok.chunk.ChunkUtils.SIZE;

public class GameCamera extends PerspectiveCamera{

    private final Main session;

    private final Vec3f up = new Vec3f(0, 1, 0);
    private float dAngX, dAngY;
    private boolean doNotRotateInTheNextFrame;

    public GameCamera(Main session, double near, double far, double fieldOfView){
        super(near, far, fieldOfView);

        this.session = session;

        doNotRotateInTheNextFrame = true;
        Glit.mouse().show(false);
    }


    public void update(){
        if(Glit.window().isFocused()){
            if(!doNotRotateInTheNextFrame && Glit.mouse().inWindow()){
                float x = Glit.mouse().getX();
                float y = Glit.mouse().getY();
                dAngX += Glit.getWidth() / 2F - x;
                dAngY += Glit.getHeight() / 2F - y;

                float sensitivity = session.getOptions().getMouseSensitivity();
                getRot().yaw += dAngX * 0.1 * sensitivity;
                getRot().pitch += dAngY * 0.1 * sensitivity;
                getRot().constrain();

                dAngX *= 0.1;
                dAngY *= 0.1;
            }
            Glit.mouse().setPos(Glit.getWidth() / 2, Glit.getHeight() / 2);
            doNotRotateInTheNextFrame = false;
        }


        float speed = Glit.getDeltaTime() * 150;
        if(isPressed(KeyMapping.SPRINT))
            speed *= 3;

        Vec3f dir = getRot().direction();
        Vec3f acceleration = dir.clone();
        acceleration.y = 0;
        acceleration.nor().mul(speed);

        if(isPressed(KeyMapping.FORWARD))
            getPos().add(acceleration);
        if(isPressed(KeyMapping.BACK))
            getPos().sub(acceleration);

        Vec3f dirXZ = dir.clone();
        dirXZ.y = 0;
        Vec3f sideMove = Vec3f.crs(up, dirXZ.nor()).mul(speed);
        if(isPressed(KeyMapping.RIGHT))
            getPos().add(sideMove);
        if(isPressed(KeyMapping.LEFT))
            getPos().sub(sideMove);
        if(isPressed(KeyMapping.JUMP))
            getPos().y += speed;
        if(isPressed(KeyMapping.SNEAK))
            getPos().y -= speed;

        super.update();
    }

    public void lockNextFrameRotate(){
        doNotRotateInTheNextFrame = true;
    }

    private boolean isPressed(KeyMapping key){
        return Glit.isPressed(session.getOptions().getKey(key));
    }
    
    
    public boolean isChunkSeen(int chunkX, int chunkZ){
        return getFrustum().isBoxInFrustum(
            chunkX * SIZE, 0, chunkZ * SIZE,
            chunkX * SIZE + SIZE, HEIGHT, chunkZ * SIZE + SIZE
        );
    }
    
    public boolean isChunkSeen(ChunkPos pos){
        return isChunkSeen(pos.x, pos.z);
    }
    
    public boolean isChunkSeen(Chunk chunk){
        ChunkPos pos = chunk.getPos();
        
        return getFrustum().isBoxInFrustum(
            pos.x * SIZE, chunk.getMinY(), pos.z * SIZE,
            pos.x * SIZE + SIZE, chunk.getMaxY() + 1, pos.z * SIZE + SIZE
        );
    }

}
