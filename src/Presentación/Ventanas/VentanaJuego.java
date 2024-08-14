package Presentación.Ventanas;

import Lógica.Entidades.*;
import Presentación.Servicios.*;
import Presentación.Enumeraciones.AcciónUsuario;
import Presentación.Enumeraciones.EstadoDeLaVentana;
import Presentación.Enumeraciones.Sonido;
import Presentación.Pintores.*;
import Utilidades.EscrituraDeJuego;
import Utilidades.GuardarPartida;

import java.awt.*;
import javax.swing.*;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class VentanaJuego extends JPanel implements Runnable, Serializable {
  //Constantes
  public static final int TAMAÑO_ESTÁNDAR_ENTIDAD = 16;
  public static final int ESCALA_ENTIDAD = 3;
  public static final int TAMAÑO_ENTIDAD = TAMAÑO_ESTÁNDAR_ENTIDAD * ESCALA_ENTIDAD; //48
  private static final int MÁXIMA_CANTIDAD_COLUMNAS = 16;
  private static final int MÁXIMA_CANTIDAD_FILAS = 12;
  private static final int ANCHO_VENTANA = MÁXIMA_CANTIDAD_COLUMNAS * TAMAÑO_ENTIDAD; //768
  private static final int ALTO_VENTANA = MÁXIMA_CANTIDAD_FILAS * TAMAÑO_ENTIDAD; //576

  //Renderizado
  public static final int FPS_JUEGO = 60;
  private static final int TIEMPO_ENTRE_DISPAROS_JUGADOR = 450;
  private static final int TIEMPO_ENTRE_DISPAROS_ENEMIGOS = 950;
  private static final int TIEMPO_ENTRE_POWER_UPS = 16000;
  private static final int TIEMPO_ENTRE_MOVIMIENTO_MENU = 200;
  private static final long TIEMPO_ENTRE_OVNIS = 15000;
  private Thread hiloJuego;

  //Controladores Juego
  private int opciónDeUsuario;
  private EstadoDeLaVentana estadoDeLaVentanaActual;
  private boolean enPausa;

  //Pintores
  private final PintorEntidad pintorJugador = new PintorEntidad("Jugador", 4);
  private final PintorColmena pintorColmena = new PintorColmena();
  private final PintorEntidad pintorMisilJugador = new PintorEntidad("MisilJugador", 1);
  private final PintorEntidad pintorMisilEnemigos = new PintorEntidad("MisilEnemigos", 1);
  private final PintorEntidad pintorPowerUpVida = new PintorEntidad("PowerUpVida", 1);
  private final PintorEntidad pintorOvni = new PintorEntidad("Ovni", 4);
  private final PintorEntidad pintorBarrera = new PintorEntidad("Barrera", 1);

  //Entidades
  private NaveJugador naveJugador;
  private NaveEnemiga[][] navesEnemigas;
  private Barrera[] barreras;

  private ArrayList<Misil> misilesJugador = new ArrayList<>();
  private ArrayList<Misil> misilesEnemigos = new ArrayList<>();
  private final ArrayList<PowerUpVida> powerUpVidas = new ArrayList<>();
  private ArrayList<Ovni> ovnis = new ArrayList<>();
  private Colmena colmena;

  //MovimientoColmena
  private boolean moviendoDerecha = true;
  private boolean colmenaDescendiendo = false;
  private double distanciaDescendida = 0;

  //Tiempo
  private long últimoTiempoMovimientoMenúDePausa = 0;
  private long últimoTiempoMovimientoEnMenúDeJuegoTerminado = 0;
  private long últimoTiempoDisparoJugador = 0;
  private long últimoTiempoDisparoColmena = 0;
  private long últimoTiempoEntrePowerUps = 0;
  private long últimoTiempoMovimientoMenú = 0;
  private long últimoTiempoGeneradoOvni = 0;
  private long tiempoDeInicioDeJuego;
  private long tiempoDeInicioDePausa;

  private long últimoTiempoConfirmar = 0;
  private static final long TIEMPO_ENTRE_CONFIRMACIONES = 200;

  //Imágenes de Fondo de pantalla
  private BufferedImage fondoMenúPrincipal;
  private BufferedImage fondoJuego;

  //Administradores de Eventos
  private final AdministradorSonido administradorSonido = new AdministradorSonido();
  private final AdministradorEventoTeclas administradorTeclas = new AdministradorEventoTeclas();

  public VentanaJuego() {
    estadoDeLaVentanaActual = EstadoDeLaVentana.PRINCIPAL;
    opciónDeUsuario = 0;
    enPausa = false;
    cargarImagenDeFondoDeMenúPrincipal();
    cargarImagenDeFondoDelJuego();
    generarNaveJugador();
    generarColmena();
    generarBarreras();
    configurarVentana();
    setFocusable(true);
    administradorSonido.ponerMusicaFondo();
    administradorSonido.reproducirMusicaFondo();
    addKeyListener(administradorTeclas);
    iniciarHiloJuego();
  }

  private void generarBarreras() {
    int numeroDeBarreras = 5;
    barreras = new Barrera[numeroDeBarreras];
    for (int i = 0; i < barreras.length; i++) {
      barreras[i] = new Barrera(
        new Posición(
          (i + 1) * (ANCHO_VENTANA / (numeroDeBarreras + 1)),
          (ALTO_VENTANA / 4) * 3
        )
      );
    }
  }

  public void verificarVidaJugador() {
    if (naveJugador.obtenerVida() <= 0) {
      cambiarEstadoDePantalla();
    }
  }

  public void cambiarEstadoDePantalla() {
    estadoDeLaVentanaActual = EstadoDeLaVentana.JUEGO_TERMINADO;
  }

  private void configurarVentana() {
    setPreferredSize(new Dimension(
      ANCHO_VENTANA, ALTO_VENTANA));
  }

  private void generarNaveJugador() {
    int posiciónJugadorEnX = ANCHO_VENTANA / 2;
    int posiciónJugadorEnY = (ALTO_VENTANA / 2) + (TAMAÑO_ENTIDAD * 5);
    naveJugador = new NaveJugador(
      new Posición(
        posiciónJugadorEnX,
        posiciónJugadorEnY
      ));
  }

  private void generarColmena() {
    int filaColmena = 3;
    int columnaColmena = 8;
    colmena = new Colmena(
      new Posición(
        (ANCHO_VENTANA / 2
          - TAMAÑO_ENTIDAD * (columnaColmena / 2)),
        (TAMAÑO_ENTIDAD * 2)
      )
    );
    navesEnemigas = colmena.generarColmenaEnemigos(filaColmena, columnaColmena);
  }

  private void generarNuevaColmena() {
    boolean colmenaEliminada = true;
    for (NaveEnemiga[] fila : navesEnemigas) {
      for (NaveEnemiga nave : fila) {
        if (nave != null) {
          colmenaEliminada = false;
          break;
        }
      }
    }
    if (colmenaEliminada) {
      generarColmena();
    }
  }

  private void reproducirSonido(Sonido sonido) {
    administradorSonido.ponerSonido(sonido);
    administradorSonido.reproducir();
  }

  private void cargarImagenDeFondoDeMenúPrincipal() {
    try {
      fondoMenúPrincipal = ImageIO.read(
        Objects.requireNonNull(getClass().getResource("/Presentación/Recursos/FondoDeJuego/fondodemenu.png")));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void cargarImagenDeFondoDelJuego() {
    try {
      fondoJuego = ImageIO.read(
        Objects.requireNonNull(getClass().getResource("/Presentación/Recursos/FondoDeJuego/fondodejuego.png")));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void iniciarHiloJuego() {
    hiloJuego = new Thread(this);
    hiloJuego.start();
  }

  public void actualizar() {
    AcciónUsuario acción = administradorTeclas.obtenerAcción();
    long tiempoActual = System.currentTimeMillis();

    if (estadoDeLaVentanaActual == EstadoDeLaVentana.PRINCIPAL) {
      if (acción == AcciónUsuario.ARRIBA) {
        if (tiempoActual - últimoTiempoMovimientoMenú >= TIEMPO_ENTRE_MOVIMIENTO_MENU) {
          opciónDeUsuario = (opciónDeUsuario - 1 + 3) % 3;
          reproducirSonido(Sonido.OPCIÓN);
          últimoTiempoMovimientoMenú = tiempoActual;
        }
      } else if (acción == AcciónUsuario.ABAJO) {
        if (tiempoActual - últimoTiempoMovimientoMenú >= TIEMPO_ENTRE_MOVIMIENTO_MENU) {
          opciónDeUsuario = (opciónDeUsuario + 1) % 3;
          reproducirSonido(Sonido.OPCIÓN);
          últimoTiempoMovimientoMenú = tiempoActual;
        }
      } else if (acción == AcciónUsuario.CONFIRMAR && tiempoActual - últimoTiempoConfirmar >= TIEMPO_ENTRE_CONFIRMACIONES) {
        últimoTiempoConfirmar = tiempoActual;
        if (opciónDeUsuario == 0) {
          estadoDeLaVentanaActual = EstadoDeLaVentana.JUEGO;
          tiempoDeInicioDeJuego = System.currentTimeMillis();
          administradorTeclas.cambiarEstadoActualDeLaVentana(estadoDeLaVentanaActual);
        }
      } else if (acción == AcciónUsuario.CONFIRMAR && opciónDeUsuario == 1) {
        cargarPartida();
      } else if (acción == AcciónUsuario.CONFIRMAR && opciónDeUsuario == 2) {
        System.exit(0);
      }
    }

    if (estadoDeLaVentanaActual == EstadoDeLaVentana.JUEGO) {
      if (enPausa) {
        if (acción == AcciónUsuario.ARRIBA) {
          if (tiempoActual - últimoTiempoMovimientoMenúDePausa >= TIEMPO_ENTRE_MOVIMIENTO_MENU) {
            opciónDeUsuario = (opciónDeUsuario - 1 + 3) % 3;
            reproducirSonido(Sonido.OPCIÓN);
            últimoTiempoMovimientoMenúDePausa = tiempoActual;
          }
        } else if (acción == AcciónUsuario.ABAJO) {
          if (tiempoActual - últimoTiempoMovimientoMenúDePausa >= TIEMPO_ENTRE_MOVIMIENTO_MENU) {
            opciónDeUsuario = (opciónDeUsuario + 1) % 3;
            reproducirSonido(Sonido.OPCIÓN);
            últimoTiempoMovimientoMenúDePausa = tiempoActual;
          }
        } else if (acción == AcciónUsuario.CONFIRMAR && tiempoActual - últimoTiempoConfirmar >= TIEMPO_ENTRE_CONFIRMACIONES) {
          últimoTiempoConfirmar = tiempoActual;
          if (opciónDeUsuario == 0) {
            reanudarJuego();
          } else if (opciónDeUsuario == 1) {
            guardarPartida();
            //JOptionPane.showMessageDialog(null, "Partida guardada con éxito");

          } else if (opciónDeUsuario == 2) {
            reiniciarJuego();
            administradorTeclas.cambiarEstadoActualDeLaVentana(estadoDeLaVentanaActual);
          }
        }
      } else {
        actualizarJuego();
        if (acción == AcciónUsuario.PAUSA) {
          pausarJuego();
        }
      }
    }

    if (estadoDeLaVentanaActual == EstadoDeLaVentana.JUEGO_TERMINADO) {
      if (acción == AcciónUsuario.ARRIBA) {
        if (tiempoActual - últimoTiempoMovimientoEnMenúDeJuegoTerminado >= TIEMPO_ENTRE_MOVIMIENTO_MENU) {
          opciónDeUsuario = (opciónDeUsuario - 1 + 2) % 2;
          reproducirSonido(Sonido.OPCIÓN);
          últimoTiempoMovimientoEnMenúDeJuegoTerminado = tiempoActual;
        }
      } else if (acción == AcciónUsuario.ABAJO) {
        if (tiempoActual - últimoTiempoMovimientoEnMenúDeJuegoTerminado >= TIEMPO_ENTRE_MOVIMIENTO_MENU) {
          opciónDeUsuario = (opciónDeUsuario + 1) % 2;
          reproducirSonido(Sonido.OPCIÓN);
          últimoTiempoMovimientoEnMenúDeJuegoTerminado = tiempoActual;
        }
      } else if (acción == AcciónUsuario.CONFIRMAR && tiempoActual - últimoTiempoConfirmar >= TIEMPO_ENTRE_CONFIRMACIONES) {
        últimoTiempoConfirmar = tiempoActual;
        if (opciónDeUsuario == 0) {
          reiniciarJuego();
          reanudarMusica();
          estadoDeLaVentanaActual = EstadoDeLaVentana.JUEGO;
          administradorTeclas.cambiarEstadoActualDeLaVentana(estadoDeLaVentanaActual);
        } else if (opciónDeUsuario == 1) {
          estadoDeLaVentanaActual = EstadoDeLaVentana.PRINCIPAL;
          reiniciarJuego();
          administradorTeclas.cambiarEstadoActualDeLaVentana(estadoDeLaVentanaActual);
        }
      }
    }
    administradorTeclas.limpiarAcción();
  }

  public void actualizarJuego() {
    AcciónUsuario acciónJugador = administradorTeclas.obtenerAcción();
    long tiempoActual = System.currentTimeMillis();

    if (acciónJugador == AcciónUsuario.IZQUIERDA) {
      naveJugador.moverIzquierda();
    } else if (acciónJugador == AcciónUsuario.DERECHA) {
      naveJugador.moverDerecha();
    } else if (acciónJugador == AcciónUsuario.DISPARAR) {
      if (tiempoActual - últimoTiempoDisparoJugador >= TIEMPO_ENTRE_DISPAROS_JUGADOR) {
        misilesJugador.add(naveJugador.disparar());
        reproducirSonido(Sonido.SONIDO_DE_BALA);
        últimoTiempoDisparoJugador = tiempoActual;
      }
    }
    misilesJugador.forEach(Misil::dispararArriba);
    misilesJugador.removeIf(misil -> misil.obtenerPosiciónMisil().obtenerPosiciónY() < 0);

    if (tiempoActual - últimoTiempoDisparoColmena >= TIEMPO_ENTRE_DISPAROS_ENEMIGOS) {
      misilesEnemigos.add(colmena.disparar(navesEnemigas));
      últimoTiempoDisparoColmena = tiempoActual;
    }
    misilesEnemigos.forEach(Misil::dispararAbajo);
    misilesEnemigos.removeIf(misil -> misil.obtenerPosiciónMisil().obtenerPosiciónY() > getHeight());

    if (tiempoActual - últimoTiempoEntrePowerUps >= TIEMPO_ENTRE_POWER_UPS) {
      powerUpVidas.add(PowerUpVida.generarPowerUpVida());
      últimoTiempoEntrePowerUps = tiempoActual;
    }
    powerUpVidas.forEach(PowerUpVida::caerPowerUp);
    powerUpVidas.removeIf(powerUp -> powerUp.obtenerPosiciónPowerUp().obtenerPosiciónY() > getHeight());

    if (tiempoActual - últimoTiempoGeneradoOvni >= TIEMPO_ENTRE_OVNIS) {
      ovnis.add(Ovni.generarOvni());
      últimoTiempoGeneradoOvni = tiempoActual;
    }
    ovnis.forEach(Ovni::moverOvni);
    ovnis.removeIf(ovni -> ovni.obtenerPosiciónOvni().obtenerPosiciónY() > getHeight());

    AdministradorColisiones.colisionaMisilesEnemigosConMisilesJugador(misilesEnemigos, misilesJugador);
    AdministradorColisiones.evitarColisionarConBordesDeLaPantallaJugador(naveJugador);
    AdministradorColisiones.colisionaEnemigoConMisilDeJugador(navesEnemigas, misilesJugador, naveJugador);
    AdministradorColisiones.colisionaJugadorConMisilDeEnemigos(misilesEnemigos, naveJugador);
    AdministradorColisiones.colisionaOvniConMisilDeNaveJugador(ovnis, misilesJugador, naveJugador);
    AdministradorColisiones.colisionaBarreraConMisilJugador(misilesEnemigos, barreras);
    AdministradorColisiones.colisionaBarreraConMisilJugador(misilesJugador, barreras);
    AdministradorColisiones.colisionaPowerUpConNaveJugador(powerUpVidas, naveJugador);
    generarNuevaColmena();
    actualizarMovimientoColmena(navesEnemigas);
    pintorJugador.actualizarImagenEntidad(1, 4);
    verificarVidaJugador();
    administradorTeclas.limpiarAcción();
  }

  private void reiniciarJuego() {
    generarNaveJugador();
    generarColmena();
    generarBarreras();

    moviendoDerecha = true;

    misilesJugador.clear();
    misilesEnemigos.clear();
    powerUpVidas.clear();
    ovnis.clear();

    estadoDeLaVentanaActual = EstadoDeLaVentana.PRINCIPAL;
    enPausa = false;
    opciónDeUsuario = 0;
    tiempoDeInicioDeJuego = System.currentTimeMillis();
  }

  public void actualizarMovimientoColmena(NaveEnemiga[][] colmenaEnemigos) {
    boolean bordeAlcanzado = false;
    for (int i = 0; i < colmenaEnemigos.length; i++) {
      for (int j = 0; j < colmenaEnemigos[0].length; j++) {
        if (colmenaEnemigos[i][j] != null) {
          if (colmenaDescendiendo) {
            colmenaEnemigos[i][j].moverAbajo();
          } else {
            if (moviendoDerecha) {
              colmenaEnemigos[i][j].moverDerecha();
            } else {
              colmenaEnemigos[i][j].moverIzquierda();
            }
          }
          if (
            AdministradorColisiones.colisionaConBordesDeLaPantalla(
              colmenaEnemigos[i][j].obtenerPosición().obtenerPosiciónX())
          ) {
            bordeAlcanzado = true;
          }
        }
      }
    }
    if (colmenaDescendiendo) {
      distanciaDescendida += NaveEnemiga.obtenerVelocidadEnemigo();
      if (distanciaDescendida >= TAMAÑO_ENTIDAD) {
        colmenaDescendiendo = false;
        distanciaDescendida = 0;
      }
    } else if (bordeAlcanzado) {
      moviendoDerecha = !moviendoDerecha;
      colmenaDescendiendo = true;
      distanciaDescendida = 0;
    }
  }

  protected void paintComponent(Graphics graphics) {
    Graphics2D graphics2D = (Graphics2D) graphics;
    super.paintComponent(graphics);
    switch (estadoDeLaVentanaActual) {
      case PRINCIPAL:
        reanudarMusica();
        dibujarMenúPrincipal(graphics2D);
        break;
      case JUEGO:
        dibujarVentanaJuego(graphics2D);
        if (enPausa) {
          dibujarMenúPausa(graphics2D);
        }
        AdministradorColisiones.colisionarEnemigosConJugador(navesEnemigas, naveJugador);
        break;
      case JUEGO_TERMINADO:
        pausarMúsica();
        dibujarVentanaJuegoTerminado(graphics2D);
        break;
      case PAUSA:
        dibujarMenúPausa(graphics2D);
        break;
    }
    graphics2D.dispose();
  }

  public void pausarJuego() {
    enPausa = true;
    pausarMúsica();
    tiempoDeInicioDePausa = System.currentTimeMillis();
  }

  private void pausarMúsica() {
    administradorSonido.detenerMusicaFondo();
  }

  public void reanudarJuego() {
    enPausa = false;
    reanudarMusica();
    tiempoDeInicioDeJuego += (System.currentTimeMillis() - tiempoDeInicioDePausa);
  }

  private void reanudarMusica() {
    administradorSonido.reanudarMusicaFondo();
  }

  private void dibujarVentanaJuegoTerminado(Graphics2D graphics2D) {
    graphics2D.drawImage(fondoJuego, 0, 0, ANCHO_VENTANA, ALTO_VENTANA, null);
    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 55F));

    String text = "JUEGO TERMINADO";
    int x = obtenerXParaTextoCentrado(text, graphics2D);
    int y = TAMAÑO_ENTIDAD * 3;

    graphics2D.setColor(Color.YELLOW);
    graphics2D.drawString(text, x + 3, y + 3);
    graphics2D.setColor(Color.green);
    graphics2D.drawString(text, x, y);

    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 42F));

    text = "PUNTAJE:  ";
    x = obtenerXParaTextoCentrado(text, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(text + naveJugador.obtenerPuntos(), x + 42 - TAMAÑO_ENTIDAD, y);

    text = "VOLVER A JUGAR";
    x = obtenerXParaTextoCentrado(text, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(text, x, y);
    if (opciónDeUsuario == 0) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }

    text = "VOLVER AL MENÚ PRINCIPAL";
    x = obtenerXParaTextoCentrado(text, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(text, x, y);
    if (opciónDeUsuario == 1) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }
  }

  public void dibujarMenúPausa(Graphics2D graphics2D) {
    graphics2D.drawImage(fondoJuego, 0, 0, ANCHO_VENTANA, ALTO_VENTANA, null);
    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 55F));

    String text = "MENÚ DE PAUSA";
    int x = obtenerXParaTextoCentrado(text, graphics2D);
    int y = TAMAÑO_ENTIDAD * 3;

    graphics2D.setColor(Color.blue);
    graphics2D.drawString(text, x + 3, y + 3);
    graphics2D.setColor(Color.cyan);
    graphics2D.drawString(text, x, y);

    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 42F));

    text = "VOLVER AL JUEGO";
    x = obtenerXParaTextoCentrado(text, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(text, x, y);
    if (opciónDeUsuario == 0) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }

    text = "GUARDAR PARTIDA";
    x = obtenerXParaTextoCentrado(text, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(text, x, y);
    if (opciónDeUsuario == 1) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }

    text = "VOLVER AL MENÚ PRINCIPAL";
    x = obtenerXParaTextoCentrado(text, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(text, x, y);
    if (opciónDeUsuario == 2) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }
  }

  public void dibujarMenúPrincipal(Graphics2D graphics2D) {
    graphics2D.drawImage(fondoMenúPrincipal, 0, 0, ANCHO_VENTANA, ALTO_VENTANA, null);
    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 96F));

    String texto = "Space Invaders";
    int x = obtenerXParaTextoCentrado(texto, graphics2D);
    int y = TAMAÑO_ENTIDAD * 3;

    graphics2D.setColor(Color.yellow);
    graphics2D.drawString(texto, x + 3, y + 3);
    graphics2D.setColor(Color.orange);
    graphics2D.drawString(texto, x, y);

    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 42F));

    texto = "NUEVA PARTIDA";
    x = obtenerXParaTextoCentrado(texto, graphics2D);
    y += TAMAÑO_ENTIDAD * 3;
    graphics2D.drawString(texto, x, y);
    if (opciónDeUsuario == 0) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }

    texto = "CARGAR PARTIDA";
    x = obtenerXParaTextoCentrado(texto, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(texto, x, y);
    if (opciónDeUsuario == 1) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }

    texto = "SALIR";
    x = obtenerXParaTextoCentrado(texto, graphics2D);
    y += TAMAÑO_ENTIDAD * 2;
    graphics2D.drawString(texto, x, y);
    if (opciónDeUsuario == 2) {
      graphics2D.drawString(">", x - TAMAÑO_ENTIDAD, y);
    }
  }

  public void dibujarVentanaJuego(Graphics2D graphics2D) {
    graphics2D.drawImage(fondoJuego, 0, 0, getWidth(), getHeight(), this);
    graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 16F));
    graphics2D.setColor(Color.pink);
    graphics2D.drawString("Puntos: " + naveJugador.obtenerPuntos(), 10, 65);
    graphics2D.drawString("Vidas: " + naveJugador.obtenerVida(), 10, 90);
    long tiempoTranscurrido = System.currentTimeMillis() - tiempoDeInicioDeJuego;
    long segundosTranscurridos = tiempoTranscurrido / 1000;
    long minutosTranscurridos = segundosTranscurridos / 60;
    segundosTranscurridos = segundosTranscurridos % 60;
    String tiempoCadena = String.format("Tiempo: %02d:%02d", minutosTranscurridos, segundosTranscurridos);
    graphics2D.drawString(tiempoCadena, 10, 115);
    pintorColmena.dibujar(graphics2D, navesEnemigas);
    pintorJugador.dibujar(graphics2D,
      naveJugador.obtenerPosición().obtenerPosiciónX(), naveJugador.obtenerPosición().obtenerPosiciónY());
    for (Misil misil : misilesJugador) {
      pintorMisilJugador.dibujar(graphics2D, misil.obtenerPosiciónMisil().obtenerPosiciónX(), misil.obtenerPosiciónMisil().obtenerPosiciónY());
      pintorMisilJugador.actualizarImagenEntidad(1, 1);
    }
    for (Misil misil : misilesEnemigos) {
      pintorMisilEnemigos.dibujar(graphics2D, misil.obtenerPosiciónMisil().obtenerPosiciónX(), misil.obtenerPosiciónMisil().obtenerPosiciónY());
      pintorMisilEnemigos.actualizarImagenEntidad(1, 1);
    }
    for (PowerUpVida powerUpVida : powerUpVidas) {
      pintorPowerUpVida.dibujar(graphics2D, powerUpVida.obtenerPosiciónPowerUp().obtenerPosiciónX(), powerUpVida.obtenerPosiciónPowerUp().obtenerPosiciónY());
      pintorPowerUpVida.actualizarImagenEntidad(1, 1);
    }
    for (Ovni ovni : ovnis) {
      pintorOvni.dibujar(graphics2D, ovni.obtenerPosiciónOvni().obtenerPosiciónX(), ovni.obtenerPosiciónOvni().obtenerPosiciónY());
      pintorOvni.actualizarImagenEntidad(1, 4);
    }
    for (Barrera barrera : barreras) {
      if (barrera != null) {
        pintorBarrera.dibujar(graphics2D, barrera.obtenerPosición().obtenerPosiciónX(), barrera.obtenerPosición().obtenerPosiciónY());
        graphics2D.drawString("" + barrera.obtenerVida(),
          barrera.obtenerPosición().obtenerPosiciónX() + TAMAÑO_ENTIDAD / 2 - 5, barrera.obtenerPosición().obtenerPosiciónY() + TAMAÑO_ENTIDAD / 2);
      }
    }
  }

  private int obtenerXParaTextoCentrado(String texto, Graphics2D graphics2D) {
    int tamañoDeTexto;
    tamañoDeTexto = (int) graphics2D.getFontMetrics().getStringBounds(texto, graphics2D).getWidth();
    return (ANCHO_VENTANA / 2) - (tamañoDeTexto / 2);
  }

  @Override
  public void run() {
    double intervaloDibujo = (double) 1000000000 / FPS_JUEGO;
    double delta = 0;
    long últimoTiempo = System.nanoTime();
    long tiempoActual;

    while (hiloJuego != null) {
      tiempoActual = System.nanoTime();
      delta += (tiempoActual - últimoTiempo) / intervaloDibujo;
      últimoTiempo = tiempoActual;
      if (delta >= 1) {
        actualizar();
        repaint();
        delta -= 1;
      }
    }
  }

  public static int obtenerAnchoVentana() {
    return ANCHO_VENTANA;
  }

  public static int obtenerTamañoEntidad() {
    return TAMAÑO_ENTIDAD;
  }

  private void guardarPartida() {
    EscrituraDeJuego escrituraDeJuego = new EscrituraDeJuego();
    escrituraDeJuego.setNaveJugador(naveJugador);
    escrituraDeJuego.setNavesEnemigas(navesEnemigas);
    escrituraDeJuego.setBarreras(barreras);
    escrituraDeJuego.setMisilesJugador(misilesJugador);
    escrituraDeJuego.setMisilesEnemigos(misilesEnemigos);
    escrituraDeJuego.setOvnis(ovnis);
    escrituraDeJuego.setMoviendoDerecha(moviendoDerecha);
    escrituraDeJuego.setColmenaDescendiendo(colmenaDescendiendo);
    escrituraDeJuego.setDistanciaDescendida(distanciaDescendida);
    escrituraDeJuego.setTiempoDeInicioDeJuego(tiempoDeInicioDeJuego);

    try {
      GuardarPartida.guardarEstado(escrituraDeJuego);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void cargarPartida() {
    try {
      EscrituraDeJuego escrituraDeJuego = GuardarPartida.cargarEstado();
      naveJugador = escrituraDeJuego.obtenerNaveJugador();
      navesEnemigas = escrituraDeJuego.obtenerNavesEnemigas();
      barreras = escrituraDeJuego.obtenerBarreras();
      misilesJugador = escrituraDeJuego.obtenerMisilesJugador();
      misilesEnemigos = escrituraDeJuego.obtenerMisilesEnemigos();
      ovnis = escrituraDeJuego.obtenerOvnis();
      moviendoDerecha = escrituraDeJuego.estaMoviendoDerecha();
      colmenaDescendiendo = escrituraDeJuego.estaColmenaDescendiendo();
      distanciaDescendida = escrituraDeJuego.obtenerDistanciaDescendida();
      tiempoDeInicioDeJuego = escrituraDeJuego.obtenerTiempoDeInicioDeJuego();

      estadoDeLaVentanaActual = EstadoDeLaVentana.JUEGO;
      administradorTeclas.cambiarEstadoActualDeLaVentana(estadoDeLaVentanaActual);
    } catch (FileNotFoundException e) {
      estadoDeLaVentanaActual = EstadoDeLaVentana.PRINCIPAL;
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
}
