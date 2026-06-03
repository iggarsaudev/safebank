import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { AccountResponse } from '../../models/account.models';
import { AuthService } from '../../../auth/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly accountService = inject(AccountService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  // creamos un signal que empezará siendo null hasta que responda el backend
  accountData = signal<AccountResponse | null>(null);
  isLoading = signal<boolean>(true);

  ngOnInit(): void {
    // al cargar el componente, solicitamos los datos de la cuenta protegida por jwt
    this.accountService.getMyAccount().subscribe({
      next: (data) => {
        this.accountData.set(data);
        this.isLoading.set(false);
      },
      error: (error) => {
        console.error('error al recuperar la cuenta bancaria', error);
        this.isLoading.set(false);
        // si el token ha expirado o es inválido, forzamos la salida al login
        this.logout();
      },
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
