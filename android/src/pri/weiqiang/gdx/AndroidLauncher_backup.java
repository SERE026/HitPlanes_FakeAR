package pri.weiqiang.gdx;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.WindowManager;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import pri.weiqiang.gdx.GdxGame_backup;

public class AndroidLauncher_backup extends AndroidApplication {
	@Override
	protected void onCreate (Bundle savedInstanceState) {
		/*我没有看到其他的程序在Android中进行了横竖屏的设置，不知道他们怎么做到的*/
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//通过程序改变屏
		super.onCreate(savedInstanceState);
		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new GdxGame_backup(), config);
	}
}
