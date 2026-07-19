import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-toast-container',
  imports: [CommonModule],
  templateUrl: './toast-container.html',
  styleUrl: './toast-container.scss'
})
export class ToastContainerComponent {
  constructor(public notificaciones: NotificationService) {}

  iconoDe(tipo: string): string {
    if (tipo === 'exito') return '✓';
    if (tipo === 'error') return '✕';
    return 'ℹ';
  }
}