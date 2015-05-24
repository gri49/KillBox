//Copyright (C) 2014-2015 Alexandre-Xavier Labonté-Lamoureux
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;

public class Camera
{
	Player Plyr;

	// Postions inside the camera are as represented in OpenGL
	// The axis are inverted, thus a positive value is a negative value
	private float PosX;
	private float PosY;
	private float PosZ;
	private float RotX;
	private float RotY;
	private float RotZ;

	private float FOV;
	private float Aspect;
	private float Near;
	private float Far;

	public Texture CurrentTexture = null;
	public int[] TextXcoords = {0, 1, 1, 0};    // CLEAN ME
	public int[] TextYcoords = {0, 0, 1, 1};    // CLEAN ME
	public boolean TextureFiltered = false;

	private boolean HasControl = false;

	// Key presses
	private boolean JustPressedFilterKey = false;
	private boolean JustPressedMouseGrabKey = false;

	// Mouse movement
	private short MouseTurnH;

	public Camera(Player Plyr, float FOV, float Aspect, float Near, float Far)
	{
		this.Plyr = Plyr;

		PosX = Plyr.PosY;
		PosY = Plyr.PosZ + Plyr.ViewZ;   // Internally, the player's Z is the height.
		PosZ = Plyr.PosX;   // But in OpenGL, Z is vertical like the internal Y.
		RotX = 0;
		RotY = Plyr.GetDegreeAngle();
		RotZ = 0;

		this.FOV = FOV;
		this.Aspect = Aspect;
		this.Near = Near;
		this.Far = Far;
		InitProjection();

		//Door.Bind();    // CLEAN ME
	}

	// Sets the perspective without glu, so let's call it glPerspective. 
	private void glPerspective(float FOV, float Aspect, float Near, float Far)
	{
		// This replaces 'gluPerspective'. 
		float FH = (float) Math.tan(FOV / 360 * Math.PI) * Near;
		float FW = FH * Aspect;

		// Sets the Frustum to perspective mode. 
		glFrustum(-FW, FW, -FH, FH, Near, Far);
	}

	private void InitProjection()
	{
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glPerspective(FOV, Aspect, Near, Far);
		glMatrixMode(GL_MODELVIEW);
	}

	public void UseView()
	{
		//glRotatef(RotX, 1, 0, 0);
		glRotatef(RotY, 0, -1, 0);
		//glRotatef(RotZ, 0, 0, 1);
		//glTranslatef(PosX, PosY, PosZ);
	}

	public void UpdateCamera()
	{
		PosX = Plyr.PosY;
		PosY = Plyr.PosZ + Plyr.ViewZ;
		PosZ = Plyr.PosX;
		RotY = Plyr.GetDegreeAngle();
	}

	public void ChangePlayer(Player Plyr, boolean CanControl)
	{
		this.Plyr = Plyr;
		HasControl = CanControl;
	}

	public void ChangeProperties(float FOV, float Aspect, float Near, float Far)
	{
		this.FOV = FOV;
		this.Aspect = Aspect;
		this.Near = Near;
		this.Far = Far;
	}

	public Player CurrentPlayer()
	{
		return Plyr;
	}

	public void Render(Level Lvl, ArrayList<Player> Players)
	{
		if (Display.wasResized())
		{
			// Set the camera's properties
			this.ChangeProperties(this.FOV, (float) Display.getWidth() / (float) Display.getHeight(), this.Near, this.Far);
			// Set the view's canvas
			glViewport(0, 0, Display.getWidth(), Display.getHeight());
		}

		glClearColor(0.0f, 0.0f, 0.5f, 0.0f);   // RGBA background color
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Clear the color buffer and the depth buffer
		glLoadIdentity();
		this.UseView(); // Rotation matrices for the view

		if (Keyboard.isKeyDown(Keyboard.KEY_F1) && !JustPressedMouseGrabKey)
		{
			JustPressedMouseGrabKey = true;

			if (!Mouse.isGrabbed())
			{
				Mouse.setGrabbed(true); // Hide mouse
			}
			else
			{
				Mouse.setGrabbed(false);
			}

			Mouse.setCursorPosition(Display.getWidth() / 2, Display.getHeight() / 2);
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_F1))
		{
			JustPressedMouseGrabKey = true;
		}
		else
		{
			JustPressedMouseGrabKey = false;
		}

		if (Mouse.isGrabbed())
		{
			MouseTurnH = (short) Mouse.getDX();
		}
		else
		{
			MouseTurnH = 0;
		}

