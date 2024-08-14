package Lógica.Entidades;

import java.io.Serializable;

public class NaveEnemiga extends Nave implements Serializable {
  private static final int VELOCIDAD_NAVE = 2;
  private static final int VELOCIDAD_MISIL_NAVE = 4;
  private final int puntajeNave;

  public NaveEnemiga(Posición posición, int puntajeNave) {
    super(posición, VELOCIDAD_NAVE);
    this.puntajeNave = puntajeNave;
  }

  public int obtenerPuntos() {
    return puntajeNave;
  }

  public Misil disparar() {
    return new Misil(
      new Posición(obtenerPosición().obtenerPosiciónX(), obtenerPosición().obtenerPosiciónY()), VELOCIDAD_MISIL_NAVE);
  }

  public static int obtenerVelocidadEnemigo() {
    return VELOCIDAD_NAVE;
  }
}
