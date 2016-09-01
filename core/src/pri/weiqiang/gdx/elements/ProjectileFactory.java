package pri.weiqiang.gdx.elements;
/**
 * @author  54wall 
 * @date 创建时间：2016-7-23 下午3:28:35
 * @version 1.0 
 */
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;

public class ProjectileFactory {
	static int x=20;
	static int y=20;
	public static Image createProjectile(AtlasRegion region, Actor man,
			Vector3 target) {
		Image image = new Image(region);
		image.setX(man.getX() + man.getWidth() / 2);
		image.setY(man.getY() + man.getHeight() / 2);
		image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
//		image.addAction(Actions.repeat(50, Actions.rotateBy(360, 0.5f))); // 设置飞镖的旋转
//		image.addAction(Actions.moveTo(target.x, target.y, 2f)); // 设置飞镖的移动
		image.setRotation(180);//调整图像角度
		image.addAction(Actions.repeat(50, Actions.rotateBy(0, 0.5f))); // 不旋转
		image.addAction(Actions.moveTo(target.x, target.y, 5f)); //moveTo中的duration为速度
		return image;
	}

	public static Boolean checkAlive(Actor projectile) {
		if (projectile.getActions().size == 1) {
			return false;
		}
		return true;
	}

	public static Boolean attackAlive(Actor target, Actor projectile) {
		Rectangle rectangle = new Rectangle(target.getX(), target.getY(),
				target.getWidth(), target.getHeight()); // 创建一个矩形
		return rectangle.contains(
				projectile.getX() + projectile.getWidth() / 2,
				projectile.getY() + projectile.getHeight() / 2); //判断是否在矩阵中，即是否击中
	}
	
	/*敌机从下面飞来*/
	public static Scythe createProjectile_Enemy_down(Array<AtlasRegion> region,
			Vector3 target) {
		Scythe image = new Scythe(region);
		image.setX(MathUtils.random(20, 450));
		image.setY(250);
		image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
		image.setRotation(180);//调整图像角度无效		
		image.addAction(Actions.repeat(50, Actions.rotateBy(0, 0.5f))); // 不旋转
		image.addAction(Actions.moveTo(target.x, target.y, 5f)); //moveTo中的duration为速度
		return image;
	}
	/*敌机从上面面飞来*/
	public static Scythe createProjectile_Enemy_up(Array<AtlasRegion> region
			) {
		Scythe image = new Scythe(region);
		x=MathUtils.random(20, 450);
		image.setX(x);
		image.setY(550);
		image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);
		image.addAction(Actions.repeat(50, Actions.rotateBy(0, 0.5f))); // 不旋转
		image.addAction(Actions.moveTo(x, 0, 5f)); //moveTo中的duration为速度
		return image;
	}
	
	/*产生爆炸*/
	public static Explode createExplode(Array<AtlasRegion> region,float x,float y,float time) {
		Explode image = new Explode(region);
		image.time_explode=time;
		image.setX(x);
		image.setY(y);
		image.setOrigin(image.getWidth() / 2, image.getHeight() / 2);	
		image.addAction(Actions.repeat(50, Actions.rotateBy(0, 0.5f))); // 不旋转
		return image;
	}
}
