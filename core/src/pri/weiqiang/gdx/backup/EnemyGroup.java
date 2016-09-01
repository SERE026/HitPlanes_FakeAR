package pri.weiqiang.gdx.backup;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
/**
 * @author  54wall 
 * @date 创建时间：2016-7-22 下午4:37:30
 * @version 1.0 
 * https://github.com/htynkn/DartsShaSha/blob/master/core/src/com/huangyunkun/controller/TargetController.java
 */
public class EnemyGroup extends Group {
	
	/*下边是生成右侧*/
	public EnemyGroup(AtlasRegion region) {
		super();
		int minY = 0;
		/*maxY注意与长宽要一致，否则会出现黑屏（无）*/
        int maxY = (int) (800 - region.getRegionHeight());
        int tempY = MathUtils.random(minY, maxY);
		tempY = 0;
        for (int i = 0; i < 3; i++) {
            Image image = new Image(region);
            /*setX注意与长宽要一致*/
            image.setX(480 - image.getWidth());
            
            // 开始判断Y值是否符合要求
            boolean flag = false;
            do {
                flag = false;
                tempY = MathUtils.random(minY, maxY); // 生成Y值
 
                Actor[] actors = this.getChildren().begin(); // 获取当前已有的怪兽对象
                for (int j = 0; j < this.getChildren().size; j++) {
                    Actor tempActor = actors[j];
                    if (tempY == tempActor.getY()) { // 如果Y值相等，比如重合，所以舍弃,重新生成
                        flag = true;
                        break;
                    } else if (tempY < tempActor.getY()) { // 如果生成的Y值小于当前怪兽的Y值，则判断生成的Y值加上高度后是否合适
                        if ((tempY + region.getRegionHeight()) >= tempActor
                                .getY()) {
                            flag = true;
                            break;
                        }
                    } else { // 如果生成的Y值大于当前怪兽的Y值，则判断当前怪兽的Y值加上高度后是否合适
                        if (tempY <= (tempActor.getY() + region
                                .getRegionHeight())) {
                            flag = true;
                            break;
                        }
                    }
                }
            } while (flag);
            image.setY(tempY);
            this.addActor(image); //添加到组中
	}
}
	
}
