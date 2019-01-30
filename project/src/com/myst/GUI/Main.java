package com.myst.GUI;

import com.myst.rendering.Model;
import com.myst.rendering.Texture;
import com.myst.rendering.Window;
import com.myst.world.map.rendering.Shader;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;
import java.awt.Rectangle;

import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class Main {

    public static void main(String[] args){
        Window.setCallbacks();
        if (!glfwInit()){
            throw new IllegalStateException("Failed to initialise GLFW");
        }

        Window window = new Window();

        window.setFullscreen(false);
        window.createWindow("My GUI");

        GL.createCapabilities();

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0f,0f,0f, 0f);


        float[] vertices = new float[] {
                -0.5f, 0.5f, 0f, /*0*/  0.5f, 0.5f, 0f, /*1*/    0.5f, -0.5f, 0f, /*2*/
                -0.5f, -0.5f, 0f/*3*/
        };

        float[] textureDocks = new float[] {
                0f, 0f,   1, 0f,  1f, 1f,
                0f, 1f
        };

        int[] indices = new int[] {
                0,1,2,
                2,3,0
        };

        Shader shader = new Shader("assets/shader");
        Texture texture = new Texture("assets/gamemenu.png");

        Model model = new Model(vertices, textureDocks, indices);


        while (!window.shouldClose()){
            window.update();

            renderImage(shader,texture,0,0,new Matrix4f().scale(0.65f), model);

            window.swapBuffers();
        }

    }


    public static  void renderImage(Shader shader, Texture texture, int x, int y, Matrix4f scale, Model model){
        shader.bind();
        texture.bind(0);
        Matrix4f target = new Matrix4f();

        Matrix4f tile_pos = new Matrix4f().translate(new Vector3f(x,y,0));
        scale.mul(tile_pos, target);


        shader.setUniform("sampler",0);
        shader.setUniform("projection", target);
        model.render();


    }

    public static void renderGUI(Window window) {

    }

    public void addButton(int x, int y) {
        Rectangle bounds = new Rectangle();
        bounds.x = x;
        bounds.y = y;
        //bounds.height =
        //bounds.width =


    }
}