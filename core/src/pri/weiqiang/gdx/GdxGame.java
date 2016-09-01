package pri.weiqiang.gdx;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;

import pri.weiqiang.gdx.elements.EnemyGroupTop;
import pri.weiqiang.gdx.elements.Player;
import pri.weiqiang.gdx.elements.ProjectileFactory;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;//oldGL20
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Filter;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureListener;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;

public class GdxGame implements ApplicationListener, GestureListener {
	/*声音*/
	Sound bing;
	Sound great;
	private Music backgroundMusic;
	private Label label;
	private Vector3 vector3 = new Vector3(0, 0, 0);
	private float centerX;
	private float centerY;
	private float newCenterX;
	private float newCenterY;
	private Player ship;
	/* EnemyGroupTop不是全局变量就黑屏，这里和字体如果每次都重新新建对象调用，就会卡，可以深入研究一下 */
	private EnemyGroupTop enemygroup;
	private Group projectiles;
	private Group explosions;
	private TextureAtlas atlas;
	private float stateTime = 0;
	private Array<AtlasRegion> enemy_image_array;
	/* http://blog.csdn.net/xietansheng/article/details/50187861 */
	private Stage stage;// 舞台
	public static final float WORLD_WIDTH = 480;// 视口世界的宽高统使用 480 * 800,并统一使用伸展视口（StretchViewport）
	public static final float WORLD_HEIGHT = 800;
	public SpriteBatch batch;
	private SimpleDateFormat sDateFormat = new SimpleDateFormat(
			"yyyy-MM-dd hh:mm:ss");
	private String date = sDateFormat.format(new java.util.Date());

	public enum Mode {
		normal, prepare, preview, takePicture, waitForPictureReady,
	}

	public int state_normal = 0;
	public int state_prepare = 1;
	private PerspectiveCamera camera;
	private Mode mode = Mode.normal;
	private final DeviceCameraControl deviceCameraControl;
	private long time_1;
	private long time_2;
	

	/*
	 * 通过this.deviceCameraControl=cameraControl获取Android端摄像头然后后续所有的deviceCameraControl，
	 * 实际调用的都是Android的东西
	 */
	public GdxGame(DeviceCameraControl cameraControl) {
		this.deviceCameraControl = cameraControl;
	}

	@Override
	public void create() {
		/* http://blog.csdn.net/xietansheng/article/details/50187861 */
		// 设置日志输出级别
		Gdx.app.setLogLevel(Application.LOG_DEBUG);
		/* 与stage = new Stage();效果完全不一样，与使用伸展视口（StretchViewport）创建舞台 */
		stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
		LabelStyle labelStyle = new LabelStyle(new BitmapFont(), Color.BLACK); // 创建一个Label样式，使用默认黑色字体
		Label label = new Label("FPS:", labelStyle); // 创建标签，显示的文字是FPS：
		label.setName("fpsLabel"); // 设置标签名称为fpsLabel
		label.setY(0); // 设置Y为0，即显示在最下面
		label.setX(480 - label.getText().length); // 设置X值，显示为最后一个字紧靠屏幕最右侧	
		bing = Gdx.audio.newSound(Gdx.files.internal("sfx/shot.ogg"));
		great=Gdx.audio.newSound(Gdx.files.internal("sfx/bomb.ogg"));
		backgroundMusic = Gdx.audio.newMusic(Gdx.files
				.internal("sfx/music.mp3"));
				backgroundMusic.setLooping(true);//循环播放
				backgroundMusic.setVolume(0.6f);//设置音量
				backgroundMusic.play();//播放
				
		atlas = new TextureAtlas("images/gdxgame.pack");// 填加人物,能使用全局变量一定要使用，不然内存卡死
		ship = new Player(atlas.findRegions("ship")); // 获取图册中的Player.png并创建image对象
		atlas.findRegion("enemy", 0);
		enemy_image_array = atlas.findRegions("enemy");
		enemygroup = new EnemyGroupTop(enemy_image_array);
		stage.addActor(enemygroup);
		ship.setX(WORLD_WIDTH / 2 - ship.getWidth() / 2);
		ship.setY(0);
		stage.addActor(ship); // 将主角添加到舞台
		stage.addActor(label); // 将标签添加到舞台
		batch = new SpriteBatch();
		projectiles = new Group();
		explosions = new Group();
		stage.addActor(projectiles); // 添加飞镖组到舞台
		stage.addActor(explosions);
		/* 只有绑定输入监听器setInputProcessor，GestureListener才会生效 */
		Gdx.input.setInputProcessor(new GestureDetector(this));
		/*android摄像头一开始就进入准备模式*/
		mode = Mode.prepare;
		if (deviceCameraControl != null) {
			deviceCameraControl.prepareCameraAsync();
		}

	}

