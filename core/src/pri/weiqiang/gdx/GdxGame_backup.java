package pri.weiqiang.gdx;

import pri.weiqiang.gdx.elements.EnemyGroupTop;
import pri.weiqiang.gdx.elements.Player;
import pri.weiqiang.gdx.elements.ProjectileFactory;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;

/*http://www.oschina.net/question/565065_134673?fromerr=HvUTQ98a
 *http://www.huangyunkun.com/2013/02/17/libgdx-game-2/*/
public class GdxGame_backup extends ApplicationAdapter implements GestureListener {
	
	private Label label;
	private Vector3 vector3 = new Vector3(0, 0, 0);
	private float centerX;
	private float centerY;
	private float newCenterX;
	private float newCenterY;
	private SpriteBatch batch;
	private Player ship;
	private Stage stage;
	/* EnemyGroupTop不是全局变量就黑屏，这里和字体如果每次都重新新建对象调用，就会卡，可以深入研究一下 */
	private EnemyGroupTop enemygroup;
	
	private Group projectiles;
	private Group explosions;
	private TextureAtlas atlas;
	public static final float WORLD_WIDTH = 480;
	public static final float WORLD_HEIGHT = 800;// 960
	private OrthographicCamera camera; // 正交相机
	private float stateTime=0;
	private Array<AtlasRegion> enemy_image_array;

	@Override
	public void create() {
		
		Gdx.app.setLogLevel(Application.LOG_DEBUG);// 设置日志输出级别		
		camera = new OrthographicCamera(480, 800);// 初始化游戏相机
		camera.position.set(480 / 2, 800 / 2, 0);
		// camera.setToOrtho(false, WORLD_WIDTH, WORLD_HEIGHT);
		/* 效果完全不一样，与使用伸展视口（StretchViewport）创建舞台 */// stage = new Stage();		
		stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
		LabelStyle labelStyle = new LabelStyle(new BitmapFont(), Color.BLACK); // 创建一个Label样式，使用默认黑色字体
		Label label = new Label("FPS:", labelStyle); // 创建标签，显示的文字是FPS：
		label.setName("fpsLabel"); // 设置标签名称为fpsLabel
		label.setY(0); // 设置Y为0，即显示在最下面
		label.setX(480 - label.getText().length); // 设置X值，显示为最后一个字紧靠屏幕最右侧
		// 填加人物,能使用全局变量一定要使用，不然内存卡死
		atlas = new TextureAtlas("images/gdxgame.pack");
		ship = new Player(atlas.findRegions("ship")); // 获取图册中的Player.png并创建image对象		
		atlas.findRegion("enemy", 0);
		enemy_image_array=atlas.findRegions("enemy");		
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

	}

