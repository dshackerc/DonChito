package mx.itesm.donchito;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class RomanStruggle implements Screen {
    private OrthographicCamera camera;
    private final DonChito game;
    private Viewport view;

    private int nivel = 1;
    private int botonPresionado = 0;

    private float velocidad = 1f;

    private SimpleAsset botonIzquierda;
    private SimpleAsset botonDerecha;
    private SimpleAsset botonDisparo;
    private SimpleAsset botonPausa;
    private SimpleAsset botonPlay;
    private SimpleAsset botonRegresarMenu;
    private SimpleAsset botonConfiguraciones;

    //FALTA HACERLO PERSONAJE
    private SimpleAsset donChito;

    private SimpleAsset fondoPantalla;
    private SimpleAsset fondoPausa;

    private State estado = State.PLAY;
    private State estadoBoton = State.PRESIONADO;

    private SpriteBatch batch;

    private Music efectoBoton;
    private Music efectoGanar;
    private Music efectoPerder;
    private Music musicaFondo;
    private Music musicaIntro;


    public RomanStruggle(DonChito game) {
        this.game = game;
    }

    @Override
    public void show() {
        //init();
        camera = new OrthographicCamera(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO);
        camera.position.set(DonChito.ANCHO_MUNDO / 2, DonChito.ALTO_MUNDO / 2, 0);
        camera.update();
        view = new FitViewport(DonChito.ANCHO_MUNDO,DonChito.ALTO_MUNDO,camera);

        leerEntrada();
        cargarRecursos();

        //musicaFondo.setLooping(true);
        //musicaIntro.play();

        //TODO Refactor next code into an Asset Manager
        batch = new SpriteBatch();
        fondoPantalla = new SimpleAsset(Constants.ROMAN_FONDO,new Vector2(0,0));
        botonIzquierda = new SimpleAsset(Constants.ROMAN_BOTON_IZQUIERDA,new Vector2(20,10));
        botonDerecha = new SimpleAsset(Constants.ROMAN_BOTON_DERECHA,new Vector2(170,10));
        botonDisparo = new SimpleAsset(Constants.ROMAN_BOTON_DISPARA,new Vector2(320,10));

        //FALTA HACER A DON CHITO COMO UN PERSONAJE
        donChito = new SimpleAsset(Constants.ROMAN_PERSONAJE_DONCHITO,new Vector2(550,10));
    }
    /* inicializar niveles, vidas, etc.
    private void init() {
        nivel = 0;
        indiceSecuencia = 0;
    }
    */

    private void cargarRecursos() {
        AssetManager assetManager = DonChito.getAssetManager();

        assetManager.load(Constants.ROMAN_BOTON_IZQUIERDA,Texture.class);
        assetManager.load(Constants.ROMAN_BOTON_DERECHA,Texture.class);
        assetManager.load(Constants.ROMAN_BOTON_DISPARA, Texture.class);
        assetManager.load(Constants.ROMAN_PERSONAJE_DONCHITO, Texture.class);
        assetManager.load(Constants.ROMAN_FONDO, Texture.class);

        assetManager.load(Constants.FLEVORIO_MENU_PAUSA_PNG, Texture.class);
        assetManager.load(Constants.FLEVORIO_BOTON_PAUSA_PNG, Texture.class);
        assetManager.load(Constants.FLEVORIO_BOTON_PLAY_PNG, Texture.class);
        assetManager.load(Constants.FLEVORIO_BOTON_CONFIGURACION_PNG, Texture.class);
        assetManager.load(Constants.FLEVORIO_BOTON_SALIRMENU_PNG, Texture.class);

        /*
        assetManager.load(Constants.FLEVORIO_SONIDOBOTON_WAV, Music.class);
        assetManager.load(Constants.FLEVORIO_SONIDOFAIL_WAV,Music.class);
        assetManager.load(Constants.FLEVORIO_SONIDOVICTORY_WAV, Music.class);
        assetManager.load(Constants.FLEVORIO_MUSICAINTRO_WAV, Music.class);
        assetManager.load(Constants.FLEVORIO_MUSICAFONDO_WAV,Music.class);
        */
        assetManager.finishLoading();
        /*
        efectoBoton = assetManager.get(Constants.FLEVORIO_SONIDOBOTON_WAV);
        efectoGanar = assetManager.get(Constants.FLEVORIO_SONIDOVICTORY_WAV);
        efectoPerder = assetManager.get(Constants.FLEVORIO_SONIDOFAIL_WAV);

        musicaFondo = assetManager.get(Constants.FLEVORIO_MUSICAFONDO_WAV);
        musicaIntro = assetManager.get(Constants.FLEVORIO_MUSICAINTRO_WAV);
        */
    }

    @Override
    public void render(float delta) {
        camera.update();
        batch.begin();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        fondoPantalla.render(batch);
        botonDerecha.render(batch);
        botonIzquierda.render(batch);
        botonDisparo.render(batch);

        ejecutarInputs();

        donChito.render(batch);


        if(nivel == 5){
            //correr animacion de SUCCEES!! NEXT LVL...
            nivel++;
            if(nivel == 4) {
                musicaFondo.setLooping(false);
                if (musicaFondo.isPlaying()) {
                    musicaFondo.stop();
                }
                game.setScreen(new MenuPrincipal(game));
            }
        }
        view.apply();

        /*
        if(estado == State.PAUSA){
            fondoPausa = new SimpleAsset(Constants.FLEVORIO_MENU_PAUSA_PNG,new Vector2(0,0));
            botonPlay = new SimpleAsset(Constants.FLEVORIO_BOTON_PLAY_PNG,new Vector2(1050,10));
            botonConfiguracion = new SimpleAsset(Constants.FLEVORIO_BOTON_CONFIGURACION_PNG,new Vector2(405,175));
            botonSalirMenu = new SimpleAsset(Constants.FLEVORIO_BOTON_SALIRMENU_PNG,new Vector2(405,425));

            fondoPausa.render(batch);
            botonPlay.render(batch);
            botonConfiguracion.render(batch);
            botonSalirMenu.render(batch);
        }
        else{
            botonPausa = new SimpleAsset(Constants.FLEVORIO_BOTON_PAUSA_PNG,new Vector2(1050,10));
            botonPausa.render(batch);
        }
        */
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        view.update(width,height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        DonChito.getAssetManager().clear();
    }
    private void leerEntrada() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            public boolean touchUp (int x, int y, int pointer, int button) {
                estadoBoton = State.NOPRESIONADO;
                return true; // return true to indicate the event was handled
            }
            public boolean touchDown (int x, int y, int pointexr, int button) {
                estadoBoton = State.PRESIONADO;
                if(estado == State.PAUSA){
                    if(x<828 && x>414 && y<284 && y>213){
                        //init();
                        musicaFondo.setLooping(false);
                        if(musicaFondo.isPlaying()) {
                            musicaFondo.stop();
                        }
                        game.setScreen(new MenuPrincipal(game));
                    }
                }
                return true; // return true to indicate the event was handled
            }
        });
    }
    public enum State
    {
        PAUSA,
        PLAY,
        PRESIONADO,
        NOPRESIONADO
    }
    private void ejecutarInputs(){
        if(estadoBoton == State.PRESIONADO){
            int x = Gdx.app.getInput().getX();
            int y = Gdx.app.getInput().getY();
            if(x>20 && x<125 && y>610 && y<710) {
                Gdx.app.log("Presiono ", "IZQUIERDA");
                botonPresionado = 1;
            }
            else if(x>170 && x<265 && y>610 && y<710){
                Gdx.app.log("Presiono ","DERECHA");
                botonPresionado = 2;

            }
            else if(x>325 && x<425 && y>610 && y<710){
                botonPresionado = 3;
                Gdx.app.log("Presiono ","DISPARO");
            }
            else {
                botonPresionado = 0;
            }
            switch(botonPresionado){
                case 1:
                    donChito.setPosition(new Vector2(donChito.getSprite().getX() - velocidad,donChito.getSprite().getY()));
                    break;
                case 2:
                    donChito.setPosition(new Vector2(donChito.getSprite().getX() + velocidad, donChito.getSprite().getY()));
                    break;
                case 3:
                    break;
                default:
                    break;
            }
        }
    }
}