	/* 手动释放资源 */
	@Override
	public void dispose() {
		/* 对应button，应用退出时释放资源 */
		if (stage != null) {
			stage.dispose();
		}
		batch.dispose();
	}

	/* 通过debug发现，每次点击运行（断点在render内）render都会一直运行，可见render作为渲染的一个覆盖函数的确是处于一直运行的状态 */
	@Override
	public void render() {
			render_preview();
	}

	public void fromGdxGame_backup() {

		Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		/* 在render方法中更新fps的值,放置在stage.act() draw()前后均可 */
		label = (Label) stage.getRoot().findActor("fpsLabel"); // 获取名为fpsLabel的标签,old:Label		
		/*stackoverflow 上有人建议把getFramesPerSecond改为getRawDeltaTime， 果然没有OOM,大概讲的是getFramesPerSecond是获取平均值需要计算，所以相应就会消耗更多时间
		 * http://stackoverflow.com/questions/36814293/is-it-possible-to-wait-for-gc-for-allocation-to-complete*/
//		label.setText("FPS:" + Gdx.graphics.getFramesPerSecond());
		label.setText("FPS:" + Gdx.graphics.getRawDeltaTime());
		label.setX(430 - label.getText().length); // 更新X值以保证显示位置正确性
		stage.act();
		stage.draw();
		// 开始处理飞镖
		Actor[] projectile = projectiles.getChildren().begin();
		Actor[] targets = enemygroup.getChildren().begin();
		// 飞镖
		for (int i = 0; i < projectiles.getChildren().size; i++) {
			Actor actor = projectile[i];
			for (int j = 0; j < enemygroup.getChildren().size; j++) {
				Actor target = targets[j];
				// 敌人到底部消失
				if (target.getY() < 20) {
					enemygroup.removeActor(target);
				}
				// 发生碰撞也会消失
				if (ProjectileFactory.attackAlive(target, actor)) {
					enemygroup.removeActor(target);
					/* 增加爆照场景 */
					explosions.addActor(ProjectileFactory.createExplode(
							atlas.findRegions("explosion"), target.getX(),
							target.getY(), 0));

					projectiles.removeActor(actor);
					great.play();//great.play(volume, pitch, pan)//可以控制左右声道
					break;
				}
			}
		}

		if (explosions.getChildren().size > 5) {
			explosions.clear();
		}
		// 如果飞镖已经飞到顶部则刪除
		Actor[] explosion = explosions.getChildren().begin();
		for (int j = 0; j < explosions.getChildren().size; j++) {
			Actor actor = explosion[j];
			// 时间超时。思路新建一个列表，单独记录时间，并且累计时间，但是感觉不是特别好
			// if () {
			// explosions.removeActor(actor);
			// }
		}

		// 如果飞镖已经飞到顶部则刪除
		projectile = projectiles.getChildren().begin();
		for (int j = 0; j < projectiles.getChildren().size; j++) {
			Actor actor = projectile[j];

			if (!ProjectileFactory.checkAlive(actor)) {
				projectiles.removeActor(actor);
			}
		}
		// 可通过log得到的DeltaTime=0.0166489，fps显示为60，所以二者相乘就是1s，所以刚刚好
		stateTime += Gdx.graphics.getDeltaTime();
		/* 通过控制stateTime的大小控制子弹发射频率，否则子弹处于一直发射状态，连成一片 */
		if (stateTime > 0.5f) {
			stateTime = 0;
			/* 我把bullet换成了enemy，依然没有出现内存泄漏，原因就在生成敌人的代码块而不是图片大小的问题 */
			projectiles.addActor(ProjectileFactory.createProjectile(atlas
					.findRegion("bullet"), ship, new Vector3(ship.getX(), 800,
					0)));
			bing.play();
		}

		if (enemygroup.getChildren().size == 0 & stateTime > 0.5f) { 
			/* 自己边编写的enemygroup.addEnemy直接OOM内存泄漏 */
			// enemygroup.addEnemy(enemy_image);//问题肯定就在addEnemy之中
			/* 使用addActor就不会有问题 */
			enemygroup.addActor(ProjectileFactory.createProjectile_Enemy_down(
					atlas.findRegions("enemy"),
					new Vector3(ship.getX(), 800, 0)));
			Gdx.app.log("enemygroup:1,size:",
					String.valueOf(enemygroup.getChildren().size));
		}

		if (enemygroup.getChildren().size == 0 & stateTime > 0.25f) {
			enemygroup.addActor(ProjectileFactory
					.createProjectile_Enemy_up(atlas.findRegions("enemy")));
		}
	}