	/* 关注一下什么时候调用的dispose，感觉它和render一样也是不断的在调用 */
	@Override
	public void dispose() {
		if (stage != null) {
			stage.dispose();
		}
		batch.dispose();		
		Gdx.app.log("dispose", "1");//*只有退出Activity这里才会显示*/

	}
	/*render相当于一个循环，所以尽量在内部使用行政变量*/
	@Override
	public void render() {
//		System.gc();
		Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		/* 在render方法中更新fps的值,放置在stage.act() draw()前后均可 */
		label = (Label) stage.getRoot().findActor("fpsLabel"); // 获取名为fpsLabel的标签,old:Label label……
		label.setText("FPS:" + Gdx.graphics.getFramesPerSecond());
		label.setX(430 - label.getText().length); // 更新X值以保证显示位置正确性
		stage.act();
		stage.draw();		
		// 开始处理飞镖
		Actor[] projectile = projectiles.getChildren().begin();
		Actor[] targets = enemygroup.getChildren().begin();
		//飞镖
		for (int i = 0; i < projectiles.getChildren().size; i++) {
			Actor actor = projectile[i];
			for (int j = 0; j < enemygroup.getChildren().size; j++) {
				Actor target = targets[j];								
				//敌人到底部消失
				if (target.getY()<20) {
					enemygroup.removeActor(target);
				}
				//发生碰撞也会消失
				if (ProjectileFactory.attackAlive(target, actor)) {
					enemygroup.removeActor(target);
					/*增加爆照场景*/
					explosions.addActor(ProjectileFactory.createExplode(
							atlas.findRegions("explosion"), target.getX(),target.getY(),0));
					
					projectiles.removeActor(actor);
					break;
				}
			}
		}
		
		if (explosions.getChildren().size>5) {
			explosions.clear();
		}
		// 如果飞镖已经飞到顶部则刪除
		Actor[] explosion = explosions.getChildren().begin();
		for (int j = 0; j < explosions.getChildren().size; j++) {
			Actor actor = explosion[j];
//			时间超时。思路新建一个列表，单独记录时间，并且累计时间，但是感觉不是特别好
//			if () {
//				explosions.removeActor(actor);
//			}
		}
		
		
		
		// 如果飞镖已经飞到顶部则刪除
		projectile = projectiles.getChildren().begin();
		for (int j = 0; j < projectiles.getChildren().size; j++) {
			Actor actor = projectile[j];
			
			if (!ProjectileFactory.checkAlive(actor)) {
				projectiles.removeActor(actor);
			}
		}
		 //可通过log得到的DeltaTime=0.0166489，fps显示为60，所以二者相乘就是1s，所以刚刚好
        stateTime += Gdx.graphics.getDeltaTime(); 
        /*通过控制stateTime的大小控制子弹发射频率，否则子弹处于一直发射状态，连成一片*/
        if (stateTime > 1f)
        {
            stateTime = 0;
            /*我把bullet换成了enemy，依然没有出现内存泄漏，原因就在生成敌人的代码块而不是图片大小的问题*/
            projectiles.addActor(ProjectileFactory.createProjectile(
            		atlas.findRegion("bullet"), ship, new Vector3(ship.getX(), 800, 0))); 
        }
   
        if (enemygroup.getChildren().size ==0&stateTime>0.5f)
        {	/*在敌机消失的一瞬间，直接内存溢出,自己建立的方法，看来没有考虑到GC，所以直接使用group自己的方法addActor
        	Adds an actor as a child of this group. The actor is first removed from its parent group, if any. 
					public void addActor (Actor actor) {
					if (actor.parent != null) actor.parent.removeActor(actor, false);
					children.add(actor);
					actor.setParent(this);
					actor.setStage(getStage());
					childrenChanged();
			}可以看到官方的group类中可不仅仅是添加image那么一句，虽然使用中还是有内存溢出，但是概率大幅降低			
			*/
        	/*自己边编写的enemygroup.addEnemy直接OOM内存泄漏*/
//        	enemygroup.addEnemy(enemy_image);//问题肯定就在addEnemy之中
        	/*使用addActor就不会有问题*/
        	enemygroup.addActor(ProjectileFactory.createProjectile_Enemy_down(
            		atlas.findRegions("enemy"), new Vector3(ship.getX(), 800, 0)));       	
        	Gdx.app.log("enemygroup:1,size:",String.valueOf(enemygroup.getChildren().size));
        }
        
        if (enemygroup.getChildren().size ==0&stateTime>0.25f)
        {	
        	enemygroup.addActor(ProjectileFactory.createProjectile_Enemy_up(
        			atlas.findRegions("enemy")));       	
        }
	}

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
//		Gdx.app.log("tap", "x:" + x + ",y:" + y);
		return false;

	}

	@Override
	public boolean longPress(float x, float y) {
		// TODO Auto-generated method stub
//		Gdx.app.log("longPress", "x:" + x + ",y:" + y);
		return false;

	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		// TODO Auto-generated method stub
//		Gdx.app.log("fling", "velocityX:" + velocityX + ",velocityY:"
//				+ velocityY);
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
//		Gdx.app.log("panStop", "x:" + x + ",y:" + y);
		return false;

	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		// TODO Auto-generated method stub
		Gdx.app.log("zoom", "zoom");
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
