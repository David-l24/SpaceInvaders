package Lógica.Entidades;

import Lógica.Interfaces.Movimiento;
import Lógica.MovimientoEntidades.MovimientoAbajo;
import Lógica.Interfaces.PowerUp;
import Presentación.Ventanas.VentanaJuego;

import java.awt.*;
import java.util.Random;

public class PowerUpVida implements PowerUp {
  public static final int INCREMENTADOR_DE_VIDA = 1;
  public static final int VELOCIDAD_POWER_UP = 3;
  private final Movimiento movimientoAbajo;
  private Posición posición;

  public PowerUpVida(Posición posiciónPotenciador) {
    posición = posiciónPotenciador;
    movimientoAbajo = new MovimientoAbajo();
  }

  @Override
  public void establecerPowerUp(NaveJugador naveJugador) {
    naveJugador.establecerVida(naveJugador.obtenerVida() + INCREMENTADOR_DE_VIDA);
  }

  public void caerPowerUp() {
    movimientoAbajo.mover(posición, VELOCIDAD_POWER_UP);
  }

  @Override
  public Posición obtenerPosiciónPowerUp() {
    return posición;
  }

  public static PowerUpVida generarPowerUpVida() {
    Posición nuevaPosición = new Posición(
      new Random().nextInt(
        VentanaJuego.obtenerAnchoVentana() - VentanaJuego.obtenerTamañoEntidad()),
      0);
    return new PowerUpVida(nuevaPosición);
  }

  public Rectangle obtenerÁrea() {
    int hitBox = 24;
    Rectangle rectPowerUp = new Rectangle(obtenerPosiciónPowerUp().obtenerPosiciónX(),
      obtenerPosiciónPowerUp().obtenerPosiciónY(),
      VentanaJuego.obtenerTamañoEntidad() - hitBox,
      VentanaJuego.obtenerTamañoEntidad() - hitBox
    );
    return rectPowerUp;
  }
}
