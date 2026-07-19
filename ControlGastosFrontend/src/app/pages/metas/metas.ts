import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MetasService } from '../../services/metas.service';
import { MetaAhorro } from '../../models';
import { NotificationService } from '../../services/notification.service';

function fechaNoPasada(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const ingresada = new Date(control.value);
  const hoy = new Date();
  hoy.setHours(0, 0, 0, 0);
  return ingresada < hoy ? { fechaPasada: true } : null;
}

@Component({
  selector: 'app-metas',
  imports: [CommonModule, ReactiveFormsModule, FormsModule],
  templateUrl: './metas.html',
  styleUrl: './metas.scss'
})
export class MetasComponent implements OnInit {
  metas: MetaAhorro[] = [];
  error = '';
  cargando = true;
  mostrandoForm = false;
  editandoMeta: MetaAhorro | null = null;
  mensajeAporte: { [key: number]: number } = {};
  filtro: 'activas' | 'completadas' = 'activas';
  formulario!: FormGroup;
  formularioEditar!: FormGroup;

  get metasFiltradas(): MetaAhorro[] {
    if (this.filtro === 'completadas') return this.metas.filter(m => !m.activa);
    return this.metas.filter(m => m.activa);
  }

  get cNombre() { return this.formulario.get('nombre'); }
  get cMonto() { return this.formulario.get('montoObjetivo'); }
  get cFechaLimite() { return this.formulario.get('fechaLimite'); }

  get ceNombre() { return this.formularioEditar.get('nombre'); }
  get ceMonto() { return this.formularioEditar.get('montoObjetivo'); }
  get ceFechaLimite() { return this.formularioEditar.get('fechaLimite'); }

  constructor(
    private service: MetasService,
    private notificaciones: NotificationService,
    private fb: FormBuilder
  ) {
    this.formulario = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      montoObjetivo: [0, [Validators.required, Validators.min(0.01)]],
      fechaLimite: ['', [Validators.required, fechaNoPasada]]
    });

    this.formularioEditar = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      montoObjetivo: [0, [Validators.required, Validators.min(0.01)]],
      fechaLimite: ['', [Validators.required, fechaNoPasada]]
    });
  }

  ngOnInit() {
    this.cargar();
  }

  cargar() {
    this.cargando = true;
    this.service.listar().subscribe({
      next: (data) => { this.metas = data; this.cargando = false; },
      error: (err) => { this.error = err.message; this.cargando = false; }
    });
  }

  get nivelGlobal(): string {
    const completadas = this.metas.filter(m => !m.activa).length;
    if (completadas >= 5) return 'ORO';
    if (completadas >= 3) return 'PLATA';
    return 'BRONCE';
  }

  get progresoGlobal(): number {
    if (this.metas.length === 0) return 0;
    const totalObjetivo = this.metas.reduce((s, m) => s + m.montoObjetivo, 0);
    const totalActual = this.metas.reduce((s, m) => s + m.montoActual, 0);
    return Math.min(100, Math.round((totalActual / totalObjetivo) * 100));
  }

  progreso(meta: MetaAhorro): number {
    return Math.min(100, Math.round((meta.montoActual / meta.montoObjetivo) * 100));
  }

  nivel(meta: MetaAhorro): string {
    const pct = this.progreso(meta);
    if (pct >= 100) return 'ORO';
    if (pct >= 50) return 'PLATA';
    return 'BRONCE';
  }

  toggleForm() {
    this.mostrandoForm = !this.mostrandoForm;
    this.formulario.reset({ nombre: '', montoObjetivo: 0, fechaLimite: '' });
    this.editandoMeta = null;
  }

  crearMeta() {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid) return;
    const v = this.formulario.value;
    this.service.crear({
      nombre: v.nombre,
      montoObjetivo: v.montoObjetivo,
      fechaLimite: v.fechaLimite,
      activa: true
    }).subscribe({
      next: () => {
        this.cargar();
        this.toggleForm();
        this.notificaciones.exito('Meta creada exitosamente');
      },
      error: (err) => {
        this.error = err.message;
        this.notificaciones.error(err.message || 'Error al crear la meta');
      }
    });
  }

  editar(meta: MetaAhorro) {
    this.editandoMeta = meta;
    this.formularioEditar.patchValue({
      nombre: meta.nombre,
      montoObjetivo: meta.montoObjetivo,
      fechaLimite: meta.fechaLimite
    });
    this.mostrandoForm = false;
  }

  cancelarEdicion() {
    this.editandoMeta = null;
    this.formularioEditar.reset({ nombre: '', montoObjetivo: 0, fechaLimite: '' });
  }

  guardarEdicion() {
    this.formularioEditar.markAllAsTouched();
    if (this.formularioEditar.invalid || !this.editandoMeta) return;
    const v = this.formularioEditar.value;
    this.service.actualizar(this.editandoMeta.id!, {
      nombre: v.nombre,
      montoObjetivo: v.montoObjetivo,
      fechaLimite: v.fechaLimite,
      activa: this.editandoMeta.activa
    }).subscribe({
      next: () => {
        this.cargar();
        this.cancelarEdicion();
        this.notificaciones.exito('Meta actualizada exitosamente');
      },
      error: (err) => {
        this.error = err.message;
        this.notificaciones.error(err.message || 'Error al actualizar la meta');
      }
    });
  }

  aportar(meta: MetaAhorro) {
    const monto = this.mensajeAporte[meta.id!];
    if (monto == null || monto === 0) return;
    this.service.aportar(meta.id!, monto).subscribe({
      next: () => {
        this.cargar();
        this.mensajeAporte[meta.id!] = 0;
        this.notificaciones.exito('Aporte registrado exitosamente');
      },
      error: (err) => {
        this.error = err.message;
        this.notificaciones.error(err.message || 'Error al registrar el aporte');
      }
    });
  }

  async eliminar(meta: MetaAhorro) {
    const confirmado = await this.notificaciones.confirmar(
      `¿Está seguro de eliminar la meta "${meta.nombre}"?`,
      { titulo: 'Eliminar meta', textoConfirmar: 'Eliminar', peligroso: true }
    );
    if (!confirmado) return;

    this.service.eliminar(meta.id!).subscribe({
      next: () => {
        this.cargar();
        this.notificaciones.exito('Meta eliminada exitosamente');
      },
      error: (err) => {
        this.error = err.message;
        this.notificaciones.error(err.message || 'Error al eliminar la meta');
      }
    });
  }
}