import { Injectable, signal } from '@angular/core';

export type TipoToast = 'exito' | 'error' | 'info';

export interface Toast {
  id: number;
  tipo: TipoToast;
  mensaje: string;
}

export interface ConfirmRequest {
  mensaje: string;
  titulo: string;
  textoConfirmar: string;
  textoCancelar: string;
  peligroso: boolean;
}

/**
 * Servicio central de ventanas emergentes del sistema:
 * - Toasts de feedback (éxito / error / info) para crear, editar, eliminar, etc.
 * - Modal de confirmación (reemplaza al confirm() nativo del navegador) para
 *   acciones destructivas como eliminar una transacción.
 *
 * No depende de ninguna librería externa: se integra con el diseño existente
 * (variables.scss / mixins.scss) a través de los componentes ToastContainer y
 * ConfirmDialog montados una sola vez en app.html.
 */
@Injectable({ providedIn: 'root' })
export class NotificationService {
  private idCounter = 0;

  readonly toasts = signal<Toast[]>([]);
  readonly confirmRequest = signal<ConfirmRequest | null>(null);
  private resolveConfirm: ((valor: boolean) => void) | null = null;

  exito(mensaje: string, duracionMs = 3500) {
    this.mostrarToast('exito', mensaje, duracionMs);
  }

  error(mensaje: string, duracionMs = 4500) {
    this.mostrarToast('error', mensaje, duracionMs);
  }

  info(mensaje: string, duracionMs = 3500) {
    this.mostrarToast('info', mensaje, duracionMs);
  }

  private mostrarToast(tipo: TipoToast, mensaje: string, _duracionMs: number) {
    const id = ++this.idCounter;
    this.toasts.update(lista => [...lista, { id, tipo, mensaje }]);
  }

  cerrarToast(id: number) {
    this.toasts.update(lista => lista.filter(t => t.id !== id));
  }

  /**
   * Muestra un modal de confirmación y devuelve una promesa que resuelve en
   * true (confirmó) o false (canceló/cerró).
   */
  confirmar(
    mensaje: string,
    opciones?: { titulo?: string; textoConfirmar?: string; textoCancelar?: string; peligroso?: boolean }
  ): Promise<boolean> {
    this.confirmRequest.set({
      mensaje,
      titulo: opciones?.titulo ?? 'Confirmar acción',
      textoConfirmar: opciones?.textoConfirmar ?? 'Confirmar',
      textoCancelar: opciones?.textoCancelar ?? 'Cancelar',
      peligroso: opciones?.peligroso ?? false
    });

    return new Promise<boolean>((resolve) => {
      this.resolveConfirm = resolve;
    });
  }

  resolverConfirmacion(resultado: boolean) {
    this.confirmRequest.set(null);
    if (this.resolveConfirm) {
      this.resolveConfirm(resultado);
      this.resolveConfirm = null;
    }
  }
}