		// This will only be changed the next time a level will load
		if (Keyboard.isKeyDown(Keyboard.KEY_F5) && !JustPressedFilterKey)
		{
			if (TextureFiltered)
			{
				//Door = new Texture("Stuff/DOOR9_1.bmp", GL_NEAREST);    // Only to test
				TextureFiltered = false;
			}
			else
			{
				//Door = new Texture("Stuff/DOOR9_1.bmp", GL_LINEAR);    // Only to test
				TextureFiltered = true;
			}

			//Door.Bind();
			JustPressedFilterKey = true;
		}
		else if (Keyboard.isKeyDown(Keyboard.KEY_F5))
		{

			JustPressedFilterKey = true;
		}
		else
		{
			JustPressedFilterKey = false;
		}

		if (HasControl)    // If I am this player
		{
			CurrentPlayer().AngleTurn((short) -(MouseTurnH * 20));

			if (Keyboard.isKeyDown(Keyboard.KEY_W) || Keyboard.isKeyDown(Keyboard.KEY_UP))
			{
				CurrentPlayer().ForwardMove(1);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_S) || Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			{
				CurrentPlayer().ForwardMove(-1);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_A))
			{
				CurrentPlayer().LateralMove(-1);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_D))
			{
				CurrentPlayer().LateralMove(1);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			{
				CurrentPlayer().AngleTurn((short) 500);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			{
				CurrentPlayer().AngleTurn((short) -500);
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			{
				CurrentPlayer().MoveUp();
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL))
			{
				CurrentPlayer().MoveDown();
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
			{
				System.exit(0);
			}
			CurrentPlayer().Move();
		}
		this.UpdateCamera();

		// Print DEBUG stats
		System.out.println("X: " + (int) CurrentPlayer().PosX() + "	Y: " + (int) CurrentPlayer().PosY() + "	Z: " + (int) CurrentPlayer().PosZ()
				+ "	Ra: " + CurrentPlayer().GetRadianAngle() + "	Cam: " + this.RotY()
				+ "	dX: " + MouseTurnH + "	dY: " + Mouse.getDY() + "	MoX: " + CurrentPlayer().MoX() + "	MoY: " + CurrentPlayer().MoY
				+ "	MoA: " + (float) Math.atan2(CurrentPlayer().MoY(), CurrentPlayer().MoX()));

		if (Lvl != null)
		{
			for (int Plane = 0; Plane < Lvl.Planes.size(); Plane++)
			{
				//CurrentTexture = new Texture("Stuff/" + Lvl.Planes.get(Plane).TextureName, GL_NEAREST);
				//CurrentTexture.Bind();
				Lvl.Planes.get(Plane).Bind();

				if (Lvl.Planes.get(Plane).Reference != null)
				{
					Lvl.Planes.get(Plane).Reference.Bind();
				}

				if (Lvl.Planes.get(Plane).TwoSided())
				{
					glDisable(GL_CULL_FACE);
				}

				glPushMatrix();
				{
					// Apply color to polygons
					glColor3f(1.0f, 1.0f, 1.0f);
					// Draw polygons according to the camera position
					glTranslatef(this.PosX(), this.PosY(), this.PosZ());
					glBegin(GL_QUADS);
					{
						for (int Vertex = 0; Vertex < Lvl.Planes.get(Plane).Vertices.size(); Vertex += 3)
						{
							glTexCoord2f(TextXcoords[Vertex / 3], TextYcoords[Vertex / 3]);
							// (Ypos, Zpos, Xpos)
							glVertex3f(-Lvl.Planes.get(Plane).Vertices.get(Vertex + 1),	// There a minus here to flip the Y axis
									Lvl.Planes.get(Plane).Vertices.get(Vertex + 2),	// The Z axis is not flipped because the world will be upside down
									-Lvl.Planes.get(Plane).Vertices.get(Vertex));	// There a minus here to flip the X axis
						}
					}
					glEnd();
				}
				glPopMatrix();

				if (Lvl.Planes.get(Plane).TwoSided())
				{
					glEnable(GL_CULL_FACE);
				}

				//glPopMatrix();
			}
		}

		Display.update();
	}

	public float PosX()
	{
		return PosX;
	}

	public float PosY()
	{
		return -PosY;
	}

	public float PosZ()
	{
		return PosZ;
	}

	public void PosX(float PosX)
	{
		this.PosX = PosX;
	}

	public void PosY(float PosY)
	{
		this.PosY = -PosY;
	}

	public void PosZ(float PosZ)
	{
		this.PosZ = PosZ;
	}

	public float RotX()
	{
		return RotX;
	}

	public float RotY()
	{
		return RotY;
	}

	public float RotZ()
	{
		return RotZ;
	}

	public void RotX(float RotX)
	{
		this.RotX = RotX;
	}

	public void RotY(float RotY)
	{
		this.RotY = RotY;
	}

	public void RotZ(float RotZ)
	{
		this.RotZ = RotZ;
	}
}