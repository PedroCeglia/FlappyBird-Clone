package com.pedro.ceglia.curso.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {

	// Classe usada para inserirmos/ renderiosarmos Imagens no jogo
	private SpriteBatch batch;
	// Classe que representa a Imagem propriamente dita
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private  Texture gameOver;


	// Formas para colisoes
	private Rectangle circuloPassaro;
	private Rectangle rtCanoCima;
	private Rectangle rtCanoBaixo;
	private ShapeRenderer shapeRenderer;

	// Atributos de Configuração
	private float larguraDispositivo;
	private float alturaDispositivo;
	private float posicaoHorizontalPassaro;
	private float posicaoInicialVerticalPassaro;
	private float posicaoVerticalCano;
	private float posicaoInicialHorizontalCano;
	private float variacao;
	private float gravidade;
	private float espacoEntreCanos;
	private Random random;
	private boolean passouCano;
	private float posicaoCanoTopoVertical;
	private float posicaoCanoBaixoVertical;
	private int estadoJogo;

	// Exibição de Textos
	private BitmapFont textoPontuacao;
	private BitmapFont textoReiniciar;
	private BitmapFont textoMelhorPontuacao;
	private int pontos;
	private int pontosMax;

	// ConfigurarSon
	private Sound somVoando;
	private Sound somColisao;
	private Sound somPontuacao;

	// Salva Pontuação
	private Preferences preferences;

	// Configurando Camera
	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;


	@Override
	public void create () {
		Gdx.app.log("create", "jogo iniciado");

		random = new Random();

		estadoJogo = 0;
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo/2;

		posicaoVerticalCano = 0;
		variacao = 0;
		espacoEntreCanos = 300;
		pontos = 0;

		passouCano = false;
		gravidade = 0;

		batch = new  SpriteBatch();
		passaros= new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");
		gameOver = new Texture("game_over.png");

		textoReiniciar =  new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao =  new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);


		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");

		posicaoInicialHorizontalCano = larguraDispositivo - canoTopo.getWidth();

		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Rectangle();
		rtCanoCima = new Rectangle();
		rtCanoBaixo = new Rectangle();

		posicaoHorizontalPassaro = 0;
		// Definindo som
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));

		preferences =  Gdx.app.getPreferences("flappyBird");
		pontosMax = preferences.getInteger("pontuacaoMaxima", 0);

		// Configuração da Camera
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void render () {
		Gdx.app.log("render", "jogo renderisado");
		// Limpar frames anteriores
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		verificaEstadoJogo();
		desenharTexturas();
		validarPontos();
		detectarColisoes();
	}

	private void desenharTexturas(){

		batch.setProjectionMatrix(camera.combined);
		batch.begin();

		posicaoCanoTopoVertical = alturaDispositivo -canoTopo.getHeight() + espacoEntreCanos + posicaoVerticalCano;
		posicaoCanoBaixoVertical = alturaDispositivo/2 - canoTopo.getHeight() - espacoEntreCanos + posicaoVerticalCano;

		batch.draw(fundo,0,0, larguraDispositivo, alturaDispositivo);
		batch.draw(canoTopo,posicaoInicialHorizontalCano, posicaoCanoTopoVertical);
		batch.draw(canoBaixo,posicaoInicialHorizontalCano,posicaoCanoBaixoVertical);
		batch.draw(passaros[(int) variacao],50 + posicaoHorizontalPassaro ,posicaoInicialVerticalPassaro);

		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo/2, alturaDispositivo - alturaDispositivo/4);

		if (estadoJogo == 2){
			batch.draw(gameOver, larguraDispositivo/2 - gameOver.getWidth()/2, alturaDispositivo/2);
			textoReiniciar.draw(batch,"Toque para reiniciar", larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight()/2 );
			textoMelhorPontuacao.draw(batch,"Seu record é "+ pontosMax +" pontos\n Pontuação atual "+pontos, larguraDispositivo/2 - 140, alturaDispositivo/2 - gameOver.getHeight());
		}
		batch.end();
	}

	private void verificaEstadoJogo(){

		boolean toqueTela = Gdx.input.justTouched();

		if (estadoJogo == 0){
			if (toqueTela){
				gravidade = -15;
				estadoJogo = 1;
			}
		}  else if (estadoJogo == 1){

			// Movimentação do Cano
			if (posicaoInicialHorizontalCano<= -canoBaixo.getWidth()){
				posicaoInicialHorizontalCano = larguraDispositivo;
				posicaoVerticalCano = random.nextInt(800) -400;
				passouCano = false;
			}

			// Definindo Gravidade
			if (posicaoInicialVerticalPassaro>passaros[0].getHeight() || gravidade<0)
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;

			if (Gdx.input.justTouched()){
				gravidade = -20;
				somVoando.play();
			}


			posicaoInicialHorizontalCano-=6;
			gravidade++;

		}  else if (estadoJogo == 2){
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*100;
			posicaoInicialVerticalPassaro -= Gdx.graphics.getDeltaTime()*500;

			if (pontos > pontosMax){
				pontosMax = pontos;
				preferences.putInteger("pontuacaoMax", pontosMax);
			}

			boolean toqueTela2 = Gdx.input.justTouched();
			if (Gdx.input.justTouched()){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo/2;
				posicaoInicialHorizontalCano = larguraDispositivo + canoTopo.getWidth();
				posicaoHorizontalPassaro =0;
			}
		}
	}

	private void validarPontos(){
		if (posicaoInicialHorizontalCano< 50 - canoTopo.getWidth()){
			// Se passou cano for false
			if (!passouCano){
				pontos++;
				somPontuacao.play();
				passouCano = true;
			}
		}

		variacao+= Gdx.graphics.getDeltaTime() * 5;

		// Definindo Imagem
		if (variacao>3)
			variacao = 0;

	}

	private void detectarColisoes(){

		circuloPassaro.set(50 ,posicaoInicialVerticalPassaro, passaros[0].getWidth(), passaros[0].getHeight());
		rtCanoBaixo.set(posicaoInicialHorizontalCano, posicaoCanoBaixoVertical, canoTopo.getWidth(), canoTopo.getHeight());
		rtCanoCima.set(posicaoInicialHorizontalCano, posicaoCanoTopoVertical, canoTopo.getWidth(), canoTopo.getHeight());
		if (Intersector.overlaps(circuloPassaro, rtCanoCima) || Intersector.overlaps(circuloPassaro, rtCanoBaixo)){
			if (estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	@Override
	public void dispose () {
		Gdx.app.log("dispose", "Descarte de Conteudos");
	}
}
