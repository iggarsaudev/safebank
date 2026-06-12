import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  AuthResponse,
  LoginRequest,
  RegisterRequest,
} from '../models/auth.models';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  // usamos la nueva función inject() de angular en lugar del constructor tradicional
  private readonly http = inject(HttpClient);

  private readonly API_URL = `${environment.apiUrl}/auth`;

  login(request: LoginRequest): Observable<AuthResponse> {
    // enviamos un post al endpoint de login
    return this.http.post<AuthResponse>(`${this.API_URL}/login`, request);
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    // enviamos un post al endpoint de registro
    return this.http.post<AuthResponse>(`${this.API_URL}/register`, request);
  }

  saveToken(token: string): void {
    // guardamos el token de forma segura en el almacenamiento local del navegador
    localStorage.setItem('jwt_token', token);
  }

  getToken(): string | null {
    return localStorage.getItem('jwt_token');
  }

  logout(): void {
    // eliminamos el token para cerrar la sesión
    localStorage.removeItem('jwt_token');
  }
}
