import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, switchMap, of } from 'rxjs';
import { LoginResponse, Usuario } from '../models';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
    private apiAuth = `${environment.apiUrl}/auth`;
    private apiUsuarios = `${environment.apiUrl}/api/usuarios`;
    private tokenKey = 'jwt_token';
    private userKey = 'user_data';

    constructor(private http: HttpClient) {}

    login(email: string, password: string): Observable<any> {
        return this.http.post<LoginResponse>(`${this.apiAuth}/login`, { email, password }).pipe(
            tap(res => localStorage.setItem(this.tokenKey, res.token)),
            switchMap(res => {
                const payload = JSON.parse(atob(res.token.split('.')[1]));
                return this.http.get<Usuario>(`${this.apiUsuarios}/email/${payload.sub}`).pipe(
                    tap(u => localStorage.setItem(this.userKey, JSON.stringify({ email: u.email, id: u.id, nombre: u.nombre })))
                );
            })
        );
    }

    registrar(datos: { nombre: string; email: string; password: string; edad: number }): Observable<any> {
        return this.http.post(this.apiUsuarios, datos);
    }

    logout(): void {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.userKey);
    }

    getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    getUsuarioId(): Observable<number | null> {
        const data = localStorage.getItem(this.userKey);
        if (!data) return of(null);
        const parsed = JSON.parse(data);
        if (parsed.id) return of(parsed.id);
        return this.http.get<Usuario>(`${this.apiUsuarios}/email/${parsed.email}`).pipe(
            tap(u => localStorage.setItem(this.userKey, JSON.stringify({ email: u.email, id: u.id }))),
            switchMap(u => of(u.id ?? null))
        );
    }
}
