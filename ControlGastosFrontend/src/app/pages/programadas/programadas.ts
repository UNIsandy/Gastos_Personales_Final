import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CommonModule, DatePipe } from '@angular/common';
import { TransaccionService } from '../../services/transaccion.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { Transaccion } from '../../models';

@Component({
  selector: 'app-programadas',
  imports: [CommonModule, ReactiveFormsModule],
  providers: [DatePipe],
  templateUrl: './programadas.html',
  styleUrl: './programadas.scss'
})
export class ProgramadasComponent implements OnInit {
  transacciones: Transaccion[] = [];
  cargando = true;
  mostrandoForm = false;
  editando: Transaccion | null = null;
  formulario!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private service: TransaccionService,
    private auth: AuthService,
    private notificaciones: NotificationService,
    private datePipe: DatePipe
  ) {}

  ngOnInit() {
    this.formulario = this.fb.group({
      descripcion: ['', [Validators.required, Validators.minLength(3)]],
      monto: [0, [Validators.required, Validators.min(0.01)]],
      tipo: ['GASTO', Validators.required],
      categoria: ['Alimentación', Validators.required],
      fecha: ['', Validators.required]
    });
    this.cargar();
  }

  get cDescripcion() { return this.formulario.get('descripcion'); }
  get cMonto() { return this.formulario.get('monto'); }
  get cCategoria() { return this.formulario.get('categoria'); }
  get cFecha() { return this.formulario.get('fecha'); }

  cargar() {
    this.cargando = true;
    this.auth.getUsuarioId().subscribe(id => {
      if (!id) return;
      this.service.listarProgramadas(id).subscribe({
        next: (data) => { this.transacciones = data; this.cargando = false; },
        error: () => { this.cargando = false; }
      });
    });
  }

  toggleForm() {
    this.mostrandoForm = !this.mostrandoForm;
    this.editando = null;
    if (!this.mostrandoForm) {
      this.formulario.reset({ descripcion: '', monto: 0, tipo: 'GASTO', categoria: 'Alimentación', fecha: '' });
    }
  }

  editar(t: Transaccion) {
    this.editando = t;
    this.mostrandoForm = true;
    this.formulario.patchValue({
      descripcion: t.descripcion,
      monto: t.monto,
      tipo: t.tipo,
      categoria: t.categoria?.nombre || 'Otros',
      fecha: t.fecha
    });
  }

  cancelarEdicion() {
    this.editando = null;
    this.mostrandoForm = false;
    this.formulario.reset({ descripcion: '', monto: 0, tipo: 'GASTO', categoria: 'Alimentación', fecha: '' });
  }

  guardar() {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid) return;

    this.auth.getUsuarioId().subscribe(usuarioId => {
      if (!usuarioId) { this.notificaciones.error('Usuario no autenticado'); return; }

      const data: any = {
        descripcion: this.formulario.value.descripcion,
        monto: this.formulario.value.monto,
        tipo: this.formulario.value.tipo,
        fecha: this.formulario.value.fecha,
        categoria: { nombre: this.formulario.value.categoria },
        usuario: { id: usuarioId }
      };

      if (this.editando) {
        this.service.actualizar(this.editando.id!, data).subscribe({
          next: () => {
            this.notificaciones.exito('Transacción programada actualizada');
            this.cancelarEdicion();
            this.cargar();
          },
          error: (err) => this.notificaciones.error(err.message || 'Error al actualizar')
        });
      } else {
        this.service.crearProgramada(data).subscribe({
          next: () => {
            this.notificaciones.exito('Transacción programada creada');
            this.toggleForm();
            this.cargar();
          },
          error: (err) => this.notificaciones.error(err.message || 'Error al crear')
        });
      }
    });
  }

  eliminar(t: Transaccion) {
    this.service.eliminar(t.id!).subscribe({
      next: () => {
        this.notificaciones.exito('Transacción programada eliminada');
        this.cargar();
      },
      error: (err) => this.notificaciones.error(err.message)
    });
  }

  diasRestantes(fecha: string): string {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const target = new Date(fecha);
    target.setHours(0, 0, 0, 0);
    const diff = Math.ceil((target.getTime() - hoy.getTime()) / (1000 * 60 * 60 * 24));
    if (diff === 0) return 'Hoy';
    if (diff === 1) return 'Mañana';
    return `Faltan ${diff} días`;
  }
}
