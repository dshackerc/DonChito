package mx.itesm.donchito;


import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

public class DonChito extends Game {
	public static final float ALTO_MUNDO = 720;
	public static final float ANCHO_MUNDO = 1280;
    public  static final AssetManager assetManager = new AssetManager();

	@Override
	public void create() {
		DonChito.assetManager.setLoader(TiledMap.class,
                new TmxMapLoader(new InternalFileHandleResolver()));
		this.setScreen(new LoadingScreen(LoadingScreen.ScreenSel.CUEVA, this));
	}

	@Override
	public void setScreen(Screen screen) {
		super.setScreen(screen);
	}
    @Override
    public void dispose() {
        super.dispose();
        DonChito.assetManager.clear();
    }
}
