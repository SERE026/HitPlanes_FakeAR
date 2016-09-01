package pri.weiqiang.gdx.elements;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
/**
 * @author  54wall 
 * @date 创建时间：2016-7-22 下午4:37:30
 * @version 1.0 
 * https://github.com/htynkn/DartsShaSha/blob/master/core/src/com/huangyunkun/controller/TargetController.java
 */
public class EnemyGroupTop extends Group {
	
	int minX = 0;
    int maxX = 0;
    int tempX =0;
	public EnemyGroupTop(Array<AtlasRegion> region) {
		super();
		minX = 0;
		/*maxY注意与长宽要一致，否则会出现黑屏（无）比如把480改成800*/
        maxX = (int) (480 - region.get(0).getRegionHeight());
        tempX = MathUtils.random(minX, maxX);
		tempX = 0;
		
        for (int i = 0; i < 3; i++) {
        	Scythe image = new Scythe(region);
            /*setX注意与长宽要一致*/
            image.setY(800 - image.getHeight());            
            // 开始判断X值是否符合要求
            boolean flag = false;
            do {
                flag = false;
                tempX = MathUtils.random(minX, maxX); // 生成X值
 
                Actor[] actors = this.getChildren().begin(); // 获取当前已有的怪兽对象
                for (int j = 0; j < this.getChildren().size; j++) {
                    Actor tempActor = actors[j];
                    if (tempX == tempActor.getX()) { // 如果X值相等，比如重合，所以舍弃,重新生成
                        flag = true;
                        break;
                    } else if (tempX < tempActor.getX()) { // 如果生成的X值小于当前怪兽的X值，则判断生成的Y值加上高度后是否合适
                        if ((tempX + region.get(0).getRegionHeight()) >= tempActor
                                .getX()) {
                            flag = true;
                            break;
                        }
                    } else { // 如果生成的X值大于当前怪兽的Y值，则判断当前怪兽的X值加上高度后是否合适
                        if (tempX <= (tempActor.getX() + region.get(0)
                                .getRegionHeight())) {
                            flag = true;
                            break;
                        }
                    }
                }
            } while (flag);
            image.setX(tempX);
//            this.AddMove(image, MathUtils.random(3f, 8f)); //怪兽移动效果
            this.AddMove(image, MathUtils.random(6f, 16f)); //怪兽移动效果
            this.addActor(image); //添加到组中
	}
}
	/*增加移动*/
	public void AddMove(Actor actor, float time) {
        actor.addAction(Actions.moveTo(actor.getX(), 0, time));
    }
	
	public void addEnemy(AtlasRegion region) {

		
		minX = 0;
		/*maxY注意与长宽要一致，否则会出现黑屏（无）比如把480改成800*/
        maxX = (int) (480 - region.getRegionHeight());
        tempX = MathUtils.random(minX, maxX);
		tempX = 0;
		
        for (int i = 0; i < 3; i++) {
            Image image = new Image(region);
            /*setX注意与长宽要一致*/
            image.setY(800 - image.getHeight());
            
            // 开始判断X值是否符合要求
            boolean flag = false;
            do {
                flag = false;
                tempX = MathUtils.random(minX, maxX); // 生成X值
 
                Actor[] actors = this.getChildren().begin(); // 获取当前已有的怪兽对象
                for (int j = 0; j < this.getChildren().size; j++) {
                    Actor tempActor = actors[j];
                    if (tempX == tempActor.getX()) { // 如果X值相等，比如重合，所以舍弃,重新生成
                        flag = true;
                        break;
                    } else if (tempX < tempActor.getX()) { // 如果生成的X值小于当前怪兽的X值，则判断生成的Y值加上高度后是否合适
                        if ((tempX + region.getRegionHeight()) >= tempActor
                                .getX()) {
                            flag = true;
                            break;
                        }
                    } else { // 如果生成的X值大于当前怪兽的Y值，则判断当前怪兽的X值加上高度后是否合适
                        if (tempX <= (tempActor.getX() + region
                                .getRegionHeight())) {
                            flag = true;
                            break;
                        }
                    }
                }
            } while (flag);
            image.setX(tempX);
            /*this就是指的当前对象，所以是group*/
            
            this.AddMove(image, MathUtils.random(6f, 16f)); //怪兽移动效果
            this.addActor(image); //添加到组中
	}

		
		
	}
	
	
	
}