	public void render_preview() {
		/* 我已经将preview变为takePicture状态移动到click中，实现先预览再拍照 */
		Gdx.gl20.glHint(GL20.GL_GENERATE_MIPMAP_HINT, GL20.GL_NICEST);
		if (mode == Mode.takePicture) {
			/* 没有清屏，摄像头的预览功能就没有，演员和舞台在各个if中可以不加 */
			Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
			if (deviceCameraControl != null) {
				deviceCameraControl.takePicture();
			}
			mode = Mode.waitForPictureReady;
		} else if (mode == Mode.waitForPictureReady) {
			Gdx.gl20.glClearColor(0.0f, 0f, 0.0f, 0.0f);
		} else if (mode == Mode.prepare) {
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0f, 0.6f);
			if (deviceCameraControl != null) {
				if (deviceCameraControl.isReady()) {
					deviceCameraControl.startPreviewAsync();
					mode = Mode.preview;

				}
			}
		} else if (mode == Mode.preview) {
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 0f);
		} else {
			/* mode = normal */
			Gdx.gl20.glClearColor(0.0f, 0.0f, 0.6f, 1.0f);

		}
		Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		//清屏后绘制飞机
		fromGdxGame_backup();
		Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE);
		Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
		Gdx.gl20.glEnable(GL20.GL_LINE_LOOP);
		Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl20.glClearDepthf(1.0F);
		camera.update(true);
		if (mode == Mode.waitForPictureReady) {
			/*
			 * 注意deviceCameraControl.getPictureData()得到的是byte[]，可见整体思路就是，
			 * 将Android摄像头得到byte[],然后
			 * 将byte[]转换为Pixmap，最后将pixmap存为jpg,这样不适用Android端图片保存模式，
			 * byte[]----Pixmap----jpg
			 */
			if (deviceCameraControl.getPictureData() != null) {
				// camera picture was actually takentake Gdx Screenshot
				Pixmap screenshotPixmap = getScreenshot(0, 0,
						Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				/* 开始报错deviceCameraControl.getPictureData一直未null */
				Pixmap cameraPixmap = new Pixmap(
						deviceCameraControl.getPictureData(), 0,
						deviceCameraControl.getPictureData().length);
				merge2Pixmaps(cameraPixmap, screenshotPixmap);
				// we could call PixmapIO.writePNG(pngfile, cameraPixmap);
				/* 仅保存screenshot，对同一时间的图片进行保存然后进行比较 */
				Pixmap screenshotPixmap_test = getScreenshot(0, 0,
						Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
				FileHandle jpgfile_screenshot = Gdx.files
						.external("a_SDK_fail/libGdxSnapshot" + "_" + date
								+ "_screenshot.jpg");
				deviceCameraControl.saveAsJpeg(jpgfile_screenshot,
						screenshotPixmap_test);
				/* 仅保存cameraPixma，对同一时间的图片进行保存然后进行比较 */
				Pixmap cameraPixmap_test = new Pixmap(
						deviceCameraControl.getPictureData(), 0,
						deviceCameraControl.getPictureData().length);

				FileHandle jpgfile_cameraPixmap = Gdx.files
						.external("a_SDK_fail/libGdxSnapshot" + "_" + date
								+ "_camera.jpg");
				deviceCameraControl.saveAsJpeg(jpgfile_cameraPixmap,
						cameraPixmap_test);

				/* 保存混合之后的相片 */
				FileHandle jpgfile = Gdx.files
						.external("a_SDK_fail/libGdxSnapshot" + "_" + date
								+ ".jpg");
				Gdx.app.log("FileHandle", date);
				time_1 = System.currentTimeMillis();
				deviceCameraControl.saveAsJpeg(jpgfile, cameraPixmap);
				time_2 = System.currentTimeMillis();
				/* 可以得到35830ms=35s，所以非常忙，导致Mode非常缓慢的回到Mode.normal */
				Gdx.app.log("cost", String.valueOf(time_2 - time_1));
				deviceCameraControl.stopPreviewAsync();
				/* 保存文件后，mode回到normal继续render循环，所以中间停顿的其实是卡住了？！ */
				mode = Mode.normal;

			}
		}

	}
	/* 注意截图与Android设备摄像传回的图像整合时并非按我所看的视角进行 */
	private Pixmap merge2Pixmaps(Pixmap mainPixmap, Pixmap overlayedPixmap) {
		// merge to data and Gdx screen shot - but fix Aspect Ratio issues
		// between the screen and the camera
		Pixmap.setFilter(Filter.BiLinear);
		float mainPixmapAR = (float) mainPixmap.getWidth()
				/ mainPixmap.getHeight();
		float overlayedPixmapAR = (float) overlayedPixmap.getWidth()
				/ overlayedPixmap.getHeight();
		if (overlayedPixmapAR < mainPixmapAR) {
			int overlayNewWidth = (int) (((float) mainPixmap.getHeight() / overlayedPixmap
					.getHeight()) * overlayedPixmap.getWidth());
			int overlayStartX = (mainPixmap.getWidth() - overlayNewWidth) / 2;
			// Overlaying pixmaps
			mainPixmap.drawPixmap(overlayedPixmap, 0, 0,
					overlayedPixmap.getWidth(), overlayedPixmap.getHeight(),
					overlayStartX, 0, overlayNewWidth, mainPixmap.getHeight());
		} else {
			int overlayNewHeight = (int) (((float) mainPixmap.getWidth() / overlayedPixmap
					.getWidth()) * overlayedPixmap.getHeight());
			int overlayStartY = (mainPixmap.getHeight() - overlayNewHeight) / 2;
			// Overlaying pixmaps
			mainPixmap.drawPixmap(overlayedPixmap, 0, 0,
					overlayedPixmap.getWidth(), overlayedPixmap.getHeight(), 0,
					overlayStartY, mainPixmap.getWidth(), overlayNewHeight);
		}
		return mainPixmap;
	}

	public Pixmap getScreenshot(int x, int y, int w, int h, boolean flipY) {

		Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
		final Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
		ByteBuffer pixels = pixmap.getPixels();
		Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE,
				pixels);

		final int numBytes = w * h * 4;
		byte[] lines = new byte[numBytes];
		if (flipY) {
			final int numBytesPerLine = w * 4;
			for (int i = 0; i < h; i++) {
				pixels.position((h - i - 1) * numBytesPerLine);
				pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
			}
			pixels.clear();
			pixels.put(lines);
		} else {
			pixels.clear();
			pixels.get(lines);
		}

		return pixmap;
	}

	@Override
	public void resize(int width, int height) {
		camera = new PerspectiveCamera(67.0f, 2.0f * width / height, 2.0f);
		camera.far = 100.0f;
		camera.near = 0.1f;
		camera.position.set(2.0f, 2.0f, 2.0f);
		camera.lookAt(0.0f, 0.0f, 0.0f);

	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	ClickListener preview_on = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			Gdx.app.log("preview_on", "preview_on按钮被点击了");
			if (mode == Mode.waitForPictureReady) {
				mode = Mode.normal;
			}

			if (mode == Mode.normal) {
				mode = Mode.prepare;
				if (deviceCameraControl != null) {
					deviceCameraControl.prepareCameraAsync();
				}
			}
		}

	};

	ClickListener actor_move = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {

			// firstActor.setX(firstActor.getX() + 55);

			Gdx.app.log("actor_move", "actor_move按钮被点击了");
		}

	};

	ClickListener preview_on_1 = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			Gdx.app.log("preview_on_1", "preview_on_1按钮被点击了");
			if (mode == Mode.preview) {
				mode = Mode.takePicture;
			}

		}

	};

	/* 实现接口GestureListener */
	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		/*全部使用全局变量，避免经常出现GC_FOR_ALLOC freed： Vector3 vector3 = new Vector3(x, y, 0);*/		
		vector3.set(x, y, 0);
		stage.getCamera().unproject(vector3); // 坐标转化
		projectiles.addActor(ProjectileFactory.createProjectile(
				atlas.findRegion("missile"), ship, vector3)); // 添加新飞镖到飞镖组		
		return false;

	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
		return false;

	}

	/* deltaX:delta即增量，也就是速度 */
	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		// TODO Auto-generated method stub
//		Gdx.app.log("pan", "x:" + x + ",y:" + y + ",deltaX" + deltaX
//				+ ",deltaY:" + deltaY);
		centerX = ship.getX() + ship.getWidth() / 2f;
		centerY = ship.getY() + ship.getHeight() / 2f;
		newCenterX = centerX + deltaX;
		newCenterY = centerY - deltaY;

		if (newCenterX > 0 && newCenterX < Gdx.graphics.getWidth()) {
			ship.setX(ship.getX() + deltaX);//ship
		}
		if (newCenterY > 0 && newCenterY < Gdx.graphics.getHeight()) {
			ship.setY(ship.getY() - deltaY);
		}
		return false;

	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2,
			Vector2 pointer1, Vector2 pointer2) {
		// TODO Auto-generated method stub
		return false;

	}

	@Override
	public void pinchStop() {
		// TODO Auto-generated method stub

	}

}
