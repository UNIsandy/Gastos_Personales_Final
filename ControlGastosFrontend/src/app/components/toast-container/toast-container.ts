import { Component, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService, Toast } from '../../services/notification.service';

@Component({
  selector: 'app-toast-container',
  imports: [CommonModule],
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.scss'
})
export class ToastContainerComponent {
  constructor(public notificaciones: NotificationService) {}

  toastActual = computed(() => {
    const lista = this.notificaciones.toasts();
    return lista.length > 0 ? lista[lista.length - 1] : null;
  });

  iconoDe(tipo: string): string {
    if (tipo === 'exito') return '✓';
    if (tipo === 'error') return '✕';
    return 'ℹ';
  }

  tituloDe(tipo: string): string {
    if (tipo === 'exito') return 'Operación exitosa';
    if (tipo === 'error') return 'Error';
    return 'Información';
  }
}
