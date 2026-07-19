import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './registro.html',
  styleUrl: './registro.scss'
})
export class RegistroComponent {
  nombre = '';
  email = '';
  password = '';
  edad: number = 25;
  error = '';
  exito = '';

  constructor(private auth: AuthService, private router: Router) {}

  registrar() {
    this.error = '';
    this.exito = '';
    this.auth.registrar({ nombre: this.nombre, email: this.email, password: this.password, edad: this.edad })
      .subscribe({
        next: () => {
          this.exito = 'Usuario creado correctamente. Redirigiendo al login...';
          setTimeout(() => this.router.navigate(['/login']), 1500);
        },
        error: (err) => this.error = err.error || 'Error al registrar usuario'
      });
  }
}
