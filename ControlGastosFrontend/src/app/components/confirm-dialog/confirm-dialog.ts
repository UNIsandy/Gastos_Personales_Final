import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationService } from '../../services/notification.service';

@Component({
  selector: 'app-confirm-dialog',
  imports: [CommonModule],
  templateUrl: './confirm-dialog.html',
  styleUrl: './confirm-dialog.scss'
})
export class ConfirmDialogComponent {
  constructor(public notificaciones: NotificationService) {}

  confirmar() {
    this.notificaciones.resolverConfirmacion(true);
  }

  cancelar() {
    this.notificaciones.resolverConfirmacion(false);
  }
}