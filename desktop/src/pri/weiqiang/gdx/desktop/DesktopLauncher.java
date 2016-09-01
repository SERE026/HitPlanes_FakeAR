package pri.weiqiang.gdx.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import pri.weiqiang.gdx.GdxGame_backup;
import com.badlogic.gdx.tools.texturepacker.TexturePacker;
import com.badlogic.gdx.tools.texturepacker.TexturePacker.Settings;

public class DesktopLauncher {
	
	private static boolean rebuildAtlas = false;
	private static boolean drawDebugOutline = false;

	public static void main (String[] arg) {
		
		
		if (rebuildAtlas) {
			Settings settings = new Settings();
			settings.maxWidth = 1024;
			settings.maxHeight = 1024;
			settings.duplicatePadding = false;
			/*控制碰撞粉红色框框*/
			settings.debug = drawDebugOutline;
			TexturePacker.process(settings, 
					"assets-raw/images", 
					"../GdxGame-android/assets/images",
					"gdxgame.pack");
			}
		
		
		
		
		
		
		
		
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "GDXGame";
		config.width = 480;
		config.height = 800;
		new LwjglApplication(new GdxGame_backup(), config);
	}
}
