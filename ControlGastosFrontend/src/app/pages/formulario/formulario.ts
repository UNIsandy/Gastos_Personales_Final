import { Component, OnInit } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { RouterLink, ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { TransaccionService } from '../../services/transaccion.service';
import { AuthService } from '../../services/auth.service';
import { NotificationService } from '../../services/notification.service';
import { switchMap } from 'rxjs/operators';

function fechaNoFutura(control: AbstractControl): ValidationErrors | null {
  if (!control.value) return null;
  const seleccionada = new Date(control.value);
  const hoy = new Date();
  hoy.setHours(23, 59, 59, 999);
  return seleccionada > hoy ? { fechaFutura: true } : null;
}

@Component({
  selector: 'app-formulario',
  imports: [ReactiveFormsModule, RouterLink, CommonModule],
  templateUrl: './formulario.html',
  styleUrl: './formulario.scss'
})
export class FormularioComponent implements OnInit {
  esEdicion = false;
  idEdicion: number | null = null;
  selectedFile: File | null = null;
  previewUrl: string | null = null;
  error = '';
  formulario!: FormGroup;
  riesgo: { titulo: string; mensaje: string; recomendaciones: string[]; exito: boolean } | null = null;
  guardando = false;

  constructor(
    private fb: FormBuilder,
    private service: TransaccionService,
    private auth: AuthService,
    private route: ActivatedRoute,
    private router: Router,
    private notificaciones: NotificationService
  ) {}

  ngOnInit() {
    this.formulario = this.fb.group({
      descripcion: ['', [Validators.required, Validators.minLength(3)]],
      monto: [0, [Validators.required, Validators.min(0.01)]],
      tipo: ['GASTO', Validators.required],
      categoria: ['Alimentación', Validators.required],
      fecha: [new Date().toISOString().split('T')[0], [Validators.required, fechaNoFutura]]
    });

    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.esEdicion = true;
      this.idEdicion = +idParam;
      this.service.buscarPorId(this.idEdicion).subscribe({
        next: (t) => this.formulario.patchValue({
          descripcion: t.descripcion,
          monto: t.monto,
          tipo: t.tipo,
          categoria: t.categoria?.nombre || '',
          fecha: t.fecha
        }),
        error: (err) => this.error = err.message
      });
    }
  }

  get campoDescripcion() { return this.formulario.get('descripcion'); }
  get campoMonto() { return this.formulario.get('monto'); }
  get campoCategoria() { return this.formulario.get('categoria'); }
  get campoFecha() { return this.formulario.get('fecha'); }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files[0]) {
      this.selectedFile = input.files[0];
      const reader = new FileReader();
      reader.onload = () => this.previewUrl = reader.result as string;
      reader.readAsDataURL(input.files[0]);
    }
  }

  removerArchivo() {
    this.selectedFile = null;
    this.previewUrl = null;
  }

  guardar() {
    this.formulario.markAllAsTouched();
    if (this.formulario.invalid) return;

    if (this.esEdicion) {
      this.ejecutarGuardar();
      return;
    }

    this.auth.getUsuarioId().subscribe({
      next: (usuarioId) => {
        if (!usuarioId) { this.error = 'Usuario no autenticado'; return; }
        const body = {
          usuarioId,
          tipo: this.formulario.value.tipo,
          monto: this.formulario.value.monto,
          categoria: this.formulario.value.categoria
        };

        this.service.verificarRiesgo(body).subscribe({
          next: (res: any) => {
            if (res.riesgo || res.titulo) {
              this.riesgo = {
                titulo: res.titulo,
                mensaje: res.mensaje,
                recomendaciones: res.recomendaciones,
                exito: !res.riesgo
              };
            } else {
              this.ejecutarGuardar();
            }
          },
          error: () => this.ejecutarGuardar()
        });
      },
      error: (err) => {
        this.error = 'Error de autenticación: ' + err.message;
        this.notificaciones.error('Error de autenticación: ' + err.message);
      }
    });
  }

  cancelarRiesgo() {
    this.riesgo = null;
  }

  confirmarRiesgo() {
    this.riesgo = null;
    this.ejecutarGuardar();
  }

  private ejecutarGuardar() {
    if (this.guardando) return;
    this.guardando = true;
    this.error = '';

    this.auth.getUsuarioId().subscribe({
      next: (usuarioId) => {
        if (!usuarioId) { this.error = 'Usuario no autenticado'; this.guardando = false; return; }

        const data: any = {
          descripcion: this.formulario.value.descripcion,
          monto: this.formulario.value.monto,
          tipo: this.formulario.value.tipo,
          fecha: this.formulario.value.fecha,
          categoria: { nombre: this.formulario.value.categoria },
          usuario: { id: usuarioId }
        };

        const request = this.esEdicion
          ? this.service.actualizar(this.idEdicion!, data)
          : this.service.crear(data);

        request.subscribe({
          next: () => {
            this.notificaciones.exito('Se guardó exitosamente');
            this.router.navigate(['/lista']);
          },
          error: (err) => {
            this.error = err.message;
            this.notificaciones.error(err.message || 'Error al guardar la transacción');
            this.guardando = false;
          }
        });
      },
      error: (err) => {
        this.error = 'Error de autenticación: ' + err.message;
        this.notificaciones.error('Error de autenticación: ' + err.message);
        this.guardando = false;
      }
    });
  }
}
