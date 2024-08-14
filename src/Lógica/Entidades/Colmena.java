package Lógica.Entidades;

import Presentación.Ventanas.VentanaJuego;

import java.util.Random;

public class Colmena {
  private Posición posición;

  public Colmena(Posición posición) {
    this.posición = posición;
  }

  public Misil disparar(NaveEnemiga[][] colmenaEnemigos) {
    Random random = new Random();
    NaveEnemiga naveEnemiga = null;
    int fila, columna;

    while (naveEnemiga == null) {
      fila = random.nextInt(colmenaEnemigos.length);
      columna = random.nextInt(colmenaEnemigos[0].length);
      naveEnemiga = colmenaEnemigos[fila][columna];
    }
    return naveEnemiga.disparar();
  }

  public NaveEnemiga[][] generarColmenaEnemigos(int filas, int columnas) {
    NaveEnemiga[][] colmenaEnemigos = new NaveEnemiga[filas][columnas];
    for (int i = 0; i < colmenaEnemigos.length; i++) {
      for (int j = 0; j < colmenaEnemigos[0].length; j++) {
        int puntuacion = obtenerPuntuaciónDeEnemigos(i);
        colmenaEnemigos[i][j] = new NaveEnemiga(
          new Posición(
            (j * VentanaJuego.obtenerTamañoEntidad()) + posición.obtenerPosiciónX(),
            (i * VentanaJuego.obtenerTamañoEntidad() + posición.obtenerPosiciónY())
          ), puntuacion
        );
      }
    }
    return colmenaEnemigos;
  }

  private int obtenerPuntuaciónDeEnemigos(int fila) {
    int puntuaciónAzul = 6;
    int puntuaciónIndexOutOfBounds = 4;
    int puntuaciónNullPointerException = 2;
    switch (fila) {
      case 0:
        return puntuaciónAzul;
      case 1:
        return puntuaciónIndexOutOfBounds;
      case 2:
        return puntuaciónNullPointerException;
      default:
        return 0;
    }
  }
}
