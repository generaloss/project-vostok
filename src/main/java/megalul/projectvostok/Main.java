package megalul.projectvostok;

import glit.Glit;
import glit.context.ContextListener;
import glit.files.FileHandle;
import glit.graphics.font.BitmapFont;
import glit.graphics.font.FontLoader;
import glit.graphics.gl.*;
import glit.graphics.util.batch.TextureBatch;
import glit.io.glfw.Key;
import glit.math.Maths;
import glit.math.vecmath.vector.Vec3f;
import glit.util.time.Sync;
import megalul.projectvostok.block.blocks.Block;
import megalul.projectvostok.chunk.ChunkUtils;
import megalul.projectvostok.options.Options;
import megalul.projectvostok.world.World;
import megalul.projectvostok.world.render.WorldRenderer;

public class Main implements ContextListener{

    public static String GAME_DIR_PATH = "./";
    public static boolean UPDATE_DEPTH_MAP = true;


    public static void main(String[] args){
        Glit.create("Project Vostok", 1280, 720);

        Gl.enable(Target.DEPTH_TEST);

        Glit.init(new Main());
    }

    private TextureBatch uiBatch;
    private BitmapFont font;

    private Options options;
    private GameCamera camera;
    private World world;
    private WorldRenderer renderer;
    private Sync fpsSync;

    public void init(){
        new FileHandle(GAME_DIR_PATH).mkdirs();
        uiBatch = new TextureBatch(200);
        font = FontLoader.getDefault();
    
        camera = new GameCamera(this, 0.1, 1000, 110);
        camera.getPos().y = ChunkUtils.HEIGHT;
        camera.getRot().set(0, 0, 0);

        fpsSync = new Sync(0);
        options = new Options(this, GAME_DIR_PATH);
        
        renderer = new WorldRenderer(this);
        world = new World(this);
    }

    public void render(){
        controls();
        Gl.clearBuffer(true);
        Gl.clearColor(0.4, 0.7, 0.9);

        camera.update();
        getWorld().getChunks().updateMeshes();
        renderer.render();
        renderUi();
    }

    private void renderUi(){
        if(!options.isShowFPS())
            return;

        uiBatch.setColor(0.5F, 0.4F, 0.1F, 1);
        uiBatch.begin();

        font.drawText(uiBatch, "fps: " + Glit.getFps(), 25, Glit.getHeight() - 25 - font.getScaledLineHeight());
        font.drawText(uiBatch,
                "position: " + camera.getPos().x() + ", " + camera.getPos().y() + ", " + camera.getPos().z(),
                25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 2);
        font.drawText(uiBatch, "Threads:", 25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 3);
        font.drawText(uiBatch, "chunk find tps: " + world.getChunks().findTps.get(), 25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 4);
        font.drawText(uiBatch, "chunk load tps: " + world.getChunks().loadTps.get(),   25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 5);
        font.drawText(uiBatch, "chunk build tps: " + world.getChunks().buildTps.get(), 25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 6);
        font.drawText(uiBatch, "chunk check tps: " + world.getChunks().checkTps.get(), 25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 7);
        font.drawText(uiBatch, "meshes: " + world.getChunks().getMeshes().size(), 25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 8);
        font.drawText(uiBatch, "render chunks: " + world.getChunks().getChunks().stream().filter(camera::isChunkSeen).count(), 25, Glit.getHeight() - 25 - font.getScaledLineHeight() * 9);

        font.drawText(uiBatch, "1, 2, 3 - set render mode", 25, 25 + font.getScaledLineHeight() * 0);
        font.drawText(uiBatch, "B - set block in camera", 25, 25 + font.getScaledLineHeight() * 1);

        uiBatch.end();
    }

    private void controls(){
        if(Glit.isDown(Key.ESCAPE))
            Glit.exit();
        if(Glit.isDown(Key.F11)){
            camera.lockNextFrameRotate();
            Glit.window().toggleFullscreen();
        }
        if(Glit.isDown(Key.NUM_1))
            Gl.setPolygonMode(Face.FRONT, PolygonMode.FILL);
        if(Glit.isDown(Key.NUM_2))
            Gl.setPolygonMode(Face.FRONT, PolygonMode.LINE);
        if(Glit.isDown(Key.NUM_3))
            Gl.setPolygonMode(Face.FRONT, PolygonMode.POINT);

        Vec3f camPos = camera.getPos();
        if(Glit.isPressed(Key.B))
            world.setBlock(camPos.x(), Maths.clamp(camPos.yf(), 0, 255), camPos.z(), Block.DIRT.getState());
    }


    public void resize(int width, int height){
        camera.resize(width, height);
    }

    public void dispose(){
        uiBatch.dispose();
        font.dispose();
        renderer.dispose();

        options.save();
    }


    public Options getOptions(){
        return options;
    }

    public GameCamera getCamera(){
        return camera;
    }

    public World getWorld(){
        return world;
    }

    public Sync getFpsSync(){
        return fpsSync;
    }

}
